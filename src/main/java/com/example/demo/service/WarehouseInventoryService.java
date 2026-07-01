package com.example.demo.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.demo.Entity.Product;
import com.example.demo.Entity.Warehouse;
import com.example.demo.Entity.WarehouseInventory;
import com.example.demo.Repository.WarehouseInventoryRepository;
import com.example.demo.Repository.WarehouseRepository;
import com.example.demo.exception.CapacityExceededException;
import com.example.demo.exception.WarehouseNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WarehouseInventoryService {

    private final WarehouseInventoryRepository warehouseInventoryRepository;
    private final WarehouseRepository warehouseRepository;
    private final ProductService productService;

    @Transactional
    public WarehouseInventory addStock(Long warehouseId, Long productId, Integer quantity) {

        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new WarehouseNotFoundException(
                        "Warehouse not found with id: " + warehouseId));

        Product product = productService.findProductEntity(productId);

        Integer currentTotalStock = warehouseInventoryRepository
                .getTotalStockInWarehouse(warehouseId);

        if (currentTotalStock == null) {
            currentTotalStock = 0;
        }

        if (currentTotalStock + quantity > warehouse.getCapacity()) {
            throw new CapacityExceededException(
                    "Cannot add stock. Warehouse '" + warehouse.getName() +
                    "' capacity: " + warehouse.getCapacity() +
                    ", Current stock: " + currentTotalStock +
                    ", Trying to add: " + quantity);
        }

        WarehouseInventory inventory = warehouseInventoryRepository
                .findByWarehouseIdAndProductId(warehouseId, productId)
                .orElse(null);

        if (inventory != null) {
            inventory.setAvailableQuantity(inventory.getAvailableQuantity() + quantity);
        } else {
            inventory = WarehouseInventory.builder()
                    .warehouse(warehouse)
                    .product(product)
                    .availableQuantity(quantity)
                    .build();
        }

        return warehouseInventoryRepository.save(inventory);
    }

}