package com.solarerp.material.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.solarerp.exception.GlobalExceptionHandler;
import com.solarerp.exception.ResourceNotFoundException;
import com.solarerp.material.dto.MaterialCategoryResponse;
import com.solarerp.material.dto.MaterialRequest;
import com.solarerp.material.dto.MaterialResponse;
import com.solarerp.material.entity.MaterialCategory;
import com.solarerp.material.service.MaterialService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MaterialController Tests")
class MaterialControllerTest {

    @Mock
    private MaterialService materialService;

    @InjectMocks
    private MaterialController materialController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private UUID materialId;
    private UUID userId;
    private MaterialResponse materialResponse;
    private MaterialRequest materialRequest;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(materialController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        materialId = UUID.randomUUID();
        userId = UUID.randomUUID();

        materialResponse = new MaterialResponse(
                materialId,
                new MaterialCategoryResponse("PANEL", "Solar Panel"),
                "solarPanel",
                "Adani",
                "545W Bifacial TOPCon",
                "Mono/Bifacial, 18 panels",
                "Wp",
                "25 years",
                "8541.40",
                true,
                Instant.now(),
                userId
        );

        materialRequest = new MaterialRequest(
                MaterialCategory.PANEL,
                "solarPanel",
                "Adani",
                "545W Bifacial TOPCon",
                "Mono/Bifacial, 18 panels",
                "Wp",
                "25 years",
                "8541.40"
        );
    }

    @Nested
    @DisplayName("GET /api/v1/materials")
    class GetAllTests {

        @Test
        @DisplayName("Returns 200 with list of materials")
        void getAll_returns200WithMaterials() throws Exception {
            when(materialService.getAll())
                    .thenReturn(List.of(materialResponse));

            mockMvc.perform(get("/api/v1/materials"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].brandName")
                            .value("Adani"))
                    .andExpect(jsonPath("$[0].category.value")
                            .value("PANEL"))
                    .andExpect(jsonPath("$[0].category.label")
                            .value("Solar Panel"));
        }

        @Test
        @DisplayName("Returns 200 with empty list when no materials")
        void getAll_noMaterials_returns200WithEmptyList()
                throws Exception {
            when(materialService.getAll()).thenReturn(List.of());

            mockMvc.perform(get("/api/v1/materials"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isEmpty());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/materials/{id}")
    class GetByIdTests {

        @Test
        @DisplayName("Returns 200 with material when found")
        void getById_existingId_returns200() throws Exception {
            when(materialService.getById(materialId))
                    .thenReturn(materialResponse);

            mockMvc.perform(
                            get("/api/v1/materials/{id}", materialId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id")
                            .value(materialId.toString()))
                    .andExpect(jsonPath("$.brandName")
                            .value("Adani"));
        }

        @Test
        @DisplayName("Returns 404 when material not found")
        void getById_notFound_returns404() throws Exception {
            when(materialService.getById(materialId))
                    .thenThrow(new ResourceNotFoundException(
                            "Material", materialId));

            mockMvc.perform(
                            get("/api/v1/materials/{id}", materialId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/materials/categories")
    class GetCategoriesTests {

        @Test
        @DisplayName("Returns 200 with all categories")
        void getCategories_returns200WithAllCategories()
                throws Exception {
            List<MaterialCategoryResponse> categories = List.of(
                    new MaterialCategoryResponse("PANEL", "Solar Panel"),
                    new MaterialCategoryResponse("INVERTER", "Inverter"),
                    new MaterialCategoryResponse("CABLE", "Cable"),
                    new MaterialCategoryResponse(
                            "STRUCTURE", "Mounting Structure"),
                    new MaterialCategoryResponse(
                            "ELECTRICAL", "Electrical Components"),
                    new MaterialCategoryResponse("OTHER", "Other")
            );
            when(materialService.getCategories())
                    .thenReturn(categories);

            mockMvc.perform(get("/api/v1/materials/categories"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].value").value("PANEL"))
                    .andExpect(jsonPath("$[0].label")
                            .value("Solar Panel"));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/materials/category/{category}")
    class GetByCategoryTests {

        @Test
        @DisplayName("Returns 200 with materials filtered by category")
        void getByCategory_panel_returns200WithPanels()
                throws Exception {
            when(materialService.getByCategory(MaterialCategory.PANEL))
                    .thenReturn(List.of(materialResponse));

            mockMvc.perform(get(
                            "/api/v1/materials/category/{category}",
                            "PANEL"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].category.value")
                            .value("PANEL"));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/materials/component/{componentKey}")
    class GetByComponentKeyTests {

        @Test
        @DisplayName("Returns 200 with materials by component key")
        void getByComponentKey_solarPanel_returns200()
                throws Exception {
            when(materialService.getByComponentKey("solarPanel"))
                    .thenReturn(List.of(materialResponse));

            mockMvc.perform(get(
                            "/api/v1/materials/component/{key}",
                            "solarPanel"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].componentKey")
                            .value("solarPanel"));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/materials/search")
    class SearchTests {

        @Test
        @DisplayName("Returns 200 with matching materials")
        void search_matchingBrand_returns200() throws Exception {
            when(materialService.search("Adani"))
                    .t​​​​​​​​​​​​​​​​
