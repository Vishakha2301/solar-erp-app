package com.solarerp.costing.service;

import com.solarerp.costing.dto.CostingContextDto;
import com.solarerp.costing.dto.CostingSnapshotDto;
import com.solarerp.costing.dto.SavedCostingRequest;
import com.solarerp.costing.dto.SavedCostingResponse;
import com.solarerp.costing.entity.SavedCostingEntity;
import com.solarerp.costing.repository.CostingRepository;
import com.solarerp.costing.service.impl.CostingServiceImpl;
import com.solarerp.exception.ForbiddenException;
import com.solarerp.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CostingServiceImpl Tests")
class CostingServiceImplTest {

    @Mock
    private CostingRepository repository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private CostingServiceImpl costingService;

    private UUID costingId;
    private UUID userId;
    private UUID differentUserId;
    private SavedCostingEntity entity;
    private CostingContextDto contextDto;
    private CostingSnapshotDto snapshotDto;
    private SavedCostingRequest request;

    @BeforeEach
    void setUp() throws Exception {
        costingId = UUID.randomUUID();
        userId = UUID.randomUUID();
        differentUserId = UUID.randomUUID();

        contextDto = new CostingContextDto(
                5.0, "Rooftop", "3PH", "RCC",
                "Main Roof", false);

        snapshotDto = new CostingSnapshotDto(
                300000.0, 0.0, 15000.0,
                10000.0, 5000.0, 7500.0,
                337500.0, 367537.5, 73.5);

        request = new SavedCostingRequest(contextDto, snapshotDto);

        entity = new SavedCostingEntity();
        entity.setCreatedBy(userId);
        entity.setContext("{\"plantCapacity\":5.0}");
        entity.setSnapshot("{\"grandTotal\":337500.0}");

    }

    private void stubWriteJson() throws Exception {
        lenient().when(objectMapper.writeValueAsString(contextDto))
                .thenReturn("{\"plantCapacity\":5.0}");
        lenient().when(objectMapper.writeValueAsString(snapshotDto))
                .thenReturn("{\"grandTotal\":337500.0}");
    }

    private void stubReadJson() throws Exception {
        lenient().when(objectMapper.readValue(
                "{\"plantCapacity\":5.0}", CostingContextDto.class))
                .thenReturn(contextDto);
        lenient().when(objectMapper.readValue(
                "{\"grandTotal\":337500.0}", CostingSnapshotDto.class))
                .thenReturn(snapshotDto);
    }

    @Nested
    @DisplayName("getAll()")
    class GetAllTests {

        @Test
        @DisplayName("Returns all costings ordered by createdAt desc")
        void getAll_returnsAllCostings() throws Exception {
            stubReadJson();
            when(repository.findAllByOrderByCreatedAtDesc())
                    .thenReturn(List.of(entity));

            List<SavedCostingResponse> result = costingService.getAll();

            assertThat(result).hasSize(1);
            verify(repository, times(1))
                    .findAllByOrderByCreatedAtDesc();
        }

        @Test
        @DisplayName("Returns empty list when no costings exist")
        void getAll_noCostings_returnsEmptyList() {
            when(repository.findAllByOrderByCreatedAtDesc())
                    .thenReturn(List.of());

            List<SavedCostingResponse> result = costingService.getAll();

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getById()")
    class GetByIdTests {

        @Test
        @DisplayName("Returns costing when found")
        void getById_existingId_returnsCosting() throws Exception {
            stubReadJson();
            when(repository.findById(costingId))
                    .thenReturn(Optional.of(entity));

            SavedCostingResponse result =
                    costingService.getById(costingId);

            assertThat(result).isNotNull();
            assertThat(result.context()).isEqualTo(contextDto);
            assertThat(result.snapshot()).isEqualTo(snapshotDto);
        }

        @Test
        @DisplayName("Throws ResourceNotFoundException when not found")
        void getById_nonExistentId_throwsNotFound() {
            when(repository.findById(costingId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    costingService.getById(costingId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Costing");
        }
    }

    @Nested
    @DisplayName("create()")
    class CreateTests {

        @Test
        @DisplayName("Creates costing and returns response")
        void create_validRequest_returnsResponse() throws Exception {
            stubWriteJson();
            stubReadJson();
            when(repository.save(any())).thenReturn(entity);

            SavedCostingResponse result =
                    costingService.create(request, userId);

            assertThat(result).isNotNull();
            verify(repository, times(1)).save(any());
        }

        @Test
        @DisplayName("Sets createdBy from userId")
        void create_setsCreatedByFromUserId() throws Exception {
            stubWriteJson();
            stubReadJson();
            when(repository.save(any())).thenAnswer(inv -> {
                SavedCostingEntity saved = inv.getArgument(0);
                assertThat(saved.getCreatedBy()).isEqualTo(userId);
                return entity;
            });

            costingService.create(request, userId);

            verify(repository, times(1)).save(any());
        }
    }

    @Nested
    @DisplayName("update()")
    class UpdateTests {

        @Test
        @DisplayName("Updates costing when owner requests")
        void update_ownerRequest_updatesSuccessfully() throws Exception {
            stubWriteJson();
            stubReadJson();
            when(repository.findById(costingId))
                    .thenReturn(Optional.of(entity));
            when(repository.save(any())).thenReturn(entity);

            SavedCostingResponse result =
                    costingService.update(costingId, request, userId);

            assertThat(result).isNotNull();
            verify(repository, times(1)).save(any());
        }

        @Test
        @DisplayName("Throws ForbiddenException when non-owner updates")
        void update_nonOwner_throwsForbidden() {
            when(repository.findById(costingId))
                    .thenReturn(Optional.of(entity));

            assertThatThrownBy(() ->
                    costingService.update(
                            costingId, request, differentUserId))
                    .isInstanceOf(ForbiddenException.class);
        }

        @Test
        @DisplayName("Throws ResourceNotFoundException when not found")
        void update_nonExistentId_throwsNotFound() {
            when(repository.findById(costingId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    costingService.update(costingId, request, userId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("delete()")
    class DeleteTests {

        @Test
        @DisplayName("Deletes costing when owner requests")
        void delete_ownerRequest_deletesSuccessfully() {
            when(repository.findById(costingId))
                    .thenReturn(Optional.of(entity));

            costingService.delete(costingId, userId);

            verify(repository, times(1)).delete(entity);
        }

        @Test
        @DisplayName("Throws ForbiddenException when non-owner deletes")
        void delete_nonOwner_throwsForbidden() {
            when(repository.findById(costingId))
                    .thenReturn(Optional.of(entity));

            assertThatThrownBy(() ->
                    costingService.delete(costingId, differentUserId))
                    .isInstanceOf(ForbiddenException.class);
        }

        @Test
        @DisplayName("Throws ResourceNotFoundException when not found")
        void delete_nonExistentId_throwsNotFound() {
            when(repository.findById(costingId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    costingService.delete(costingId, userId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
