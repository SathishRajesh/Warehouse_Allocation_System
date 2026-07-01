package com.example.demo.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.demo.service.StockTransferService;
import com.example.demo.dto.request.StockTransferRequest;
import com.example.demo.dto.response.StockTransferResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/transfers")
@RequiredArgsConstructor
public class StockTransferController {

    private final StockTransferService stockTransferService;

    @PostMapping
    public ResponseEntity<StockTransferResponse> transferStock(
            @Valid @RequestBody StockTransferRequest request) {
        return ResponseEntity.ok(stockTransferService.transferStock(request));
    }

    @GetMapping
    public ResponseEntity<List<StockTransferResponse>> getAllTransfers() {
        return ResponseEntity.ok(stockTransferService.getAllTransfers());
    }

    @GetMapping("/warehouse/{warehouseId}")
    public ResponseEntity<List<StockTransferResponse>> getByWarehouse(
            @PathVariable Long warehouseId) {
        return ResponseEntity.ok(stockTransferService.getTransfersByWarehouse(warehouseId));
    }

}