package com.example.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.demo.Entity.WarehouseInventory;
import com.example.demo.service.WarehouseInventoryService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class WarehouseInventoryController {

    private final WarehouseInventoryService warehouseInventoryService;

    @PostMapping("/add-stock")
    public ResponseEntity<WarehouseInventory> addStock(
            @RequestParam Long warehouseId,
            @RequestParam Long productId,
            @RequestParam Integer quantity) {

        return ResponseEntity.ok(
                warehouseInventoryService.addStock(warehouseId, productId, quantity));
    }

}