package com.solarerp.integration;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Customer Integration Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CustomerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private TestDataHelper helper;
    private String token;
    private static String createdCustomerId;

    @BeforeEach
    void setUp() {
        helper = new TestDataHelper(restTemplate, baseUrl());
        token = helper.obtainToken("admin", "admin123");
    }

    @Nested
    @DisplayName("POST /api/v1/customers")
    class CreateTests {

        @Test
        @Order(1)
        @DisplayName("Creates individual customer successfully")
        void create_individualCustomer_returns201() {
            Map<String, Object> request = Map.of(
                    "customerType", "INDIVIDUAL",
                    "name", "Rahul Sharma",
                    "phone", "9876543210",
                    "email", "rahul@example.com",
                    "address", "123 MG Road",
                    "city", "Nashik",
                    "state", "Maharashtra",
                    "pincode", "422001",
                    "sites", List.of(Map.of(
                            "siteLabel", "Main Roof",
                            "address", "123 MG Road",
                            "city", "Nashik",
                            "state", "Maharashtra",
                            "pincode", "422001",
                            "isDefault", true
                    ))
            );

            ResponseEntity<Map> response = helper.post(
                    "/api/v1/customers", request, token, Map.class);

            assertThat(response.getStatusCode())
                    .isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().get("id")).isNotNull();
            assertThat(response.getBody().get("name"))
                    .isEqualTo("Rahul Sharma");
            assertThat(response.getBody().get("phone"))
                    .isEqualTo("9876543210");
            assertThat(response.getBody().get("active"))
                    .isEqualTo(true);

            createdCustomerId = (String) response.getBody().get("id");
        }

        @Test
        @Order(2)
        @DisplayName("Creates company customer successfully")
        void create_companyCustomer_returns201() {
            Map<String, Object> request = Map.of(
                    "customerType", "COMPANY",
                    "name", "John Doe",
                    "companyName", "ABC Solar Pvt Ltd",
                    "phone", "9876543211",
                    "address", "Plot 45 MIDC",
                    "city", "Pune",
                    "state", "Maharashtra",
                    "pincode", "411001",
                    "sites", List.of()
            );

            ResponseEntity<Map> response = helper.post(
                    "/api/v1/customers", request, token, Map.class);

            assertThat(response.getStatusCode())
                    .isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody().get("customerType"))
                    .isEqualTo("COMPANY");
            assertThat(response.getBody().get("companyName"))
                    .isEqualTo("ABC Solar Pvt Ltd");
        }

        @Test
        @Order(3)
        @DisplayName("Returns 400 when name is blank")
        void create_blankName_returns400() {
            Map<String, Object> request = Map.of(
                    "customerType", "INDIVIDUAL",
                    "name", "",
                    "phone", "9876543210",
                    "sites", List.of()
            );

            ResponseEntity<Map> response = helper.post(
                    "/api/v1/customers", request, token, Map.class);

            assertThat(response.getStatusCode())
                    .isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody().get("status"))
                    .isEqualTo(400);
        }

        @Test
        @Order(4)
        @DisplayName("Returns 400 when phone is blank")
        void create_blankPhone_returns400() {
            Map<String, Object> request = Map.of(
                    "customerType", "INDIVIDUAL",
                    "name", "Test Customer",
                    "phone", "",
                    "sites", List.of()
            );

            ResponseEntity<Map> response = helper.post(
                    "/api/v1/customers", request, token, Map.class);

            assertThat(response.getStatusCode())
                    .isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/customers")
    class GetAllTests {

        @Test
        @DisplayName("Returns list of customers with auth token")
        void getAll_withToken_returns200() {
            ResponseEntity<List> response = helper.get(
                    "/api/v1/customers", token, List.class);

            assertThat(response.getStatusCode())
                    .isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
        }

        @Test
        @DisplayName("Returns 401 without token")
        void getAll_withoutToken_returns401() {
            ResponseEntity<Map> response = restTemplate.getForEntity(
                    baseUrl() + "/api/v1/customers", Map.class);

            assertThat(response.getStatusCode())
                    .isEqualTo(HttpStatus.UNAUTHORIZED);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/customers/{id}")
    class GetByIdTests {

        @Test
        @DisplayName("Returns customer when found")
        void getById_existingCustomer_returns200() {
            // First create a customer
            Map<String, Object> createRequest = Map.of(
                    "customerType", "INDIVIDUAL",
                    "name", "Test Get Customer",
                    "phone", "9876543299",
                    "sites", List.of()
            );
            ResponseEntity<Map> created = helper.post(
                    "/api/v1/customers", createRequest,
                    token, Map.class);
            String id = (String) created.getBody().get("id");

            // Then fetch it
            ResponseEntity<Map> response = helper.get(
                    "/api/v1/customers/" + id, token, Map.class);

            assertThat(response.getStatusCode())
                    .isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().get("id")).isEqualTo(id);
            assertThat(response.getBody().get("name"))
                    .isEqualTo("Test Get Customer");
        }

        @Test
        @DisplayName("Returns 404 for non-existent customer")
        void getById_nonExistent_returns404() {
            ResponseEntity<Map> response = helper.get(
                    "/api/v1/customers/00000000-0000-0000-0000-000000000000",
                    token, Map.class);

            assertThat(response.getStatusCode())
                    .isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(response.getBody().get("status"))
                    .isEqualTo(404);
            assertThat(response.getBody().get("error"))
                    .isEqualTo("Not Found");
        }
    }

    @Nested
    @DisplayName("GET /api/v1/customers/search")
    class SearchTests {

        @Test
        @DisplayName("Returns matching customers by name")
        void search_matchingName_returns200() {
            // Create customer first
            Map<String, Object> createRequest = Map.of(
                    "customerType", "INDIVIDUAL",
                    "name", "Unique Search Name",
                    "phone", "9876543298",
                    "sites", List.of()
            );
            helper.post("/api/v1/customers", createRequest,
                    token, Map.class);

            ResponseEntity<List> response = helper.get(
                    "/api/v1/customers/search?name=Unique",
                    token, List.class);

            assertThat(response.getStatusCode())
                    .isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotEmpty();
        }

        @Test
        @DisplayName("Returns empty list for no match")
        void search_noMatch_returnsEmptyList() {
            ResponseEntity<List> response = helper.get(
                    "/api/v1/customers/search?name=XYZNotExist",
                    token, List.class);

            assertThat(response.getStatusCode())
                    .isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEmpty();
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/customers/{id}")
    class UpdateTests {

        @Test
        @DisplayName("Updates customer successfully")
        void update_existingCustomer_returns200() {
            // Create first
            Map<String, Object> createRequest = Map.of(
                    "customerType", "INDIVIDUAL",
                    "name", "Original Name",
                    "phone", "9876543297",
                    "sites", List.of()
            );
            ResponseEntity<Map> created = helper.post(
                    "/api/v1/customers", createRequest,
                    token, Map.class);
            String id = (String) created.getBody().get("id");

            // Update
            Map<String, Object> updateRequest = Map.of(
                    "customerType", "INDIVIDUAL",
                    "name", "Updated Name",
                    "phone", "9876543297",
                    "city", "Mumbai",
                    "state", "Maharashtra",
                    "sites", List.of()
            );
            ResponseEntity<Map> response = helper.put(
                    "/api/v1/customers/" + id,
                    updateRequest, token, Map.class);

            assertThat(response.getStatusCode())
                    .isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().get("name"))
                    .isEqualTo("Updated Name");
            assertThat(response.getBody().get("city"))
                    .isEqualTo("Mumbai");
        }

        @Test
        @DisplayName("Returns 404 for non-existent customer")
        void update_nonExistent_returns404() {
            Map<String, Object> updateRequest = Map.of(
                    "customerType", "INDIVIDUAL",
                    "name", "Updated Name",
                    "phone", "9876543210",
                    "sites", List.of()
            );
            ResponseEntity<Map> response = helper.put(
                    "/api/v1/customers/00000000-0000-0000-0000-000000000000",
                    updateRequest, token, Map.class);

            assertThat(response.getStatusCode())
                    .isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/customers/{id}")
    class DeactivateTests {

        @Test
        @DisplayName("Deactivates customer successfully")
        void deactivate_existingCustomer_returns204() {
            // Create first
            Map<String, Object> createRequest = Map.of(
                    "customerType", "INDIVIDUAL",
                    "name", "To Be Deactivated",
                    "phone", "9876543296",
                    "sites", List.of()
            );
            ResponseEntity<Map> created = helper.post(
                    "/api/v1/customers", createRequest,
                    token, Map.class);
            String id = (String) created.getBody().get("id");

            // Deactivate
            ResponseEntity<Void> response = helper.delete(
                    "/api/v1/customers/" + id,
                    token, Void.class);

            assertThat(response.getStatusCode())
                    .isEqualTo(HttpStatus.NO_CONTENT);

            // Verify deactivated — should not appear in list
            ResponseEntity<List> listResponse = helper.get(
                    "/api/v1/customers", token, List.class);
            assertThat(listResponse.getBody())
                    .extracting(o -> ((Map) o).get("id"))
                    .doesNotContain(id);
        }

        @Test
        @DisplayName("Returns 404 for non-existent customer")
        void deactivate_nonExistent_returns404() {
            ResponseEntity<Map> response = helper.delete(
                    "/api/v1/customers/00000000-0000-0000-0000-000000000000",
                    token, Map.class);

            assertThat(response.getStatusCode())
                    .isEqualTo(HttpStatus.NOT_FOUND);
        }
    }
}
