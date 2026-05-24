package com.solarerp.material.service;

import com.solarerp.exception.ResourceNotFoundException;
import com.solarerp.material.dto.MaterialCategoryResponse;
import com.solarerp.material.dto.MaterialRequest;
import com.solarerp.material.dto.MaterialResponse;
import com.solarerp.material.entity.Material;
import com.solarerp.material.entity.MaterialCategory;
import com.solarerp.material.repository.MaterialRepository;
import com.solarerp.material.service.impl.MaterialServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MaterialServiceImpl Tests")
class MaterialServiceImplTest {

    @Mock
    private MaterialRepository repository;

    @InjectMocks
    private MaterialServiceImpl materialService;

    private UUID materialId;
    private UUID userId;
    private Material material;
    private MaterialRequest request;

    @BeforeEach
    void setUp() {
        materialId = UUID.randomUUID();
        userId = UUID.randomUUID();

        material = new Material();
        material.setCategory(MaterialCategory.PANEL);
        material.setComponentKey("solarPanel");
        material.setBrandName("Adani");
        material.setModelName("545W Bifacial TOPCon");
        material.setSpecification("Mono/Bifacial, 18 panels");
        material.setUnit("Wp");
        material.setWarranty("25 years performance");
        material.setHsnCode("8541.40");
        material.setActive(true);
        material.setCreatedBy(userId);

        request = new MaterialRequest(
                MaterialCategory.PANEL,
                "solarPanel",
                "Adani",
                "545W Bifacial TOPCon",
                "Mono/Bifacial, 18 panels",
                "Wp",
                "25 years performance",
                "8541.40",
                null,
                null
        );
    }

    @Nested
    @DisplayName("getAll()")
    class GetAllTests {

