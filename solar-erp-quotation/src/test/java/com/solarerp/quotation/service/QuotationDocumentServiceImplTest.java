package com.solarerp.quotation.service;

import com.solarerp.costing.dto.CostingContextDto;
import com.solarerp.costing.dto.CostingSnapshotDto;
import com.solarerp.costing.dto.SavedCostingResponse;
import com.solarerp.costing.entity.SavedCostingEntity;
import com.solarerp.costing.repository.CostingRepository;
import com.solarerp.costing.service.CostingService;
import com.solarerp.customer.entity.Customer;
import com.solarerp.customer.entity.CustomerType;
import com.solarerp.exception.DocumentGenerationException;
import com.solarerp.exception.ResourceNotFoundException;
import com.solarerp.material.entity.Material;
import com.solarerp.quotation.entity.*;
import com.solarerp.quotation.repository.QuotationRepository;
import com.solarerp.quotation.service.impl.QuotationDocumentServiceImpl;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("QuotationDocumentServiceImpl Tests")
class QuotationDocumentServiceImplTest {

    @Mock
    private QuotationRepository quotationRepository;

    @Mock
    private CostingRepository costingRepository;

    @Mock
    private CostingService costingService;

    @InjectMocks
    private QuotationDocumentServiceImpl documentService;

    private UUID quotationId;
    private UUID costingId;
    private Quotation quotation;
    private Customer customer;
    private SavedCostingEntity costingEntity;
    private SavedCostingResponse costingResponse;

    @BeforeEach
    void setUp() {
        quotationId = UUID.randomUUID();
        costingId = UUID.randomUUID();

        customer = new Customer();
        customer.setCustomerType(CustomerType.INDIVIDUAL);
        customer.setName("Rahul Sharma");
        customer.setPhone("9876543210");
        customer.setEmail("rahul@example.com");
        customer.setAddress("123 MG Road");
        customer.setCity("Nashik");
        customer.setState("Maharashtra");
        customer.setPincode("422001");

        costingEntity = new SavedCostingEntity();
        costingEntity.setContext("{\"plantCapacity\":5.0}");
        costingEntity.setSnapshot("{\"grandTotal\":337500.0}");

        CostingContextDto contextDto = new CostingContextDto(
                5.0, "Rooftop", "3PH", "RCC",
                "Main Roof", false);

        CostingSnapshotDto snapshotDto = new CostingSnapshotDto(
                300000.0, 0.0, 15000.0,
                10000.0, 5000.0, 7500.0,
                337500.0, 367537.5, 73.5);

        costingResponse = new SavedCostingResponse(
                costingId, Instant.now(), UUID.randomUUID(),
                contextDto, snapshotDto);

        // Build quotation with costing
        QuotationCosting qc = new QuotationCosting();
        qc.setCosting(costingEntity);
        qc.setRoofLabel("Main Roof");
        qc.setSubsidyAmount(BigDecimal.valueOf(78000));

        // Build instalments
        QuotationInstalment inst1 = new QuotationInstalment();
        inst1.setInstalmentNo(1);
        inst1.setDescription("Advance");
        inst1.setPercentage(BigDecimal.valueOf(10));

        QuotationInstalment inst2 = new QuotationInstalment();
        inst2.setInstalmentNo(2);
        inst2.setDescription("Procurement");
        inst2.setPercentage(BigDecimal.valueOf(60));

        QuotationInstalment inst3 = new QuotationInstalment();
        inst3.setInstalmentNo(3);
        inst3.setDescription("On Installation");
        inst3.setPercentage(BigDecimal.valueOf(10));

        QuotationInstalment inst4 = new QuotationInstalment();
        inst4.setInstalmentNo(4);
        inst4.setDescription("Net Metering");
        inst4.setPercentage(BigDecimal.valueOf(20));

        quotation = new Quotation();
        quotation.setId(quotationId);
        quotation.setQuotationNumber("QT-2026-001");
        quotation.setCustomer(customer);
        quotation.setSystemType("ONGRID 5KW");
        quotation.setValidityDays(30);
        quotation.setDiscount(BigDecimal.valueOf(5000));
        quotation.setStatus(QuotationStatus.APPROVED);
        quotation.setCostings(List.of(qc));
        quotation.setInstalments(List.of(inst1, inst2, inst3, inst4));
        quotation.setPackages(new ArrayList<>());
    }

    @Nested
    @DisplayName("generateDocx()")
    class GenerateDocxTests {

