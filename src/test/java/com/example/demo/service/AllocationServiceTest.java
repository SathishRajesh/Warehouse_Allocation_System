package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.example.demo.Entity.Allocation;
import com.example.demo.Entity.AllocationStatus;
import com.example.demo.Entity.Product;
import com.example.demo.Entity.Warehouse;
import com.example.demo.Entity.WarehouseInventory;
import com.example.demo.Entity.WarehouseStatus;
import com.example.demo.Repository.AllocationRepository;
import com.example.demo.Repository.WarehouseInventoryRepository;
import com.example.demo.Repository.WarehouseRepository;
import com.example.demo.dto.request.AllocationRequest;
import com.example.demo.dto.response.AllocationResponse;
import com.example.demo.exception.CapacityExceededException;
import com.example.demo.exception.InsufficientStockException;
import com.example.demo.exception.WarehouseNotFoundException;

@ExtendWith(MockitoExtension.class)
class AllocationServiceTest {

    @Mock
    private AllocationRepository allocationRepository;

    @Mock
    private WarehouseInventoryRepository warehouseInventoryRepository;

    @Mock
    private WarehouseRepository warehouseRepository;

    @Mock
    private ProductService productService;

    @InjectMocks
    private AllocationService allocationService;

    private Product product;
    private Warehouse warehouse;
    private WarehouseInventory inventory;
    private Allocation allocation;
    private AllocationRequest request;

    @BeforeEach
    void setUp() {
        product = Product.builder()
                .id(1L)
                .name("iPhone 15")
                .sku("APPL-IPH-15")
                .totalStock(500)
                .build();

        warehouse = Warehouse.builder()
                .id(1L)
                .name("Warehouse A")
                .location("Chennai")
                .capacity(1000)
                .status(WarehouseStatus.ACTIVE)
                .build();

        inventory = WarehouseInventory.builder()
                .id(1L)
                .warehouse(warehouse)
                .product(product)
                .availableQuantity(200)
                .build();

        allocation = Allocation.builder()
                .id(1L)
                .product(product)
                .warehouse(warehouse)
                .quantity(50)
                .status(AllocationStatus.CONFIRMED)
                .allocatedAt(LocalDateTime.now())
                .build();

        request = AllocationRequest.builder()
                .productId(1L)
                .warehouseId(1L)
                .quantity(50)
                .build();
    }

