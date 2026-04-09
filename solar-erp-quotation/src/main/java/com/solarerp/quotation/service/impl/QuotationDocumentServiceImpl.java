package com.solarerp.quotation.service.impl;

import com.solarerp.costing.entity.SavedCostingEntity;
import com.solarerp.costing.repository.CostingRepository;
import com.solarerp.costing.service.CostingService;
import com.solarerp.exception.DocumentGenerationException;
import com.solarerp.exception.ResourceNotFoundException;
import com.solarerp.quotation.entity.Quotation;
import com.solarerp.quotation.entity.QuotationCosting;
import com.solarerp.quotation.entity.QuotationInstalment;
import com.solarerp.quotation.entity.QuotationPackageMaterial;
import com.solarerp.quotation.repository.QuotationRepository;
import com.solarerp.quotation.service.QuotationDocumentService;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class QuotationDocumentServiceImpl implements QuotationDocumentService {

    private final QuotationRepository quotationRepository;
    private final CostingRepository costingRepository;
    private final CostingService costingService;

    public QuotationDocumentServiceImpl(
            QuotationRepository quotationRepository,
            CostingRepository costingRepository,
            CostingService costingService) {
        this.quotationRepository = quotationRepository;
        this.costingRepository = costingRepository;
        this.costingService = costingService;
    }

    @Override
    public byte[] generateDocx(UUID quotationId) {
        Quotation quotation = quotationRepository.findById(quotationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Quotation", quotationId));
        try {
            ClassPathResource resource =
                new ClassPathResource("templates/quotation-template.docx");

            try (InputStream is = resource.getInputStream();
                 XWPFDocument doc = new XWPFDocument(is)) {

                Map<String, String> placeholders =
                        buildPlaceholders(quotation);
                replacePlaceholders(doc, placeholders);

                ByteArrayOutputStream out = new ByteArrayOutputStream();
                doc.write(out);
                return out.toByteArray();
            }
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new DocumentGenerationException(
                    "Failed to generate quotation document: "
                            + e.getMessage(), e);
        }
    }

    private Map<String, String> buildPlaceholders(Quotation quotation) {
        Map<String, String> map = new LinkedHashMap<>();

        // Resolve system details from costing context
        String systemDetails = quotation.getSystemType() != null
                ? quotation.getSystemType() : "";

        // Aggregate commercial details from all costings
        BigDecimal totalGrandTotal = BigDecimal.ZERO;
        BigDecimal totalSubsidy = BigDecimal.ZERO;

        for (QuotationCosting qc : quotation.getCostings()) {
            SavedCostingEntity costing = costingRepository
                    .findById(qc.getCosting().getId())
                    .orElse(null);
            if (costing != null) {
                var response = costingService.getById(costing.getId());

                totalGrandTotal = totalGrandTotal.add(
                        BigDecimal.valueOf(
                                response.snapshot().grandTotal()));

                if (systemDetails.isEmpty()
                        && response.context() != null) {
                    systemDetails = response.context().systemType()
                            + " " + response.context().plantCapacity()
                            + "KW";
                }
            }
            if (qc.getSubsidyAmount() != null) {
                totalSubsidy = totalSubsidy.add(qc.getSubsidyAmount());
            }
        }

        // Cover page
        map.put("{{system_details}}", systemDetails);
        map.put("{{date}}", LocalDate.now()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        map.put("{{quotation_no}}", quotation.getQuotationNumber());
        map.put("{{consumer_name}}", quotation.getCustomer().getName());
        map.put("{{consumer_address}}", buildAddress(quotation));
        map.put("{{phone_number}}", quotation.getCustomer().getPhone());
        map.put("{{cosumer_number}}", "");

        // Component makes from packages
        String panelBrand = "";
        String panelSpec = "";
        String panelWarranty = "";
        String inverterBrand = "";
        String inverterSpec = "";
        String cableBrand = "";

        for (var pkg : quotation.getPackages()) {
            for (QuotationPackageMaterial qpm : pkg.getMaterials()) {
                var material = qpm.getMaterial();
                switch (qpm.getComponentKey()) {
                    case "solarPanel" -> {
                        panelBrand = material.getBrandName() + " "
                                + material.getModelName();
                        panelSpec = material.getSpecification() != null
                                ? material.getSpecification() : "";
                        panelWarranty = material.getWarranty() != null
                                ? material.getWarranty() : "";
                    }
                    case "invertor" -> {
                        inverterBrand = material.getBrandName() + " "
                                + material.getModelName();
                        inverterSpec = material.getSpecification() != null
                                ? material.getSpecification() : "";
                    }
                    case "dcCable" -> cableBrand =
                            material.getBrandName() + " "
                                    + material.getModelName();
                }
            }
        }

        map.put("{{panel_brand}}", panelBrand);
        map.put("{{panel_specification}}", panelSpec);
        map.put("{{inverter_brand}}", inverterBrand);
        map.put("{{inverter_specification}}", inverterSpec);
        map.put("{{cable_brand}}", cableBrand);
        map.put("{{warranty}}", panelWarranty);

        // GST calculation
        BigDecimal gstRate = new BigDecimal("0.089");
        BigDecimal costWithoutGst = totalGrandTotal
                .divide(BigDecimal.ONE.add(gstRate), 2,
                        RoundingMode.HALF_UP);
        BigDecimal gstAmount = totalGrandTotal.subtract(costWithoutGst);
        BigDecimal discount = quotation.getDiscount() != null
                ? quotation.getDiscount() : BigDecimal.ZERO;
        BigDecimal landedCost = totalGrandTotal
                .subtract(totalSubsidy)
                .subtract(discount);

        map.put("{{system_cost_without_gst}}",
                formatAmount(costWithoutGst));
        map.put("{{gst_amout}}", formatAmount(gstAmount));
        map.put("{{actual_system_cost}}", formatAmount(totalGrandTotal));
        map.put("{{subsidy_amount}}", formatAmount(totalSubsidy));
        map.put("{{landed_cost}}", formatAmount(landedCost));
        map.put("{{amount_in_word}}", amountInWords(landedCost));

        // Payment instalments
        Map<Integer, QuotationInstalment> instalmentMap = new HashMap<>();
        for (QuotationInstalment inst : quotation.getInstalments()) {
            instalmentMap.put(inst.getInstalmentNo(), inst);
        }

        map.put("{{advanced}}", getInstalmentPct(instalmentMap, 1));
        map.put("{{procurement}}", getInstalmentPct(instalmentMap, 2));
        map.put("{{installation}}", getInstalmentPct(instalmentMap, 3));
        map.put("{{commisioning}}", getInstalmentPct(instalmentMap, 4));

        return map;
    }

    private void replacePlaceholders(XWPFDocument doc,
                                      Map<String, String> placeholders) {
        for (XWPFParagraph para : doc.getParagraphs()) {
            replaceParagraph(para, placeholders);
        }
        for (XWPFTable table : doc.getTables()) {
            for (XWPFTableRow row : table.getRows()) {
                for (XWPFTableCell cell : row.getTableCells()) {
                    for (XWPFParagraph para : cell.getParagraphs()) {
                        replaceParagraph(para, placeholders);
                    }
                }
            }
        }
        for (XWPFHeader header : doc.getHeaderList()) {
            for (XWPFParagraph para : header.getParagraphs()) {
                replaceParagraph(para, placeholders);
            }
        }
        for (XWPFFooter footer : doc.getFooterList()) {
            for (XWPFParagraph para : footer.getParagraphs()) {
                replaceParagraph(para, placeholders);
            }
        }
    }

    private void replaceParagraph(XWPFParagraph para,
                                   Map<String, String> placeholders) {
        String fullText = para.getText();
        if (fullText == null || fullText.isEmpty()) return;

        boolean hasPlaceholder = placeholders.keySet().stream()
                .anyMatch(fullText::contains);
        if (!hasPlaceholder) return;

        String replaced = fullText;
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            replaced = replaced.replace(entry.getKey(), entry.getValue());
        }

        List<XWPFRun> runs = para.getRuns();
        if (runs.isEmpty()) return;

        XWPFRun firstRun = runs.get(0);
        String fontFamily = firstRun.getFontFamily();
        int fontSize = firstRun.getFontSize();
        boolean bold = firstRun.isBold();
        boolean italic = firstRun.isItalic();
        String color = firstRun.getColor();

        for (int i = runs.size() - 1; i > 0; i--) {
            para.removeRun(i);
        }

        firstRun.setText(replaced, 0);
        if (fontFamily != null) firstRun.setFontFamily(fontFamily);
        if (fontSize > 0) firstRun.setFontSize(fontSize);
        firstRun.setBold(bold);
        firstRun.setItalic(italic);
        if (color != null) firstRun.setColor(color);
    }

    private String buildAddress(Quotation quotation) {
        var customer = quotation.getCustomer();
        var site = quotation.getCustomerSite();
        if (site != null && site.getAddress() != null) {
            return buildAddressString(site.getAddress(), site.getCity(),
                    site.getState(), site.getPincode());
        }
        return buildAddressString(customer.getAddress(),
                customer.getCity(), customer.getState(),
                customer.getPincode());
    }

    private String buildAddressString(String address, String city,
                                       String state, String pincode) {
        StringBuilder sb = new StringBuilder();
        if (address != null) sb.append(address);
        if (city != null) sb.append(", ").append(city);
        if (state != null) sb.append(", ").append(state);
        if (pincode != null) sb.append(" - ").append(pincode);
        return sb.toString();
    }

    private String formatAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) == 0)
            return "0";
        return String.format("%,.0f", amount);
    }

    private String getInstalmentPct(
            Map<Integer, QuotationInstalment> map, int no) {
        QuotationInstalment inst = map.get(no);
        if (inst == null) return "0";
        return inst.getPercentage().toPlainString();
    }

    private String amountInWords(BigDecimal amount) {
        if (amount == null) return "";
        long rupees = amount.longValue();
        return "Rupees " + convertToWords(rupees) + " Only";
    }

    private String convertToWords(long number) {
        if (number == 0) return "Zero";
        String[] ones = {"", "One", "Two", "Three", "Four", "Five",
                "Six", "Seven", "Eight", "Nine", "Ten", "Eleven",
                "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen",
                "Seventeen", "Eighteen", "Nineteen"};
        String[] tens = {"", "", "Twenty", "Thirty", "Forty", "Fifty",
                "Sixty", "Seventy", "Eighty", "Ninety"};
        if (number < 20) return ones[(int) number];
        if (number < 100)
            return tens[(int) (number / 10)] +
                    (number % 10 != 0
                            ? " " + ones[(int) (number % 10)] : "");
        if (number < 1000)
            return ones[(int) (number / 100)] + " Hundred" +
                    (number % 100 != 0
                            ? " " + convertToWords(number % 100) : "");
        if (number < 100000)
            return convertToWords(number / 1000) + " Thousand" +
                    (number % 1000 != 0
                            ? " " + convertToWords(number % 1000) : "");
        if (number < 10000000)
            return convertToWords(number / 100000) + " Lakh" +
                    (number % 100000 != 0
                            ? " " + convertToWords(number % 100000) : "");
        return convertToWords(number / 10000000) + " Crore" +
                (number % 10000000 != 0
                        ? " " + convertToWords(number % 10000000) : "");
    }
}