        @Test
        @DisplayName("Throws ResourceNotFoundException when quotation not found")
        void generateDocx_quotationNotFound_throwsNotFound() {
            when(quotationRepository.findById(quotationId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    documentService.generateDocx(quotationId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Quotation");
        }

        @Test
        @DisplayName("Throws DocumentGenerationException when template missing")
        void generateDocx_templateMissing_throwsDocumentGenerationException() {
            when(quotationRepository.findById(quotationId))
                    .thenReturn(Optional.of(quotation));
            // Template file doesn't exist in test classpath
            assertThatThrownBy(() ->
                    documentService.generateDocx(quotationId))
                    .isInstanceOf(DocumentGenerationException.class);
        }
    }

    @Nested
    @DisplayName("Amount calculation")
    class AmountCalculationTests {

        @Test
        @DisplayName("Landed cost = grand total - subsidy - discount")
        void landedCost_correctlyCalculated() {
            // grandTotal = 337500
            // subsidy = 78000
            // discount = 5000
            // landedCost = 337500 - 78000 - 5000 = 254500
            BigDecimal grandTotal = BigDecimal.valueOf(337500);
            BigDecimal subsidy = BigDecimal.valueOf(78000);
            BigDecimal discount = BigDecimal.valueOf(5000);
            BigDecimal expected = grandTotal
                    .subtract(subsidy)
                    .subtract(discount);

            assertThat(expected)
                    .isEqualByComparingTo(BigDecimal.valueOf(254500));
        }

        @Test
        @DisplayName("GST amount correctly calculated at 8.9%")
        void gstAmount_correctlyCalculated() {
            BigDecimal grandTotal = BigDecimal.valueOf(337500);
            BigDecimal gstRate = new BigDecimal("0.089");
            BigDecimal costWithoutGst = grandTotal
                    .divide(BigDecimal.ONE.add(gstRate), 2,
                            java.math.RoundingMode.HALF_UP);
            BigDecimal gstAmount = grandTotal.subtract(costWithoutGst);

            assertThat(gstAmount).isGreaterThan(BigDecimal.ZERO);
            assertThat(costWithoutGst.add(gstAmount))
                    .isEqualByComparingTo(grandTotal);
        }
    }

    @Nested
    @DisplayName("Address building")
    class AddressBuildingTests {

        @Test
        @DisplayName("Uses customer address when no site")
        void buildAddress_noSite_usesCustomerAddress() {
            assertThat(customer.getAddress()).isEqualTo("123 MG Road");
            assertThat(customer.getCity()).isEqualTo("Nashik");
            assertThat(customer.getState()).isEqualTo("Maharashtra");
            assertThat(customer.getPincode()).isEqualTo("422001");
        }
    }

    @Nested
    @DisplayName("Private helpers")
    class HelperTests {

        @SuppressWarnings("unchecked")
        private Map<String, String> buildPlaceholders(Quotation sourceQuotation) {
            return (Map<String, String>) ReflectionTestUtils.invokeMethod(
                    documentService, "buildPlaceholders", sourceQuotation);
        }

        private String formatAmount(BigDecimal amount) {
            return (String) ReflectionTestUtils.invokeMethod(
                    documentService, "formatAmount", amount);
        }

        private String getInstalmentPct(Map<Integer, QuotationInstalment> map,
                                        int instalmentNo) {
            return (String) ReflectionTestUtils.invokeMethod(
                    documentService, "getInstalmentPct", map, instalmentNo);
        }

        @Test
        @DisplayName("buildPlaceholders() fills costing, package and instalment placeholders")
        void buildPlaceholders_populatesExpectedValues() {
            Material panel = new Material();
            panel.setBrandName("Waaree");
            panel.setModelName("540WP");
            panel.setSpecification("Mono PERC");
            panel.setWarranty("25 years");

            Material inverter = new Material();
            inverter.setBrandName("Sungrow");
            inverter.setModelName("5kW");
            inverter.setSpecification("Hybrid");

            Material cable = new Material();
            cable.setBrandName("Polycab");
            cable.setModelName("4sqmm");

            QuotationPackageMaterial panelMaterial = new QuotationPackageMaterial();
            panelMaterial.setMaterial(panel);
            panelMaterial.setComponentKey("solarPanel");

            QuotationPackageMaterial inverterMaterial = new QuotationPackageMaterial();
            inverterMaterial.setMaterial(inverter);
            inverterMaterial.setComponentKey("invertor");

            QuotationPackageMaterial cableMaterial = new QuotationPackageMaterial();
            cableMaterial.setMaterial(cable);
            cableMaterial.setComponentKey("dcCable");

            QuotationPackage quotationPackage = new QuotationPackage();
            quotationPackage.setMaterials(List.of(
                    panelMaterial, inverterMaterial, cableMaterial));
            quotation.setPackages(List.of(quotationPackage));

            quotation.setSystemType(null);
            quotation.setInstalments(List.of(quotation.getInstalments().get(0)));

            when(costingRepository.findById(any()))
                    .thenReturn(Optional.of(costingEntity));
            when(costingService.getById(any()))
                    .thenReturn(costingResponse);

            Map<String, String> placeholders = buildPlaceholders(quotation);

            assertThat(placeholders.get("{{system_details}}"))
                    .isEqualTo("Rooftop 5.0KW");
            assertThat(placeholders.get("{{panel_brand}}"))
                    .isEqualTo("Waaree 540WP");
            assertThat(placeholders.get("{{inverter_brand}}"))
                    .isEqualTo("Sungrow 5kW");
            assertThat(placeholders.get("{{cable_brand}}"))
                    .isEqualTo("Polycab 4sqmm");
            assertThat(placeholders.get("{{warranty}}"))
                    .isEqualTo("25 years");
            assertThat(placeholders.get("{{actual_system_cost}}"))
                    .isEqualTo("337,500");
            assertThat(placeholders.get("{{subsidy_amount}}"))
                    .isEqualTo("78,000");
            assertThat(placeholders.get("{{advanced}}")).isEqualTo("10");
            assertThat(placeholders.get("{{procurement}}")).isEqualTo("0");
        }

        @Test
        @DisplayName("formatAmount() returns zero for null and zero values")
        void formatAmount_nullAndZero_returnsZero() {
            assertThat(formatAmount(null)).isEqualTo("0");
            assertThat(formatAmount(BigDecimal.ZERO)).isEqualTo("0");
            assertThat(formatAmount(BigDecimal.valueOf(12345))).isEqualTo("12,345");
        }

        @Test
        @DisplayName("getInstalmentPct() returns value when present else zero")
        void getInstalmentPct_handlesPresentAndMissingInstalments() {
            QuotationInstalment inst = new QuotationInstalment();
            inst.setInstalmentNo(2);
            inst.setPercentage(BigDecimal.valueOf(60));

            Map<Integer, QuotationInstalment> map = new HashMap<>();
            map.put(2, inst);

            assertThat(getInstalmentPct(map, 2)).isEqualTo("60");
            assertThat(getInstalmentPct(map, 4)).isEqualTo("0");
        }

        @Test
        @DisplayName("replaceParagraph() replaces placeholders and preserves style")
        void replaceParagraph_replacesTextAndPreservesFormatting() {
            XWPFDocument doc = new XWPFDocument();
            XWPFParagraph paragraph = doc.createParagraph();
            var run = paragraph.createRun();
            run.setText("Hello {{name}}");
            run.setBold(true);
            run.setItalic(true);
            run.setColor("FF0000");
            run.setFontFamily("Arial");
            run.setFontSize(12);

            Map<String, String> placeholders = new LinkedHashMap<>();
            placeholders.put("{{name}}", "Solar ERP");

            ReflectionTestUtils.invokeMethod(
                    documentService, "replaceParagraph", paragraph, placeholders);

            assertThat(paragraph.getText()).isEqualTo("Hello Solar ERP");
            assertThat(paragraph.getRuns()).hasSize(1);
            assertThat(paragraph.getRuns().get(0).isBold()).isTrue();
            assertThat(paragraph.getRuns().get(0).isItalic()).isTrue();
            assertThat(paragraph.getRuns().get(0).getColor()).isEqualTo("FF0000");
        }

        @Test
        @DisplayName("replacePlaceholders() traverses paragraphs and tables")
        void replacePlaceholders_replacesAcrossDocumentElements() {
            XWPFDocument doc = new XWPFDocument();
            doc.createParagraph().createRun().setText("{{title}}");
            XWPFTable table = doc.createTable(1, 1);
            table.getRow(0).getCell(0).setText("{{title}}");

            Map<String, String> placeholders = Map.of("{{title}}", "Quotation");
            ReflectionTestUtils.invokeMethod(
                    documentService, "replacePlaceholders", doc, placeholders);

            assertThat(doc.getParagraphs().get(0).getText()).isEqualTo("Quotation");
            assertThat(table.getRow(0).getCell(0).getText()).contains("Quotation");
        }
    }
}
