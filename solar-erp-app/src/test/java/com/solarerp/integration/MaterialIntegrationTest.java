package com.solarerp.integration;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Material Integration Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MaterialIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private TestDataHelper helper;
    private String token;
    private static String createdMaterialId;

    @BeforeEach
    void setUp() {
        helper = new TestDataHelper(restTemplate, baseUrl());
        token = helper.obtainToken("admin", "admin123");
    }

    @Nested
    @DisplayName("POST /api/v1/materials")
    class CreateTests {

        @Test
        @Order(1)
        @DisplayName("Creates solar panel material successfully")
        void create_solarPanel_returns201() {
            Map<String, Object> request = Map.of(
                    "category", "PANEL",
                    "componentKey", "solarPanel",
                    "brandName", "Adani",
                    "modelName", "545W Bifacial TOPCon",
                    "specification", "Mono/Bifacial, 545Wp",
                    "unit", "Wp",
                    "warranty", "25 years performance",
                    "hsnCode", "8541.40"
            );

            ResponseEntity<Map> response = helper.post(
                    "/api/v1/materials", request, token, Map.class);

            assertThat(response.getStatusCode())
                    .isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().get("id")).isNotNull();
            assertThat(response.getBody().get("brandName"))
                    .isEqualTo("Adani");
            assertThat(response.getBody().get("modelName"))
                    .isEqualTo("545W Bifacial TOPCon");

            // Verify category label is human readable
            Map<String, Object> category =
                    (Map<String, Object>) response.getBody()
                            .get("category");
            assertThat(category.get("value")).isEqualTo("PANEL");
            assertThat(category.get("label"))
                    .isEqualTo("Solar Panel");

            createdMaterialId =
                    (String) response.getBody().get("id");
        }

        @Test
        @Order(2)
        @DisplayName("Creates inverter material successfully")
        void create_inverter_returns201() {
            Map<String, Object> request = Map.of(
                    "category", "INVERTER",
                    "componentKey", "invertor",
                    "brandName", "Solis",
                    "modelName", "5kW Single Phase",
                    "specification", "5kW, 230V, Single Phase",
                    "unit", "kW",
                    "warranty", "10 years",
                    "hsnCode", "8504.40"
            );

            ResponseEntity<Map> response = helper.post(
                    "/api/v1/materials", request, token, Map.class);

            assertThat(response.getStatusCode())
                    .isEqualTo(HttpStatus.CREATED);

            Map<String, Object> category =
                    (Map<String, Object>) response.getBody()
                            .get("category");
            assertThat(category.get("label"))
                    .isEqualTo("Inverter");
        }

        @Test
        @Order(3)
        @DisplayName("Creates cable material successfully")
        void create_cable_returns201() {
            Map<String, Object> request = Map.of(
                    "category", "CABLE",
                    "componentKey", "dcCable",
                    "brandName", "Polycab",
                    "modelName", "4mm DC Solar Cable",
                    "specification", "4mm, UV Resistant",
                    "unit", "metre",
                    "warranty", "5 years",
                    "hsnCode", "8544.49"
            );

            ResponseEntity<Map> response = helper.post(
                    "/api/v1/materials", request, token, Map.class);

            assertThat(response.getStatusCode())
                    .isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody().get("brandName"))
                    .isEqualTo("Polycab");
        }

        @Test
        @Order(4)
        @DisplayName("Returns 400 when brandName is blank")
        void create_blankBrandName_returns400() {
            Map<String, Object> request = Map.of(
                    "category", "PANEL",
                    "componentKey", "solarPanel",
                    "brandName", "",
                    "modelName", "Model"
            );

            ResponseEntity<Map> response = helper.post(
                    "/api/v1/materials", request, token, Map.class);

            assertThat(response.getStatusCode())
                    .isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @Order(5)
        @DisplayName("Returns 400 when category is null")
        void create_nullCategory_returns400() {
            Map<String, Object> request = new java.util.HashMap<>();
            request.put("category", null);
            request.put("componentKey", "solarPanel");
            request.put("brandName", "Adani");
            request.put("modelName", "Model");

            ResponseEntity<Map> response = helper.post(
                    "/api/v1/materials", request, token, Map.class);

            assertThat(response.getStatusCode())
                    .isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/materials")
    class GetAllTests {

        @Test
        @DisplayName("Returns list of materials")
        void getAll_returns200WithMaterials() {
            ResponseEntity<List> response = helper.get(
                    "/api/v1/materials", token, List.class);

            assertThat(response.getStatusCode())
                    .isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
        }

        @Test
        @DisplayName("Returns 401 without token")
        void getAll_withoutToken_returns401() {
            ResponseEntity<Map> response = restTemplate.getForEntity(
                    baseUrl() + "/api/v1/materials", Map.class);

            assertThat(response.getStatusCode())
                    .isEqualTo(HttpStatus.UNAUTHORIZED);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/materials/categories")
    class GetCategoriesTests {

        @Test
        @DisplayName("Returns all categories with correct labels")
        void getCategories_returns200WithAllCategories() {
            ResponseEntity<List> response = helper.get(
                    "/api/v1/materials/categories",
                    token, List.class);

            assertThat(response.getStatusCode())
                    .isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).hasSize(6);

            // Verify labels are human readable not raw enums
            List<Map<String, String>> categories =
                    (List<Map<String, String>>) response.getBody();
            assertThat(categories)
                    .extracting(c -> c.get("value"))
                    .contains("PANEL", "INVERTER", "CABLE",
                            "STRUCTURE", "ELECTRICAL", "OTHER");
            assertThat(categories)
                    .extracting(c -> c.get("label"))
                    .contains("Solar Panel", "Inverter", "Cable",
                            "Mounting Structure",
                            "Electrical Components", "Other");
        }
    }

    @Nested
    @DisplayName("GET /api/v1/materials/category/{category}")
    class GetByCategoryTests {

        @Test
        @DisplayName("Returns materials filtered by PANEL category")
        void getByCategory_panel_returnsPanels() {
            ResponseEntity<List> response = helper.get(
                    "/api/v1/materials/category/PANEL",
                    token, List.class);

            assertThat(response.getStatusCode())
                    .isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();

            List<Map<String, Object>> materials =
                    (List<Map<String, Object>>) response.getBody();
            materials.forEach(m -> {
                Map<String, Object> category =
                        (Map<String, Object>) m.get("category");
                assertThat(category.get("value")).isEqualTo("PANEL");
            });
        }

        @Test
        @DisplayName("Returns materials filtered by INVERTER category")
        void getByCategory_inverter_returnsInverters() {
            ResponseEntity<List> response = helper.get(
                    "/api/v1/materials/category/INVERTER",
                    token, List.class);

            assertThat(response.getStatusCode())
                    .isEqualTo(HttpStatus.OK);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/materials/{id}")
    class GetByIdTests {

        @Test
        @DisplayName("Returns material when found")
        void getById_existingMaterial_returns200() {
            // Create first
            Map<String, Object> createRequest = Map.of(
                    "category", "STRUCTURE",
                    "componentKey", "structure",
                    "brandName", "Ganges",
                    "modelName", "GI Structure",
                    "unit", "set"
            );
            ResponseEntity<Map> created = helper.post(
                    "/api/v1/materials", createRequest,
                    token, Map.class);
            String id = (String) created.getBody().get("id");

            ResponseEntity<Map> response = helper.get(
                    "/api/v1/materials/" + id, token, Map.class);

            assertThat(response.getStatusCode())
                    .isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().get("id")).isEqualTo(id);
            assertThat(response.getBody().get("brandName"))
                    .isEqualTo("Ganges");
        }

        @Test
        @DisplayName("Returns 404 for non-existent material")
        void getById_nonExistent_returns404() {
            ResponseEntity<Map> response = helper.get(
                    "/api/v1/materials/00000000-0000-0000-0000-000000000000",
                    token, Map.class);

            assertThat(response.getStatusCode())
                    .isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(response.getBody().get("status"))
                    .isEqualTo(404);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/materials/search")
    class SearchTests {

        @Test
        @DisplayName("Returns matching materials by brand name")
        void search_matchingBrand_returns200() {
            ResponseEntity<List> response = helper.get(
                    "/api/v1/materials/search?brandName=Adani",
                    token, List.class);

            assertThat(response.getStatusCode())
                    .isEqualTo(HttpStatus.OK);
        }

        @Test
        @DisplayName("Returns empty list for no match")
        void search_noMatch_returnsEmptyList() {
            ResponseEntity<List> response = helper.get(
                    "/api/v1/materials/search?brandName=XYZNotExist",
                    token, List.class);

            assertThat(response.getStatusCode())
                    .isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEmpty();
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/materials/{id}")
    class UpdateTests {

        @Test
        @DisplayName("Updates material successfully")
        void update_existingMaterial_returns200() {
            // Create first
            Map<String, Object> createRequest = Map.of(
                    "category", "ELECTRICAL",
                    "componentKey", "acdb",
                    "brandName", "Havells",
                    "modelName", "ACDB Panel",
                    "unit", "set"
            );
            ResponseEntity<Map> created = helper.post(
                    "/api/v1/materials", createRequest,
                    token, Map.class);
            String id = (String) created.getBody().get("id");

            // Update
            Map<String, Object> updateRequest = Map.of(
                    "category", "ELECTRICAL",
                    "componentKey", "acdb",
                    "brandName", "Havells",
                    "modelName", "ACDB Panel Pro",
                    "specification", "Updated spec",
                    "unit", "set"
            );
            ResponseEntity<Map> response = helper.put(
                    "/api/v1/materials/" + id,
                    updateRequest, token, Map.class);

            assertThat(response.getStatusCode())
                    .isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().get("modelName"))
                    .isEqualTo("ACDB Panel Pro");
            assertThat(response.getBody().get("specification"))
                    .isEqualTo("Updated spec");
        }

        @Test
        @DisplayName("Returns 404 for non-existent material")
        void update_nonExistent_returns404() {
            Map<String, Object> updateRequest = Map.of(
                    "category", "PANEL",
                    "componentKey", "solarPanel",
                    "brandName", "Brand",
                    "modelName", "Model",
                    "unit", "Wp"
            );
            ResponseEntity<Map> response = helper.put(
                    "/api/v1/materials/00000000-0000-0000-0000-000000000000",
                    updateRequest, token, Map.class);

            assertThat(response.getStatusCode())
                    .isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/materials/{id}")
    class DeactivateTests {

        @Test
        @DisplayName("Deactivates material successfully")
        void deactivate_existingMaterial_returns204() {
            // Create first
            Map<String, Object> createRequest = Map.of(
                    "category", "OTHER",
                    "componentKey", "other",
                    "brandName", "To Deactivate Brand",
                    "modelName", "To Deactivate Model",
                    "unit", "set"
            );
            ResponseEntity<Map> created = helper.post(
                    "/api/v1/materials", createRequest,
                    token, Map.class);
            String id = (String) created.getBody().get("id");

            ResponseEntity<Void> response = helper.delete(
                    "/api/v1/materials/" + id,
                    token, Void.class);

            assertThat(response.getStatusCode())
                    .isEqualTo(HttpStatus.NO_CONTENT);

            // Verify deactivated material not in active list
            ResponseEntity<List> listResponse = helper.get(
                    "/api/v1/materials", token, List.class);
            assertThat(listResponse.getBody())
                    .extracting(o -> ((Map) o).get("id"))
                    .doesNotContain(id);
        }

        @Test
        @DisplayName("Returns 404 for non-existent material")
        void deactivate_nonExistent_returns404() {
            ResponseEntity<Map> response = helper.delete(
                    "/api/v1/materials/00000000-0000-0000-0000-000000000000",
                    token, Map.class);

            assertThat(response.getStatusCode())
                    .isEqualTo(HttpStatus.NOT_FOUND);
        }
    }
}
