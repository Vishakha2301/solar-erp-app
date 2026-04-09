package com.solarerp.integration;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Quotation Integration Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class QuotationIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private TestDataHelper helper;
    private String token;

    // Shared state across tests
    private static String customerId;
    private static String costingId;
    private static String panelMaterialId;
    private static String inverterMaterialId;
    private static String quotationId;

    @BeforeEach
    void setUp() {
        helper = new TestDataHelper(restTemplate, baseUrl());
        token = helper.obtainToken("admin", "admin123");
    }

    // ── Setup helpers ─────────────────────────────────────────────

    private String createCustomer() {
        Map<String, Object> request = Map.of(
                "customerType", "INDIVIDUAL",
                "name", "Quotation Test Customer",
                "phone", "9876500001",
                "address", "123 Test Road",
                "city", "Nashik",
                "state", "Maharashtra",
                "pincode", "422001",
                "sites", List.of(Map.of(
                        "siteLabel", "Main Roof",
                        "address", "123 Test Road",
                        "city", "Nashik",
                        "state", "Maharashtra",
                        "pincode", "422001",
                        "isDefault", true
                ))
        );
        ResponseEntity<Map> response = helper.post(
                "/api/v1/customers", request, token, Map.class);
        return (String) response.getBody().get("id");
    }

    private String createCosting() {
        Map<String, Object> request = Map.of(
                "context", Map.of(
                        "plantCapacity", 5.0,
                        "systemType", "ONGRID",
                        "phaseType", "3PH",
                        "roofType", "RCC",
                        "roofIdentifier", "Main Roof",
                        "isSubsidyProject", false
                ),
                "snapshot", Map.of(
                        "systemSubTotal", 300000.0,
                        "subsidyProcessingFee", 0.0,
                        "contingency", 15000.0,
                        "cp1", 10000.0,
                        "cp2", 5000.0,
                        "amc", 7500.0,
                        "grandTotal", 337500.0,
                        "projectCostAfterGst", 367537.5,
                        "perWpAfterGst", 73.5
                )
        );
        ResponseEntity<Map> response = helper.post(
                "/api/v1/costings", request, token, Map.class);
        return (String) response.getBody().get("id");
    }

    private String createPanel() {
        Map<String, Object> request = Map.of(
                "category", "PANEL",
                "componentKey", "solarPanel",
                "brandName", "Adani",
                "modelName", "545W TOPCon",
                "specification", "545Wp Bifacial",
                "unit", "Wp",
                "warranty", "25 years"
        );
        ResponseEntity<Map> response = helper.post(
                "/api/v1/materials", request, token, Map.class);
        return (String) response.getBody().get("id");
    }

    private String createInverter() {
        Map<String, Object> request = Map.of(
                "category", "INVERTER",
                "componentKey", "invertor",
                "brandName", "Solis",
                "modelName", "5kW Hybrid",
                "specification", "5kW, 230V",
                "unit", "kW",
                "warranty", "10 years"
        );
        ResponseEntity<Map> response = helper.post(
                "/api/v1/materials", request, token, Map.class);
        return (String) response.getBody().get("id");
    }

    private Map<String, Object> buildQuotationRequest(
            String custId, String cId,
            String panelId, String inverterId) {
        return Map.of(
                "customerId", custId,
                "systemType", "ONGRID 5KW",
                "validityDays", 30,
                "discount", 0,
                "financingAvailable", false,
                "notes", "Test quotation",
                "costings", List.of(Map.of(
                        "costingId", cId,
                        "roofLabel", "Main Roof",
                        "subsidyAmount", 0
                )),
                "instalments", List.of(
                        Map.of("instalmentNo", 1,
                                "description", "Advance",
                                "percentage", 10),
                        Map.of("instalmentNo", 2,
                                "description", "Procurement",
                                "percentage", 60),
                        Map.of("instalmentNo", 3,
                                "description", "On Installation",
                                "percentage", 10),
                        Map.of("instalmentNo", 4,
                                "description", "Net Metering",
                                "percentage", 20)
                ),
                "packages", List.of(Map.of(
                        "packageName", "Standard",
                        "isRecommended", true,
                        "materials", List.of(
                                Map.of("materialId", panelId,
                                        "componentKey", "solarPanel",
                                        "isRecommended", true),
                                Map.of("materialId", inverterId,
                                        "componentKey", "invertor",
                                        "isRecommended", true)
                        )
                ))
        );
    }

    // ── Tests ─────────────────────────────────────────────────────

    @Nested
    @DisplayName("POST /api/v1/quotations — Create")
    class CreateTests {

        @Test
        @Order(1)
        @DisplayName("Creates quotation with DRAFT status")
        void create_validRequest_returns201WithDraftStatus() {
            customerId = createCustomer();
            costingId = createCosting();
            panelMaterialId = createPanel();
            inverterMaterialId = createInverter();

            Map<String, Object> request = buildQuotationRequest(
                    customerId, costingId,
                    panelMaterialId, inverterMaterialId);

            ResponseEntity<Map> response = helper.post(
                    "/api/v1/quotations", request, token, Map.class);

            assertThat(response.getStatusCode())
                    .isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().get("id")).isNotNull();
            assertThat(response.getBody().get("status"))
                    .isEqualTo("DRAFT");
            assertThat(response.getBody().get("quotationNumber"))
                    .isNotNull()
                    .asString()
                    .startsWith("QT-");
            assertThat(response.getBody().get("systemType"))
                    .isEqualTo("ONGRID 5KW");

            quotationId = (String) response.getBody().get("id");
        }

        @Test
        @Order(2)
        @DisplayName("Quotation has correct customer details")
        void create_quotationHasCorrectCustomerDetails() {
            String cId = createCustomer();
            String coId = createCosting();
            String pId = createPanel();
            String invId = createInverter();

            ResponseEntity<Map> response = helper.post(
                    "/api/v1/quotations",
                    buildQuotationRequest(cId, coId, pId, invId),
                    token, Map.class);

            Map<String, Object> customer =
                    (Map<String, Object>) response.getBody()
                            .get("customer");
            assertThat(customer.get("name"))
                    .isEqualTo("Quotation Test Customer");
            assertThat(customer.get("phone"))
                    .isEqualTo("9876500001");
        }

        @Test
        @Order(3)
        @DisplayName("Quotation has correct instalments")
        void create_quotationHasCorrectInstalments() {
            String cId = createCustomer();
            String coId = createCosting();
            String pId = createPanel();
            String invId = createInverter();

            ResponseEntity<Map> response = helper.post(
                    "/api/v1/quotations",
                    buildQuotationRequest(cId, coId, pId, invId),
                    token, Map.class);

            List<Map<String, Object>> instalments =
                    (List<Map<String, Object>>) response.getBody()
                            .get("instalments");
            assertThat(instalments).hasSize(4);
            assertThat(instalments.get(0).get("description"))
                    .isEqualTo("Advance");
            assertThat(instalments.get(0).get("percentage"))
                    .isEqualTo(10);
        }

        @Test
        @Order(4)
        @DisplayName("Quotation has correct package with materials")
        void create_quotationHasCorrectPackageWithMaterials() {
            String cId = createCustomer();
            String coId = createCosting();
            String pId = createPanel();
            String invId = createInverter();

            ResponseEntity<Map> response = helper.post(
                    "/api/v1/quotations",
                    buildQuotationRequest(cId, coId, pId, invId),
                    token, Map.class);

            List<Map<String, Object>> packages =
                    (List<Map<String, Object>>) response.getBody()
                            .get("packages");
            assertThat(packages).hasSize(1);
            assertThat(packages.get(0).get("packageName"))
                    .isEqualTo("Standard");

            List<Map<String, Object>> materials =
                    (List<Map<String, Object>>) packages.get(0)
                            .get("materials");
            assertThat(materials).hasSize(2);
        }

        @Test
        @Order(5)
        @DisplayName("Returns 404 when customer not found")
        void create_customerNotFound_returns404() {
            Map<String, Object> request = buildQuotationRequest(
                    "00000000-0000-0000-0000-000000000000",
                    createCosting(),
                    createPanel(),
                    createInverter());

            ResponseEntity<Map> response = helper.post(
                    "/api/v1/quotations", request, token, Map.class);

            assertThat(response.getStatusCode())
                    .isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @Order(6)
        @DisplayName("Returns 404 when costing not found")
        void create_costingNotFound_returns404() {
            Map<String, Object> request = buildQuotationRequest(
                    createCustomer(),
                    "00000000-0000-0000-0000-000000000000",
                    createPanel(),
                    createInverter());

            ResponseEntity<Map> response = helper.post(
                    "/api/v1/quotations", request, token, Map.class);

            assertThat(response.getStatusCode())
                    .isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/quotations")
    class GetAllTests {

        @Test
        @DisplayName("Returns list of quotations")
        void getAll_returns200WithQuotations() {
            ResponseEntity<List> response = helper.get(
                    "/api/v1/quotations", token, List.class);

            assertThat(response.getStatusCode())
                    .isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
        }

        @Test
        @DisplayName("Returns 401 without token")
        void getAll_withoutToken_returns401() {
            ResponseEntity<Map> response = restTemplate.getForEntity(
                    baseUrl() + "/api/v1/quotations", Map.class);

            assertThat(response.getStatusCode())
                    .isEqualTo(HttpStatus.UNAUTHORIZED);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/quotations/status/{status}")
    class GetByStatusTests {

        @Test
        @DisplayName("Returns DRAFT quotations")
        void getByStatus_draft_returnsDraftQuotations() {
            ResponseEntity<List> response = helper.get(
                    "/api/v1/quotations/status/DRAFT",
                    token, List.class);

            assertThat(response.getStatusCode())
                    .isEqualTo(HttpStatus.OK);
            List<Map<String, Object>> quotations =
                    (List<Map<String, Object>>) response.getBody();
            quotations.forEach(q ->
                    assertThat(q.get("status")).isEqualTo("DRAFT"));
        }
    }

    @Nested
    @DisplayName("Full Approval Workflow")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class ApprovalWorkflowTests {

        @Test
        @Order(10)
        @DisplayName("Step 1 — DRAFT quotation can be submitted")
        void workflow_step1_submitDraft() {
            // Ensure quotationId is set
            if (quotationId == null) {
                customerId = createCustomer();
                costingId = createCosting();
                panelMaterialId = createPanel();
                inverterMaterialId = createInverter();
                ResponseEntity<Map> created = helper.post(
                        "/api/v1/quotations",
                        buildQuotationRequest(customerId,
                                costingId, panelMaterialId,
                                inverterMaterialId),
                        token, Map.class);
                quotationId = (String) created.getBody().get("id");
            }

            ResponseEntity<Map> response = helper.post(
                    "/api/v1/quotations/" + quotationId + "/submit",
                    Map.of(), token, Map.class);

            assertThat(response.getStatusCode())
                    .isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().get("status"))
                    .isEqualTo("SUBMITTED");
            assertThat(response.getBody().get("submittedAt"))
                    .isNotNull();
        }

        @Test
        @Order(11)
        @DisplayName("Step 2 — SUBMITTED quotation cannot be submitted again")
        void workflow_step2_cannotResubmitSubmitted() {
            ResponseEntity<Map> response = helper.post(
                    "/api/v1/quotations/" + quotationId + "/submit",
                    Map.of(), token, Map.class);

            assertThat(response.getStatusCode())
                    .isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody().get("status"))
                    .isEqualTo(400);
        }

        @Test
        @Order(12)
        @DisplayName("Step 3 — SUBMITTED quotation can be rejected")
        void workflow_step3_rejectSubmitted() {
            ResponseEntity<Map> response = restTemplate.exchange(
                    baseUrl() + "/api/v1/quotations/"
                            + quotationId + "/reject"
                            + "?rejectionReason=Price+too+high",
                    HttpMethod.POST,
                    new HttpEntity<>(helper.authHeaders(token)),
                    Map.class);

            assertThat(response.getStatusCode())
                    .isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().get("status"))
                    .isEqualTo("REJECTED");
            assertThat(response.getBody().get("rejectionReason"))
                    .isEqualTo("Price too high");
        }

        @Test
        @Order(13)
        @DisplayName("Step 4 — REJECTED quotation can be edited")
        void workflow_step4_editRejected() {
            Map<String, Object> updateRequest = buildQuotationRequest(
                    customerId, costingId,
                    panelMaterialId, inverterMaterialId);

            ResponseEntity<Map> response = helper.put(
                    "/api/v1/quotations/" + quotationId,
                    updateRequest, token, Map.class);

            assertThat(response.getStatusCode())
                    .isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().get("status"))
                    .isEqualTo("REJECTED");
        }

        @Test
        @Order(14)
        @DisplayName("Step 5 — REJECTED quotation resubmit sets REVISED")
        void workflow_step5_resubmitRejectedSetsRevised() {
            ResponseEntity<Map> response = helper.post(
                    "/api/v1/quotations/" + quotationId + "/submit",
                    Map.of(), token, Map.class);

            assertThat(response.getStatusCode())
                    .isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().get("status"))
                    .isEqualTo("REVISED");
            assertThat(response.getBody()
                    .get("rejectionReason")).isNull();
        }

        @Test
        @Order(15)
        @DisplayName("Step 6 — REVISED quotation can be approved")
        void workflow_step6_approveRevised() {
            ResponseEntity<Map> response = restTemplate.exchange(
                    baseUrl() + "/api/v1/quotations/"
                            + quotationId + "/approve"
                            + "?approvalNotes=Looks+good",
                    HttpMethod.POST,
                    new HttpEntity<>(helper.authHeaders(token)),
                    Map.class);

            assertThat(response.getStatusCode())
                    .isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().get("status"))
                    .isEqualTo("APPROVED");
            assertThat(response.getBody().get("approvalNotes"))
                    .isEqualTo("Looks good");
            assertThat(response.getBody()
                    .get("approvedRejectedAt")).isNotNull();
        }

        @Test
        @Order(16)
        @DisplayName("Step 7 — APPROVED quotation cannot be edited")
        void workflow_step7_cannotEditApproved() {
            Map<String, Object> updateRequest = buildQuotationRequest(
                    customerId, costingId,
                    panelMaterialId, inverterMaterialId);

            ResponseEntity<Map> response = helper.put(
                    "/api/v1/quotations/" + quotationId,
                    updateRequest, token, Map.class);

            assertThat(response.getStatusCode())
                    .isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody().get("message")
                    .toString())
                    .contains("DRAFT or REJECTED");
        }

        @Test
        @Order(17)
        @DisplayName("Step 8 — APPROVED quotation cannot be deleted")
        void workflow_step8_cannotDeleteApproved() {
            ResponseEntity<Map> response = helper.delete(
                    "/api/v1/quotations/" + quotationId,
                    token, Map.class);

            assertThat(response.getStatusCode())
                    .isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody().get("message")
                    .toString())
                    .contains("Only DRAFT");
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/quotations/{id}")
    class DeleteTests {

        @Test
        @DisplayName("DRAFT quotation can be deleted")
        void delete_draftQuotation_returns204() {
            String cId = createCustomer();
            String coId = createCosting();
            String pId = createPanel();
            String invId = createInverter();

            ResponseEntity<Map> created = helper.post(
                    "/api/v1/quotations",
                    buildQuotationRequest(cId, coId, pId, invId),
                    token, Map.class);
            String id = (String) created.getBody().get("id");

            ResponseEntity<Void> response = helper.delete(
                    "/api/v1/quotations/" + id,
                    token, Void.class);

            assertThat(response.getStatusCode())
                    .isEqualTo(HttpStatus.NO_CONTENT);

            // Verify deleted
            ResponseEntity<Map> getResponse = helper.get(
                    "/api/v1/quotations/" + id, token, Map.class);
            assertThat(getResponse.getStatusCode())
                    .isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/quotations/{id}/document")
    class DocumentTests {

        @Test
        @DisplayName("Returns docx document for approved quotation")
        void downloadDocument_approvedQuotation_returns200() {
            // Create full quotation and approve it
            String cId = createCustomer();
            String coId = createCosting();
            String pId = createPanel();
            String invId = createInverter();

            ResponseEntity<Map> created = helper.post(
                    "/api/v1/quotations",
                    buildQuotationRequest(cId, coId, pId, invId),
                    token, Map.class);
            String id = (String) created.getBody().get("id");

            // Submit
            helper.post("/api/v1/quotations/" + id + "/submit",
                    Map.of(), token, Map.class);

            // Approve
            restTemplate.exchange(
                    baseUrl() + "/api/v1/quotations/" + id
                            + "/approve",
                    HttpMethod.POST,
                    new HttpEntity<>(helper.authHeaders(token)),
                    Map.class);

            // Download document
            HttpEntity<Void> entity = new HttpEntity<>(
                    helper.authHeaders(token));
            ResponseEntity<byte[]> response = restTemplate.exchange(
                    baseUrl() + "/api/v1/quotations/" + id
                            + "/document",
                    HttpMethod.GET, entity, byte[].class);

            assertThat(response.getStatusCode())
                    .isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().length).isGreaterThan(0);
            assertThat(response.getHeaders().getContentType()
                    .toString())
                    .contains(
                            "application/vnd.openxmlformats-officedocument");
        }

        @Test
        @DisplayName("Returns 404 for non-existent quotation document")
        void downloadDocument_notFound_returns404() {
            ResponseEntity<Map> response = helper.get(
                    "/api/v1/quotations/00000000-0000-0000-0000-000000000000/document",
                    token, Map.class);

            assertThat(response.getStatusCode())
                    .isEqualTo(HttpStatus.NOT_FOUND);
        }
    }
}
