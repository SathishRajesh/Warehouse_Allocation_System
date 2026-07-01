package com.example.demo.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.demo.Entity.Warehouse;
import com.example.demo.Entity.WarehouseStatus;
import com.example.demo.Repository.WarehouseRepository;
import com.example.demo.dto.request.WarehouseRequest;
import com.example.demo.dto.response.WarehouseResponse;
import com.example.demo.exception.WarehouseNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class WarehouseService {

    private final WarehouseRepository warehouseRepository;

    @Transactional
    public WarehouseResponse createWarehouse(WarehouseRequest request) {
        Warehouse warehouse = Warehouse.builder()
                .name(request.getName())
                .location(request.getLocation())
                .capacity(request.getCapacity())
                .status(WarehouseStatus.ACTIVE)
                .build();

        Warehouse saved = warehouseRepository.save(warehouse);

        log.info("Warehouse CREATED — id={}, name={}, capacity={}",
                saved.getId(), saved.getName(), saved.getCapacity());

        return mapToResponse(saved);
    }

    public WarehouseResponse getWarehouseById(Long id) {
        Warehouse warehouse = findWarehouseEntity(id);
        return mapToResponse(warehouse);
    }

    public List<WarehouseResponse> getAllWarehouses() {
        return warehouseRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<WarehouseResponse> getActiveWarehouses() {
        return warehouseRepository.findByStatus(WarehouseStatus.ACTIVE)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public WarehouseResponse updateWarehouse(Long id, WarehouseRequest request) {
        Warehouse warehouse = findWarehouseEntity(id);

        warehouse.setName(request.getName());
        warehouse.setLocation(request.getLocation());
        warehouse.setCapacity(request.getCapacity());

        Warehouse updated = warehouseRepository.save(warehouse);

        log.info("Warehouse UPDATED — id={}, name={}, capacity={}",
                updated.getId(), updated.getName(), updated.getCapacity());

        return mapToResponse(updated);
    }

    @Transactional
    public WarehouseResponse activateWarehouse(Long id) {
        Warehouse warehouse = findWarehouseEntity(id);
        warehouse.setStatus(WarehouseStatus.ACTIVE);
        Warehouse saved = warehouseRepository.save(warehouse);

        log.info("Warehouse ACTIVATED — id={}, name={}", saved.getId(), saved.getName());

        return mapToResponse(saved);
    }

    @Transactional
    public WarehouseResponse deactivateWarehouse(Long id) {
        Warehouse warehouse = findWarehouseEntity(id);
        warehouse.setStatus(WarehouseStatus.INACTIVE);
        Warehouse saved = warehouseRepository.save(warehouse);

        log.info("Warehouse DEACTIVATED — id={}, name={}", saved.getId(), saved.getName());

        return mapToResponse(saved);
    }

    private Warehouse findWarehouseEntity(Long id) {
        return warehouseRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Warehouse NOT FOUND — id={}", id);
                    return new WarehouseNotFoundException(
                            "Warehouse not found with id: " + id);
                });
    }

    private WarehouseResponse mapToResponse(Warehouse warehouse) {
        return WarehouseResponse.builder()
                .id(warehouse.getId())
                .name(warehouse.getName())
                .location(warehouse.getLocation())
                .capacity(warehouse.getCapacity())
                .status(warehouse.getStatus())
                .createdAt(warehouse.getCreatedAt())
                .build();
    }

}