    @Test
    void allocateStock_SpecificWarehouse_Success() {
        when(productService.findProductEntity(1L)).thenReturn(product);
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));
        when(warehouseInventoryRepository.findByWarehouseIdAndProductId(1L, 1L))
                .thenReturn(Optional.of(inventory));
        when(warehouseInventoryRepository.getTotalStockInWarehouse(1L)).thenReturn(200);
        when(warehouseInventoryRepository.save(any(WarehouseInventory.class)))
                .thenReturn(inventory);
        when(allocationRepository.save(any(Allocation.class))).thenReturn(allocation);

        AllocationResponse response = allocationService.allocateStock(request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("iPhone 15", response.getProductName());
        assertEquals("Warehouse A", response.getWarehouseName());
        assertEquals(50, response.getQuantity());
        assertEquals(AllocationStatus.CONFIRMED, response.getStatus());
        verify(allocationRepository, times(1)).save(any(Allocation.class));
    }

    @Test
    void allocateStock_AutoWarehouse_Success() {
        AllocationRequest autoRequest = AllocationRequest.builder()
                .productId(1L)
                .warehouseId(null)
                .quantity(50)
                .build();

        when(productService.findProductEntity(1L)).thenReturn(product);
        when(warehouseInventoryRepository.findAvailableWarehousesForProduct(1L, 50))
                .thenReturn(List.of(inventory));
        when(warehouseInventoryRepository.save(any(WarehouseInventory.class)))
                .thenReturn(inventory);
        when(allocationRepository.save(any(Allocation.class))).thenReturn(allocation);

        AllocationResponse response = allocationService.allocateStock(autoRequest);

        assertNotNull(response);
        assertEquals("Warehouse A", response.getWarehouseName());
        assertEquals(AllocationStatus.CONFIRMED, response.getStatus());
    }

    @Test
    void allocateStock_InsufficientStock() {
        AllocationRequest bigRequest = AllocationRequest.builder()
                .productId(1L)
                .warehouseId(1L)
                .quantity(99999)
                .build();

        when(productService.findProductEntity(1L)).thenReturn(product);
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));
        when(warehouseInventoryRepository.findByWarehouseIdAndProductId(1L, 1L))
                .thenReturn(Optional.of(inventory));
        when(warehouseInventoryRepository.getTotalStockInWarehouse(1L)).thenReturn(200);

        assertThrows(InsufficientStockException.class, () ->
                allocationService.allocateStock(bigRequest));

        verify(allocationRepository, never()).save(any(Allocation.class));
    }

    @Test
    void allocateStock_WarehouseNotFound() {
        when(productService.findProductEntity(1L)).thenReturn(product);
        when(warehouseRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(WarehouseNotFoundException.class, () ->
                allocationService.allocateStock(request));

        verify(allocationRepository, never()).save(any(Allocation.class));
    }

    @Test
    void allocateStock_WarehouseInactive() {
        warehouse.setStatus(WarehouseStatus.INACTIVE);

        when(productService.findProductEntity(1L)).thenReturn(product);
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));

        assertThrows(IllegalStateException.class, () ->
                allocationService.allocateStock(request));

        verify(allocationRepository, never()).save(any(Allocation.class));
    }

    @Test
    void allocateStock_CapacityExceeded() {
        when(productService.findProductEntity(1L)).thenReturn(product);
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));
        when(warehouseInventoryRepository.findByWarehouseIdAndProductId(1L, 1L))
                .thenReturn(Optional.of(inventory));
        when(warehouseInventoryRepository.getTotalStockInWarehouse(1L))
                .thenReturn(9999);

        assertThrows(CapacityExceededException.class, () ->
                allocationService.allocateStock(request));

        verify(allocationRepository, never()).save(any(Allocation.class));
    }

    @Test
    void allocateStock_NoAvailableWarehouse() {
        AllocationRequest autoRequest = AllocationRequest.builder()
                .productId(1L)
                .warehouseId(null)
                .quantity(50)
                .build();

        when(productService.findProductEntity(1L)).thenReturn(product);
        when(warehouseInventoryRepository.findAvailableWarehousesForProduct(1L, 50))
                .thenReturn(List.of());

        assertThrows(InsufficientStockException.class, () ->
                allocationService.allocateStock(autoRequest));
    }

    @Test
    void getAllAllocations_Success() {
        when(allocationRepository.findAll()).thenReturn(List.of(allocation));

        List<AllocationResponse> responses = allocationService.getAllAllocations();

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("iPhone 15", responses.get(0).getProductName());
    }

    @Test
    void getAllAllocationsPaged_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Allocation> page = new PageImpl<>(List.of(allocation));

        when(allocationRepository.findAll(pageable)).thenReturn(page);

        Page<AllocationResponse> responses = allocationService.getAllAllocationsPaged(pageable);

        assertNotNull(responses);
        assertEquals(1, responses.getTotalElements());
    }

    @Test
    void getAllocationsByProduct_Success() {
        when(allocationRepository.findByProductId(1L)).thenReturn(List.of(allocation));

        List<AllocationResponse> responses = allocationService.getAllocationsByProduct(1L);

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("iPhone 15", responses.get(0).getProductName());
    }

    @Test
    void getAllocationsByWarehouse_Success() {
        when(allocationRepository.findByWarehouseId(1L)).thenReturn(List.of(allocation));

        List<AllocationResponse> responses = allocationService.getAllocationsByWarehouse(1L);

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("Warehouse A", responses.get(0).getWarehouseName());
    }

    @Test
    void searchAllocations_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Allocation> page = new PageImpl<>(List.of(allocation));

        when(allocationRepository.searchAllocations(
                any(), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(page);

        Page<AllocationResponse> responses = allocationService.searchAllocations(
                1L, 1L, AllocationStatus.CONFIRMED,
                LocalDateTime.of(2024, 1, 1, 0, 0),
                LocalDateTime.now(), pageable);

        assertNotNull(responses);
        assertEquals(1, responses.getTotalElements());
        assertEquals("iPhone 15", responses.getContent().get(0).getProductName());
    }

}