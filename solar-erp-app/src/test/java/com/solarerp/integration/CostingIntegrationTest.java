package com.solarerp.integration;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Costing Integration Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CostingIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private TestDataHelper helper;
    private String token;
    private static String createdCostingId;

    @BeforeEach
    void setUp() {
        helper = new TestDataHelper(restTemplate, baseUrl());
        token = helper.obtainToken("admin", "admin123");
    }

    private Map<String, Object> buildCostingRequest() {
        return Map.of(
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
    }

    private Map<String, Object> buildSubsidyCostingRequest() {
        return Map.of(
                "context", Map.of(
                        "plantCapacity", 3.0,
                        "systemType", "ONGRID",
                        "phaseType", "1PH",
                        "roofType", "RCC",
                        "roofIdentifier", "South Roof",
                        "isSubsidyProject", true
                ),
                "snapshot", Map.of(
                        "systemSubTotal", 180000.0,
                        "subsidyProcessingFee", 5000.0,
                        "contingency", 9000.0,
                        "cp1", 6000.0,
                        "cp2", 3000.0,
                        "amc", 4500.0,
                        "grandTotal", 207500.0,
                        "projectCostAfterGst", 225978.0,
                        "perWpAfterGst", 75.3
                )
        );
    }

    @Nested
    @DisplayName("POST /api/v1/costings")
    class CreateTests {

        @Test
        @Order(1)
        @DisplayName("Creates costing with typed context and snapshot")
        void create_validCosting_returns201() {
            ResponseEntity<Map> response = helper.post(
                    "/api/v1/costings",
                    buildCostingRequest(),
                    token, Map.class);

            assertThat(response.getStatusCode())
                    .isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().get("id")).isNotNull();

            // Verify context is returned as typed object
            Map<String, Object> context =
                    (Map<String, Object>) response.getBody()
                            .get("context");
            assertThat(context.get("plantCapacity")).isEqualTo(5.0);
            assertThat(context.get("systemType")).isEqualTo("ONGRID");
            assertThat(context.get("roofIdentifier"))
                    .isEqualTo("Main Roof");
            assertThat(context.get("isSubsidyProject"))
                    .isEqualTo(false);

            // Verify snapshot is returned as typed object
            Map<String, Object> snapshot =
                    (Map<String, Object>) response.getBody()
                            .get("snapshot");
            assertThat(snapshot.get("grandTotal")).isEqualTo(337500.0);
            assertThat(snapshot.get("plantCapacity")).isNull();

            createdCostingId =
                    (String) response.getBody().get("id");
        }

        @Test
        @Order(2)
        @DisplayName("Creates subsidy costing correctly")
        void create_subsidyCosting_returns201() {
            ResponseEntity<Map> response = helper.post(
                    "/api/v1/costings",
                    buildSubsidyCostingRequest(),
                    token, Map.class);

            assertThat(response.getStatusCode())
                    .isEqualTo(HttpStatus.CREATED);

            Map<String, Object> context =
                    (Map<String, Object>) response.getBody()
                            .get("context");
            assertThat(context.get("isSubsidyProject"))
                    .isEqualTo(true);
            assertThat(context.get("plantCapacity")).isEqualTo(3.0);
        }

        @Test
        @Order(3)
        @DisplayName("Returns 400 when context is null")
        void create_nullContext_returns400() {
            Map<String, Object> request = new java.util.HashMap<>();
            request.put("context", null);
            request.put("snapshot", Map.of(
                    "grandTotal", 337500.0));

            ResponseEntity<Map> response = helper.post(
                    "/api/v1/costings", request, token, Map.class);

            assertThat(response.getStatusCode())
                    .isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @Order(4)
        @DisplayName("Returns 400 when snapshot is null")
        void create_nullSnapshot_returns400() {
            Map<String, Object> request = new java.util.HashMap<>();
            request.put("context", Map.of(
                    "plantCapacity", 5.0,
                    "systemType", "ONGRID",
                    "phaseType", "3PH",
                    "roofType", "RCC",
                    "roofIdentifier", "Main Roof",
                    "isSubsidyProject", false));
            request.put("snapshot", null);

            ResponseEntity<Map> response = helper.post(
                    "/api/v1/costings", request, token, Map.class);

            assertThat(response.getStatusCode())
                    .isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/costings")
    class GetAllTests {

        @Test
        @DisplayName("Returns list of costings ordered by createdAt desc")
        void getAll_returns200WithCostings() {
            // Create two costings
            helper.post("/api/v1/costings",
                    buildCostingRequest(), token, Map.class);
            helper.post("/api/v1/costings",
                    buildSubsidyCostingRequest(), token, Map.class);

            ResponseEntity<List> response = helper.get(
                    "/api/v1/costings", token, List.class);

            assertThat(response.getStatusCode())
                    .isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotEmpty();

            // Verify typed context and snapshot in response
            List<Map<String, Object>> costings =
                    (List<Map<String, Object>>) response.getBody();
            costings.forEach(c -> {
                assertThat(c.get("context")).isNotNull();
                assertThat(c.get("snapshot")).isNotNull();
                Map<String, Object> ctx =
                        (Map<String, Object>) c.get("context");
                assertThat(ctx.get("plantCapacity")).isNotNull();
                assertThat(ctx.get("systemType")).isNotNull();
            });
        }

        @Test
        @DisplayName("Returns 401 without token")
        void getAll_withoutToken_returns401() {
            ResponseEntity<Map> response = restTemplate.getForEntity(
                    baseUrl() + "/api/v1/costings", Map.class);

            assertThat(response.getStatusCode())
                    .isEqualTo(HttpStatus.UNAUTHORIZED);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/costings/{id}")
    class GetByIdTests {

        @Test
        @DisplayName("Returns costing when found")
        void getById_existingCosting_returns200() {
            ResponseEntity<Map> created = helper.post(
                    "/api/v1/costings",
                    buildCostingRequest(), token, Map.class);
            String id = (String) created.getBody().get("id");

            ResponseEntity<Map> response = helper.get(
                    "/api/v1/costings/" + id, token, Map.class);

            assertThat(response.getStatusCode())
                    .isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().get("id")).isEqualTo(id);

            Map<String, Object> snapshot =
                    (Map<String, Object>) response.getBody()
                            .get("snapshot");
            assertThat(snapshot.get("grandTotal"))
                    .isEqualTo(337500.0);
        }

        @Test
        @DisplayName("Returns 404 for non-existent costing")
        void getById_nonExistent_returns404() {
            ResponseEntity<Map> response = helper.get(
                    "/api/v1/costings/00000000-0000-0000-0000-000000000000",
                    token, Map.class);

            assertThat(response.getStatusCode())
                    .isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(response.getBody().get("status"))
                    .isEqualTo(404);
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/costings/{id}")
    class UpdateTests {

        @Test
        @DisplayName("Owner can update costing")
        void update_owner_returns200() {
            ResponseEntity<Map> created = helper.post(
                    "/api/v1/costings",
                    buildCostingRequest(), token, Map.class);
            String id = (String) created.getBody().get("id");

            Map<String, Object> updateRequest = Map.of(
                    "context", Map.of(
                            "plantCapacity", 7.5,
                            "systemType", "ONGRID",
                            "phaseType", "3PH",
                            "roofType", "RCC",
                            "roofIdentifier", "Updated Roof",
                            "isSubsidyProject", false
                    ),
                    "snapshot", Map.of(
                            "systemSubTotal", 450000.0,
                            "subsidyProcessingFee", 0.0,
                            "contingency", 22500.0,
                            "cp1", 15000.0,
                            "cp2", 7500.0,
                            "amc", 11250.0,
                            "grandTotal", 506250.0,
                            "projectCostAfterGst", 551306.0,
                            "perWpAfterGst", 73.5
                    )
            );

            ResponseEntity<Map> response = helper.put(
                    "/api/v1/costings/" + id,
                    updateRequest, token, Map.class);

            assertThat(response.getStatusCode())
                    .isEqualTo(HttpStatus.OK);

            Map<String, Object> context =
                    (Map<String, Object>) response.getBody()
                            .get("context");
            assertThat(context.get("plantCapacity")).isEqualTo(7.5);
            assertThat(context.get("roofIdentifier"))
                    .isEqualTo("Updated Roof");

            Map<String, Object> snapshot =
                    (Map<String, Object>) response.getBody()
                            .get("snapshot");
            assertThat(snapshot.get("grandTotal"))
                    .isEqualTo(506250.0);
        }

        @Test
        @DisplayName("Returns 404 for non-existent costing")
        void update_nonExistent_returns404() {
            ResponseEntity<Map> response = helper.put(
                    "/api/v1/costings/00000000-0000-0000-0000-000000000000",
                    buildCostingRequest(), token, Map.class);

            assertThat(response.getStatusCode())
                    .isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/costings/{id}")
    class DeleteTests {

        @Test
        @DisplayName("Owner can delete costing")
        void delete_owner_returns204() {
            ResponseEntity<Map> created = helper.post(
                    "/api/v1/costings",
                    buildCostingRequest(), token, Map.class);
            String id = (String) created.getBody().get("id");

            ResponseEntity<Void> response = helper.delete(
                    "/api/v1/costings/" + id,
                    token, Void.class);

            assertThat(response.getStatusCode())
                    .isEqualTo(HttpStatus.NO_CONTENT);

            // Verify deleted
            ResponseEntity<Map> getResponse = helper.get(
                    "/api/v1/costings/" + id, token, Map.class);
            assertThat(getResponse.getStatusCode())
                    .isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("Returns 404 for non-existent costing")
        void delete_nonExistent_returns404() {
            ResponseEntity<Map> response = helper.delete(
                    "/api/v1/costings/00000000-0000-0000-0000-000000000000",
                    token, Map.class);

            assertThat(response.getStatusCode())
                    .isEqualTo(HttpStatus.NOT_FOUND);
        }
    }
}
