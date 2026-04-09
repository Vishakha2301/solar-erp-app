package com.solarerp.quotation.service;

import com.solarerp.costing.repository.CostingRepository;
import com.solarerp.costing.service.CostingService;
import com.solarerp.customer.entity.Customer;
import com.solarerp.customer.entity.CustomerSite;
import com.solarerp.customer.entity.CustomerType;
import com.solarerp.quotation.entity.Quotation;
import com.solarerp.quotation.entity.QuotationStatus;
import com.solarerp.quotation.repository.QuotationRepository;
import com.solarerp.quotation.service.impl.QuotationDocumentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BuildAddress Tests")
class BuildAddressTest {

    @Mock
    private QuotationRepository quotationRepository;

    @Mock
    private CostingRepository costingRepository;

    @Mock
    private CostingService costingService;

    private QuotationDocumentServiceImpl documentService;

    private Customer customer;
    private CustomerSite site;
    private Quotation quotation;

    @BeforeEach
    void setUp() {
        documentService = new QuotationDocumentServiceImpl(
                quotationRepository,
                costingRepository,
                costingService);

        customer = new Customer();
        customer.setCustomerType(CustomerType.INDIVIDUAL);
        customer.setName("Rahul Sharma");
        customer.setPhone("9876543210");
        customer.setAddress("123 MG Road");
        customer.setCity("Nashik");
        customer.setState("Maharashtra");
        customer.setPincode("422001");

        site = new CustomerSite();
        site.setSiteLabel("Installation Site");
        site.setAddress("Plot 45 MIDC");
        site.setCity("Pune");
        site.setState("Maharashtra");
        site.setPincode("411001");

        quotation = new Quotation();
        quotation.setCustomer(customer);
        quotation.setStatus(QuotationStatus.APPROVED);
        quotation.setValidityDays(30);
        quotation.setDiscount(BigDecimal.ZERO);
        quotation.setCostings(new ArrayList<>());
        quotation.setInstalments(new ArrayList<>());
        quotation.setPackages(new ArrayList<>());
    }

    private String buildAddress(Quotation q) {
        return (String) ReflectionTestUtils.invokeMethod(
                documentService, "buildAddress", q);
    }

    private String buildAddressString(String address, String city,
                                       String state, String pincode) {
        return (String) ReflectionTestUtils.invokeMethod(
                documentService, "buildAddressString",
                address, city, state, pincode);
    }

    @Nested
    @DisplayName("buildAddress() — site priority")
    class SitePriorityTests {

        @Test
        @DisplayName("Uses site address when site has address")
        void buildAddress_withSite_usesSiteAddress() {
            quotation.setCustomerSite(site);

            String result = buildAddress(quotation);

            assertThat(result).contains("Plot 45 MIDC");
            assertThat(result).contains("Pune");
            assertThat(result).doesNotContain("123 MG Road");
            assertThat(result).doesNotContain("Nashik");
        }

        @Test
        @DisplayName("Uses customer address when no site")
        void buildAddress_noSite_usesCustomerAddress() {
            quotation.setCustomerSite(null);

            String result = buildAddress(quotation);

            assertThat(result).contains("123 MG Road");
            assertThat(result).contains("Nashik");
            assertThat(result).contains("Maharashtra");
            assertThat(result).contains("422001");
        }

        @Test
        @DisplayName("Uses customer address when site has null address")
        void buildAddress_siteNullAddress_usesCustomerAddress() {
            site.setAddress(null);
            quotation.setCustomerSite(site);

            String result = buildAddress(quotation);

            assertThat(result).contains("123 MG Road");
            assertThat(result).contains("Nashik");
        }
    }

    @Nested
    @DisplayName("buildAddressString()")
    class BuildAddressStringTests {

        @Test
        @DisplayName("Builds full address with all fields")
        void buildAddressString_allFields_buildsFullAddress() {
            String result = buildAddressString(
                    "123 MG Road", "Nashik",
                    "Maharashtra", "422001");

            assertThat(result)
                    .isEqualTo("123 MG Road, Nashik, Maharashtra - 422001");
        }

        @Test
        @DisplayName("Builds address without pincode")
        void buildAddressString_noPincode_excludesPincode() {
            String result = buildAddressString(
                    "123 MG Road", "Nashik",
                    "Maharashtra", null);

            assertThat(result)
                    .isEqualTo("123 MG Road, Nashik, Maharashtra");
            assertThat(result).doesNotContain(" - ");
        }

        @Test
        @DisplayName("Builds address without city")
        void buildAddressString_noCity_excludesCity() {
            String result = buildAddressString(
                    "123 MG Road", null,
                    "Maharashtra", "422001");

            assertThat(result)
                    .isEqualTo("123 MG Road, Maharashtra - 422001");
        }

        @Test
        @DisplayName("Builds address without state")
        void buildAddressString_noState_excludesState() {
            String result = buildAddressString(
                    "123 MG Road", "Nashik",
                    null, "422001");

            assertThat(result)
                    .isEqualTo("123 MG Road, Nashik - 422001");
        }

        @Test
        @DisplayName("Builds address with only street address")
        void buildAddressString_onlyAddress_returnsAddress() {
            String result = buildAddressString(
                    "123 MG Road", null, null, null);

            assertThat(result).isEqualTo("123 MG Road");
        }

        @Test
        @DisplayName("Returns empty string when all fields null")
        void buildAddressString_allNull_returnsEmpty() {
            String result = buildAddressString(
                    null, null, null, null);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Site address overrides customer address in document")
        void siteAddress_overridesCustomerAddress() {
            quotation.setCustomerSite(site);

            String result = buildAddress(quotation);

            assertThat(result)
                    .isEqualTo(
                            "Plot 45 MIDC, Pune, Maharashtra - 411001");
        }
    }

    @Nested
    @DisplayName("Real address scenarios")
    class RealAddressScenariosTests {

        @Test
        @DisplayName("Typical Maharashtra residential address")
        void typicalResidentialAddress() {
            String result = buildAddressString(
                    "B-504 Shree Ram Empire",
                    "Pune",
                    "Maharashtra",
                    "411001");

            assertThat(result).isEqualTo(
                    "B-504 Shree Ram Empire, Pune, Maharashtra - 411001");
        }

        @Test
        @DisplayName("Customer with company site address")
        void companyWithSiteAddress() {
            Customer company = new Customer();
            company.setCustomerType(CustomerType.COMPANY);
            company.setName("ABC Industries");
            company.setPhone("9876543210");
            company.setAddress("Corporate Office, Nariman Point");
            company.setCity("Mumbai");
            company.setState("Maharashtra");
            company.setPincode("400021");

            CustomerSite factorySite = new CustomerSite();
            factorySite.setSiteLabel("Factory");
            factorySite.setAddress("Plot 12, MIDC Industrial Area");
            factorySite.setCity("Nashik");
            factorySite.setState("Maharashtra");
            factorySite.setPincode("422010");

            quotation.setCustomer(company);
            quotation.setCustomerSite(factorySite);

            String result = buildAddress(quotation);

            assertThat(result).isEqualTo(
                    "Plot 12, MIDC Industrial Area, Nashik, Maharashtra - 422010");
            assertThat(result).doesNotContain("Nariman Point");
            assertThat(result).doesNotContain("Mumbai");
        }
    }
}
