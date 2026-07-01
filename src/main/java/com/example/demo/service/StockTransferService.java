package com.example.demo.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.demo.Entity.Product;
import com.example.demo.Entity.StockTransfer;
import com.example.demo.Entity.Warehouse;
import com.example.demo.Entity.WarehouseInventory;
import com.example.demo.Repository.StockTransferRepository;
import com.example.demo.Repository.WarehouseInventoryRepository;
import com.example.demo.Repository.WarehouseRepository;
import com.example.demo.dto.request.StockTransferRequest;
import com.example.demo.dto.response.StockTransferResponse;
import com.example.demo.exception.InsufficientStockException;
import com.example.demo.exception.WarehouseNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class StockTransferService {

    private final StockTransferRepository stockTransferRepository;
    private final WarehouseInventoryRepository warehouseInventoryRepository;
    private final WarehouseRepository warehouseRepository;
    private final ProductService productService;

    @Transactional
    public StockTransferResponse transferStock(StockTransferRequest request) {

        log.info("Transfer request received: source={}, target={}, product={}, quantity={}",
                request.getSourceWarehouseId(), request.getTargetWarehouseId(),
                request.getProductId(), request.getQuantity());

        if (request.getSourceWarehouseId().equals(request.getTargetWarehouseId())) {
            log.warn("Transfer FAILED — Source and target warehouse are same, id={}",
                    request.getSourceWarehouseId());
            throw new IllegalArgumentException(
                    "Source and target warehouse cannot be the same");
        }

        Product product = productService.findProductEntity(request.getProductId());

        WarehouseInventory sourceInventory = warehouseInventoryRepository
                .findByWarehouseIdAndProductId(
                        request.getSourceWarehouseId(), request.getProductId())
                .orElseThrow(() -> {
                    log.warn("Transfer FAILED — No inventory for productId={} in source warehouseId={}",
                            request.getProductId(), request.getSourceWarehouseId());
                    return new InsufficientStockException(
                            "No inventory found for this product in source warehouse");
                });

        if (sourceInventory.getAvailableQuantity() < request.getQuantity()) {
            log.warn("Transfer FAILED — Insufficient stock. Source warehouse={}, Available={}, Requested={}",
                    sourceInventory.getWarehouse().getName(),
                    sourceInventory.getAvailableQuantity(), request.getQuantity());
            throw new InsufficientStockException(
                    "Insufficient stock in source warehouse. Available: " +
                    sourceInventory.getAvailableQuantity() +
                    ", Requested: " + request.getQuantity());
        }

        Warehouse targetWarehouse = warehouseRepository
                .findById(request.getTargetWarehouseId())
                .orElseThrow(() -> {
                    log.warn("Transfer FAILED — Target warehouse not found, id={}",
                            request.getTargetWarehouseId());
                    return new WarehouseNotFoundException(
                            "Target warehouse not found with id: " +
                            request.getTargetWarehouseId());
                });

        if (targetWarehouse.getStatus().name().equals("INACTIVE")) {
            log.warn("Transfer FAILED — Target warehouse '{}' is INACTIVE", targetWarehouse.getName());
            throw new IllegalStateException(
                    "Target warehouse '" + targetWarehouse.getName() + "' is inactive");
        }

        WarehouseInventory targetInventory = warehouseInventoryRepository
                .findByWarehouseIdAndProductId(
                        request.getTargetWarehouseId(), request.getProductId())
                .orElse(null);

        int currentTargetStock = (targetInventory != null)
                ? targetInventory.getAvailableQuantity() : 0;

        if (currentTargetStock + request.getQuantity() > targetWarehouse.getCapacity()) {
            log.error("Transfer FAILED — Target warehouse '{}' capacity exceeded. Capacity={}, Current+Incoming={}",
                    targetWarehouse.getName(), targetWarehouse.getCapacity(),
                    currentTargetStock + request.getQuantity());
            throw new IllegalStateException(
                    "Target warehouse capacity exceeded. Capacity: " +
                    targetWarehouse.getCapacity() +
                    ", Current + Incoming: " +
                    (currentTargetStock + request.getQuantity()));
        }

        sourceInventory.setAvailableQuantity(
                sourceInventory.getAvailableQuantity() - request.getQuantity());
        warehouseInventoryRepository.save(sourceInventory);

        if (targetInventory != null) {
            targetInventory.setAvailableQuantity(
                    targetInventory.getAvailableQuantity() + request.getQuantity());
            warehouseInventoryRepository.save(targetInventory);
        } else {
            WarehouseInventory newInventory = WarehouseInventory.builder()
                    .warehouse(targetWarehouse)
                    .product(product)
                    .availableQuantity(request.getQuantity())
                    .build();
            warehouseInventoryRepository.save(newInventory);
        }

        StockTransfer transfer = StockTransfer.builder()
                .sourceWarehouse(sourceInventory.getWarehouse())
                .targetWarehouse(targetWarehouse)
                .product(product)
                .quantity(request.getQuantity())
                .build();

        StockTransfer saved = stockTransferRepository.save(transfer);

        log.info("Transfer SUCCESS — id={}, product={}, {} -> {}, quantity={}",
                saved.getId(), product.getName(),
                sourceInventory.getWarehouse().getName(),
                targetWarehouse.getName(), request.getQuantity());

        return mapToResponse(saved);
    }

    public List<StockTransferResponse> getAllTransfers() {
        return stockTransferRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<StockTransferResponse> getTransfersByWarehouse(Long warehouseId) {
        return stockTransferRepository.findAllTransfersByWarehouseId(warehouseId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private StockTransferResponse mapToResponse(StockTransfer transfer) {
        return StockTransferResponse.builder()
                .id(transfer.getId())
                .sourceWarehouseId(transfer.getSourceWarehouse().getId())
                .sourceWarehouseName(transfer.getSourceWarehouse().getName())
                .targetWarehouseId(transfer.getTargetWarehouse().getId())
                .targetWarehouseName(transfer.getTargetWarehouse().getName())
                .productId(transfer.getProduct().getId())
                .productName(transfer.getProduct().getName())
                .quantity(transfer.getQuantity())
                .transferDate(transfer.getTransferDate())
                .build();
    }

}