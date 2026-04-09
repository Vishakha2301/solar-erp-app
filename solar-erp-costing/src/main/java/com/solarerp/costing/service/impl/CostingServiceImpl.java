package com.solarerp.costing.service.impl;

import tools.jackson.databind.ObjectMapper;
import com.solarerp.costing.dto.CostingContextDto;
import com.solarerp.costing.dto.CostingSnapshotDto;
import com.solarerp.costing.dto.SavedCostingRequest;
import com.solarerp.costing.dto.SavedCostingResponse;
import com.solarerp.costing.entity.SavedCostingEntity;
import com.solarerp.costing.repository.CostingRepository;
import com.solarerp.costing.service.CostingService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class CostingServiceImpl implements CostingService {

    private final CostingRepository repository;
    private final ObjectMapper objectMapper;

    public CostingServiceImpl(CostingRepository repository,
                               ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<SavedCostingResponse> getAll() {
        return repository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public SavedCostingResponse getById(UUID id) {
        return toResponse(findOrThrow(id));
    }

    @Override
    public SavedCostingResponse create(SavedCostingRequest request,
                                        UUID userId) {
        SavedCostingEntity entity = new SavedCostingEntity();
        entity.setCreatedBy(userId);
        entity.setContext(toJson(request.context()));
        entity.setSnapshot(toJson(request.snapshot()));
        return toResponse(repository.save(entity));
    }

    @Override
    public SavedCostingResponse update(UUID id, SavedCostingRequest request,
                                        UUID userId) {
        SavedCostingEntity entity = findOrThrow(id);
        checkOwnership(entity, userId);
        entity.setContext(toJson(request.context()));
        entity.setSnapshot(toJson(request.snapshot()));
        return toResponse(repository.save(entity));
    }

    @Override
    public void delete(UUID id, UUID userId) {
        SavedCostingEntity entity = findOrThrow(id);
        checkOwnership(entity, userId);
        repository.delete(entity);
    }

    private SavedCostingEntity findOrThrow(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Costing not found: " + id));
    }

    private void checkOwnership(SavedCostingEntity entity, UUID userId) {
        if (!entity.getCreatedBy().equals(userId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You do not have permission to modify this costing");
        }
    }

    private SavedCostingResponse toResponse(SavedCostingEntity entity) {
        return new SavedCostingResponse(
                entity.getId(),
                entity.getCreatedAt(),
                entity.getCreatedBy(),
                fromJson(entity.getContext(), CostingContextDto.class),
                fromJson(entity.getSnapshot(), CostingSnapshotDto.class)
        );
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Invalid JSON in request");
        }
    }

    private <T> T fromJson(String json, Class<T> type) {
        try {
            return objectMapper.readValue(json, type);
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Corrupt stored JSON: " + e.getMessage());
        }
    }
}