        @Test
        @DisplayName("Returns all active materials ordered by brand name")
        void getAll_returnsActiveMaterials() {
            when(repository.findAllByActiveTrueOrderByBrandNameAsc())
                    .thenReturn(List.of(material));

            List<MaterialResponse> result = materialService.getAll();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).brandName()).isEqualTo("Adani");
            verify(repository, times(1))
                    .findAllByActiveTrueOrderByBrandNameAsc();
        }

        @Test
        @DisplayName("Returns empty list when no active materials")
        void getAll_noMaterials_returnsEmptyList() {
            when(repository.findAllByActiveTrueOrderByBrandNameAsc())
                    .thenReturn(List.of());

            List<MaterialResponse> result = materialService.getAll();

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getById()")
    class GetByIdTests {

        @Test
        @DisplayName("Returns material when found")
        void getById_existingId_returnsMaterial() {
            when(repository.findById(materialId))
                    .thenReturn(Optional.of(material));

            MaterialResponse result =
                    materialService.getById(materialId);

            assertThat(result).isNotNull();
            assertThat(result.brandName()).isEqualTo("Adani");
            assertThat(result.modelName())
                    .isEqualTo("545W Bifacial TOPCon");
        }

        @Test
        @DisplayName("Throws ResourceNotFoundException when not found")
        void getById_nonExistentId_throwsNotFound() {
            when(repository.findById(materialId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    materialService.getById(materialId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Material");
        }
    }

    @Nested
    @DisplayName("getByCategory()")
    class GetByCategoryTests {

        @Test
        @DisplayName("Returns materials filtered by category")
        void getByCategory_panelCategory_returnsPanels() {
            when(repository
                    .findByCategoryAndActiveTrueOrderByBrandNameAsc(
                            MaterialCategory.PANEL))
                    .thenReturn(List.of(material));

            List<MaterialResponse> result =
                    materialService.getByCategory(MaterialCategory.PANEL);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).category().value())
                    .isEqualTo("PANEL");
            assertThat(result.get(0).category().label())
                    .isEqualTo("Solar Panel");
        }

        @Test
        @DisplayName("Returns empty list when no materials in category")
        void getByCategory_noMaterials_returnsEmptyList() {
            when(repository
                    .findByCategoryAndActiveTrueOrderByBrandNameAsc(
                            MaterialCategory.INVERTER))
                    .thenReturn(List.of());

            List<MaterialResponse> result = materialService
                    .getByCategory(MaterialCategory.INVERTER);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getCategories()")
    class GetCategoriesTests {

        @Test
        @DisplayName("Returns all categories with correct labels")
        void getCategories_returnsAllCategoriesWithLabels() {
            List<MaterialCategoryResponse> result =
                    materialService.getCategories();

            assertThat(result).hasSize(
                    MaterialCategory.values().length);
            assertThat(result)
                    .extracting(MaterialCategoryResponse::value)
                    .contains("PANEL", "INVERTER", "CABLE",
                            "STRUCTURE", "ELECTRICAL", "OTHER");
            assertThat(result)
                    .extracting(MaterialCategoryResponse::label)
                    .contains("Solar Panel", "Inverter", "Cable",
                            "Mounting Structure",
                            "Electrical Components", "Other");
        }

        @Test
        @DisplayName("PANEL category has label Solar Panel")
        void getCategories_panelHasCorrectLabel() {
            List<MaterialCategoryResponse> result =
                    materialService.getCategories();

            MaterialCategoryResponse panel = result.stream()
                    .filter(c -> c.value().equals("PANEL"))
                    .findFirst()
                    .orElseThrow();

            assertThat(panel.label()).isEqualTo("Solar Panel");
        }
    }

    @Nested
    @DisplayName("create()")
    class CreateTests {

        @Test
        @DisplayName("Creates material and returns response")
        void create_validRequest_returnsResponse() {
            when(repository.save(any())).thenReturn(material);

            MaterialResponse result =
                    materialService.create(request, userId);

            assertThat(result).isNotNull();
            assertThat(result.brandName()).isEqualTo("Adani");
            verify(repository, times(1)).save(any());
        }

        @Test
        @DisplayName("Sets createdBy from userId")
        void create_setsCreatedByFromUserId() {
            when(repository.save(any())).thenAnswer(inv -> {
                Material saved = inv.getArgument(0);
                assertThat(saved.getCreatedBy()).isEqualTo(userId);
                return material;
            });

            materialService.create(request, userId);

            verify(repository, times(1)).save(any());
        }

        @Test
        @DisplayName("Category label is correctly mapped")
        void create_categoryLabelCorrectlyMapped() {
            when(repository.save(any())).thenReturn(material);

            MaterialResponse result =
                    materialService.create(request, userId);

            assertThat(result.category().value()).isEqualTo("PANEL");
            assertThat(result.category().label())
                    .isEqualTo("Solar Panel");
        }
    }

    @Nested
    @DisplayName("update()")
    class UpdateTests {

        @Test
        @DisplayName("Updates material successfully")
        void update_existingMaterial_updatesSuccessfully() {
            when(repository.findById(materialId))
                    .thenReturn(Optional.of(material));
            when(repository.save(any())).thenReturn(material);

            MaterialResponse result =
                    materialService.update(materialId, request);

            assertThat(result).isNotNull();
            verify(repository, times(1)).save(any());
        }

        @Test
        @DisplayName("Throws ResourceNotFoundException when not found")
        void update_nonExistentId_throwsNotFound() {
            when(repository.findById(materialId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    materialService.update(materialId, request))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("deactivate()")
    class DeactivateTests {

        @Test
        @DisplayName("Deactivates active material")
        void deactivate_activeMaterial_setsActiveFalse() {
            when(repository.findById(materialId))
                    .thenReturn(Optional.of(material));
            when(repository.save(any())).thenReturn(material);

            materialService.deactivate(materialId);

            assertThat(material.isActive()).isFalse();
            verify(repository, times(1)).save(material);
        }

        @Test
        @DisplayName("Throws ResourceNotFoundException when not found")
        void deactivate_nonExistentId_throwsNotFound() {
            when(repository.findById(materialId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    materialService.deactivate(materialId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
