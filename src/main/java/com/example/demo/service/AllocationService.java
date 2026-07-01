package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.demo.Entity.Allocation;
import com.example.demo.Entity.AllocationStatus;
import com.example.demo.Entity.Product;
import com.example.demo.Entity.Warehouse;
import com.example.demo.Entity.WarehouseInventory;
import com.example.demo.Repository.AllocationRepository;
import com.example.demo.Repository.WarehouseInventoryRepository;
import com.example.demo.Repository.WarehouseRepository;
import com.example.demo.dto.request.AllocationRequest;
import com.example.demo.dto.response.AllocationResponse;
import com.example.demo.exception.CapacityExceededException;
import com.example.demo.exception.InsufficientStockException;
import com.example.demo.exception.WarehouseNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class AllocationService {

    private final AllocationRepository allocationRepository;
    private final WarehouseInventoryRepository warehouseInventoryRepository;
    private final WarehouseRepository warehouseRepository;
    private final ProductService productService;

    @Transactional
public AllocationResponse allocateStock(AllocationRequest request) {

    log.info("Allocation request received: productId={}, warehouseId={}, quantity={}",
            request.getProductId(), request.getWarehouseId(), request.getQuantity());

    Product product = productService.findProductEntity(request.getProductId());

    WarehouseInventory inventory;

    if (request.getWarehouseId() != null) {
        inventory = findInventory(request.getWarehouseId(), request.getProductId());
    } else {
        inventory = findBestWarehouse(request.getProductId(), request.getQuantity());
    }

    if (inventory.getAvailableQuantity() < request.getQuantity()) {
        log.warn("Allocation FAILED — Insufficient stock. Product={}, Warehouse={}, Available={}, Requested={}",
                product.getName(), inventory.getWarehouse().getName(),
                inventory.getAvailableQuantity(), request.getQuantity());

        throw new InsufficientStockException(
                "Insufficient stock in warehouse: " + inventory.getWarehouse().getName() +
                ". Available: " + inventory.getAvailableQuantity() +
                ", Requested: " + request.getQuantity());
    }

    inventory.setAvailableQuantity(
            inventory.getAvailableQuantity() - request.getQuantity());
    warehouseInventoryRepository.save(inventory);

    Allocation allocation = Allocation.builder()
            .product(product)
            .warehouse(inventory.getWarehouse())
            .quantity(request.getQuantity())
            .status(AllocationStatus.CONFIRMED)
            .build();

    Allocation saved = allocationRepository.save(allocation);

    log.info("Allocation SUCCESS — id={}, product={}, warehouse={}, quantity={}",
            saved.getId(), product.getName(), inventory.getWarehouse().getName(),
            request.getQuantity());

    return mapToResponse(saved);
}

  private WarehouseInventory findInventory(Long warehouseId, Long productId) {

    Warehouse warehouse = warehouseRepository.findById(warehouseId)
            .orElseThrow(() -> {
                log.warn("Allocation FAILED — Warehouse not found, id={}", warehouseId);
                return new WarehouseNotFoundException(
                        "Warehouse not found with id: " + warehouseId);
            });

    if (warehouse.getStatus().name().equals("INACTIVE")) {
        log.warn("Allocation FAILED — Warehouse '{}' is INACTIVE", warehouse.getName());
        throw new IllegalStateException(
                "Warehouse '" + warehouse.getName() + "' is inactive");
    }

    WarehouseInventory inventory = warehouseInventoryRepository
            .findByWarehouseIdAndProductId(warehouseId, productId)
            .orElseThrow(() -> {
                log.warn("Allocation FAILED — No inventory for productId={} in warehouseId={}",
                        productId, warehouseId);
                return new InsufficientStockException(
                        "No inventory found for this product in the selected warehouse");
            });

    Integer totalStock = warehouseInventoryRepository
            .getTotalStockInWarehouse(warehouseId);

    if (totalStock != null && totalStock > warehouse.getCapacity()) {
        log.error("Allocation FAILED — Warehouse '{}' exceeded capacity. Capacity={}, CurrentStock={}",
                warehouse.getName(), warehouse.getCapacity(), totalStock);
        throw new CapacityExceededException(
                "Warehouse '" + warehouse.getName() +
                "' has exceeded its capacity. This allocation cannot proceed.");
    }

    return inventory;
}

    private WarehouseInventory findBestWarehouse(Long productId, Integer quantity) {

        List<WarehouseInventory> availableWarehouses =
                warehouseInventoryRepository
                        .findAvailableWarehousesForProduct(productId, quantity);

        if (availableWarehouses.isEmpty()) {
            throw new InsufficientStockException(
                    "No warehouse has sufficient stock for this product");
        }

        return availableWarehouses.get(0);
    }

    public List<AllocationResponse> getAllAllocations() {
        return allocationRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public Page<AllocationResponse> getAllAllocationsPaged(Pageable pageable) {
        return allocationRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    public List<AllocationResponse> getAllocationsByProduct(Long productId) {
        return allocationRepository.findByProductId(productId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<AllocationResponse> getAllocationsByWarehouse(Long warehouseId) {
        return allocationRepository.findByWarehouseId(warehouseId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public Page<AllocationResponse> searchAllocations(
            Long productId,
            Long warehouseId,
            AllocationStatus status,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable) {

        LocalDateTime effectiveStart = (startDate != null) ? startDate : LocalDateTime.of(2000, 1, 1, 0, 0);
        LocalDateTime effectiveEnd = (endDate != null) ? endDate : LocalDateTime.now();

        Pageable safePage = pageable.getSort().isSorted() &&
                !pageable.getSort().toString().contains("string")
                ? pageable
                : org.springframework.data.domain.PageRequest.of(
                        pageable.getPageNumber(),
                        pageable.getPageSize(),
                        org.springframework.data.domain.Sort.by("allocatedAt").descending());

        return allocationRepository.searchAllocations(
                        productId, warehouseId, status, effectiveStart, effectiveEnd, safePage)
                .map(this::mapToResponse);
    }

    private AllocationResponse mapToResponse(Allocation allocation) {
        return AllocationResponse.builder()
                .id(allocation.getId())
                .productId(allocation.getProduct().getId())
                .productName(allocation.getProduct().getName())
                .warehouseId(allocation.getWarehouse().getId())
                .warehouseName(allocation.getWarehouse().getName())
                .quantity(allocation.getQuantity())
                .allocatedAt(allocation.getAllocatedAt())
                .status(allocation.getStatus())
                .build();
    }

}