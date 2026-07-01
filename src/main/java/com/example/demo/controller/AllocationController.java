package com.example.demo.controller;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.demo.Entity.AllocationStatus;
import com.example.demo.service.AllocationService;
import com.example.demo.dto.request.AllocationRequest;
import com.example.demo.dto.response.AllocationResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/allocations")
@RequiredArgsConstructor
public class AllocationController {

    private final AllocationService allocationService;

    @PostMapping
    public ResponseEntity<AllocationResponse> allocateStock(
            @Valid @RequestBody AllocationRequest request) {
        return ResponseEntity.ok(allocationService.allocateStock(request));
    }

    @GetMapping
    public ResponseEntity<List<AllocationResponse>> getAllAllocations() {
        return ResponseEntity.ok(allocationService.getAllAllocations());
    }

    @GetMapping("/paged")
    public ResponseEntity<Page<AllocationResponse>> getAllAllocationsPaged(
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(allocationService.getAllAllocationsPaged(pageable));
    }	

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<AllocationResponse>> getByProduct(
            @PathVariable Long productId) {
        return ResponseEntity.ok(allocationService.getAllocationsByProduct(productId));
    }

    @GetMapping("/warehouse/{warehouseId}")
    public ResponseEntity<List<AllocationResponse>> getByWarehouse(
            @PathVariable Long warehouseId) {
        return ResponseEntity.ok(allocationService.getAllocationsByWarehouse(warehouseId));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<AllocationResponse>> searchAllocations(
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) AllocationStatus status,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(allocationService.searchAllocations(
                productId, warehouseId, status, startDate, endDate, pageable));
    }

}