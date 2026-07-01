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

import com.example.demo.Entity.Product;
import com.example.demo.Entity.StockTransfer;
import com.example.demo.Entity.Warehouse;
import com.example.demo.Entity.WarehouseInventory;
import com.example.demo.Entity.WarehouseStatus;
import com.example.demo.Repository.StockTransferRepository;
import com.example.demo.Repository.WarehouseInventoryRepository;
import com.example.demo.Repository.WarehouseRepository;
import com.example.demo.dto.request.StockTransferRequest;
import com.example.demo.dto.response.StockTransferResponse;
import com.example.demo.exception.InsufficientStockException;
import com.example.demo.exception.WarehouseNotFoundException;

@ExtendWith(MockitoExtension.class)
class StockTransferServiceTest {

    @Mock
    private StockTransferRepository stockTransferRepository;

    @Mock
    private WarehouseInventoryRepository warehouseInventoryRepository;

    @Mock
    private WarehouseRepository warehouseRepository;

    @Mock
    private ProductService productService;

    @InjectMocks
    private StockTransferService stockTransferService;

    private Product product;
    private Warehouse sourceWarehouse;
    private Warehouse targetWarehouse;
    private WarehouseInventory sourceInventory;
    private WarehouseInventory targetInventory;
    private StockTransfer stockTransfer;
    private StockTransferRequest request;

    @BeforeEach
    void setUp() {
        product = Product.builder()
                .id(1L)
                .name("iPhone 15")
                .sku("APPL-IPH-15")
                .totalStock(500)
                .build();

        sourceWarehouse = Warehouse.builder()
                .id(1L)
                .name("Warehouse A")
                .location("Chennai")
                .capacity(1000)
                .status(WarehouseStatus.ACTIVE)
                .build();

        targetWarehouse = Warehouse.builder()
                .id(2L)
                .name("Warehouse B")
                .location("Bangalore")
                .capacity(500)
                .status(WarehouseStatus.ACTIVE)
                .build();

        sourceInventory = WarehouseInventory.builder()
                .id(1L)
                .warehouse(sourceWarehouse)
                .product(product)
                .availableQuantity(200)
                .build();

        targetInventory = WarehouseInventory.builder()
                .id(2L)
                .warehouse(targetWarehouse)
                .product(product)
                .availableQuantity(50)
                .build();

        stockTransfer = StockTransfer.builder()
                .id(1L)
                .sourceWarehouse(sourceWarehouse)
                .targetWarehouse(targetWarehouse)
                .product(product)
                .quantity(20)
                .transferDate(LocalDateTime.now())
                .build();

        request = StockTransferRequest.builder()
                .sourceWarehouseId(1L)
                .targetWarehouseId(2L)
                .productId(1L)
                .quantity(20)
                .build();
    }

    @Test
    void transferStock_Success_ExistingTargetInventory() {
        when(productService.findProductEntity(1L)).thenReturn(product);
        when(warehouseInventoryRepository.findByWarehouseIdAndProductId(1L, 1L))
                .thenReturn(Optional.of(sourceInventory));
        when(warehouseRepository.findById(2L)).thenReturn(Optional.of(targetWarehouse));
        when(warehouseInventoryRepository.findByWarehouseIdAndProductId(2L, 1L))
                .thenReturn(Optional.of(targetInventory));
        when(warehouseInventoryRepository.save(any(WarehouseInventory.class)))
                .thenReturn(sourceInventory);
        when(stockTransferRepository.save(any(StockTransfer.class)))
                .thenReturn(stockTransfer);

        StockTransferResponse response = stockTransferService.transferStock(request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Warehouse A", response.getSourceWarehouseName());
        assertEquals("Warehouse B", response.getTargetWarehouseName());
        assertEquals("iPhone 15", response.getProductName());
        assertEquals(20, response.getQuantity());
        verify(stockTransferRepository, times(1)).save(any(StockTransfer.class));
    }

    @Test
    void transferStock_Success_NewTargetInventory() {
        when(productService.findProductEntity(1L)).thenReturn(product);
        when(warehouseInventoryRepository.findByWarehouseIdAndProductId(1L, 1L))
                .thenReturn(Optional.of(sourceInventory));
        when(warehouseRepository.findById(2L)).thenReturn(Optional.of(targetWarehouse));
        when(warehouseInventoryRepository.findByWarehouseIdAndProductId(2L, 1L))
                .thenReturn(Optional.empty());
        when(warehouseInventoryRepository.save(any(WarehouseInventory.class)))
                .thenReturn(sourceInventory);
        when(stockTransferRepository.save(any(StockTransfer.class)))
                .thenReturn(stockTransfer);

        StockTransferResponse response = stockTransferService.transferStock(request);

        assertNotNull(response);
        assertEquals("Warehouse A", response.getSourceWarehouseName());
        assertEquals("Warehouse B", response.getTargetWarehouseName());
        verify(warehouseInventoryRepository, times(2))
                .save(any(WarehouseInventory.class));
    }

    @Test
    void transferStock_SameWarehouse() {
        StockTransferRequest sameRequest = StockTransferRequest.builder()
                .sourceWarehouseId(1L)
                .targetWarehouseId(1L)
                .productId(1L)
                .quantity(20)
                .build();

        assertThrows(IllegalArgumentException.class, () ->
                stockTransferService.transferStock(sameRequest));

        verify(stockTransferRepository, never()).save(any(StockTransfer.class));
    }

    @Test
    void transferStock_SourceInventoryNotFound() {
        when(productService.findProductEntity(1L)).thenReturn(product);
        when(warehouseInventoryRepository.findByWarehouseIdAndProductId(1L, 1L))
                .thenReturn(Optional.empty());

        assertThrows(InsufficientStockException.class, () ->
                stockTransferService.transferStock(request));

        verify(stockTransferRepository, never()).save(any(StockTransfer.class));
    }

    // ❌ TRANSFER — Insufficient Stock in Source
    @Test
    void transferStock_InsufficientStock() {
        StockTransferRequest bigRequest = StockTransferRequest.builder()
                .sourceWarehouseId(1L)
                .targetWarehouseId(2L)
                .productId(1L)
                .quantity(99999)
                .build();

        when(productService.findProductEntity(1L)).thenReturn(product);
        when(warehouseInventoryRepository.findByWarehouseIdAndProductId(1L, 1L))
                .thenReturn(Optional.of(sourceInventory));

        assertThrows(InsufficientStockException.class, () ->
                stockTransferService.transferStock(bigRequest));

        verify(stockTransferRepository, never()).save(any(StockTransfer.class));
    }

    @Test
    void transferStock_TargetWarehouseNotFound() {
        when(productService.findProductEntity(1L)).thenReturn(product);
        when(warehouseInventoryRepository.findByWarehouseIdAndProductId(1L, 1L))
                .thenReturn(Optional.of(sourceInventory));
        when(warehouseRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(WarehouseNotFoundException.class, () ->
                stockTransferService.transferStock(request));

        verify(stockTransferRepository, never()).save(any(StockTransfer.class));
    }

    @Test
    void transferStock_TargetWarehouseInactive() {
        targetWarehouse.setStatus(WarehouseStatus.INACTIVE);

        when(productService.findProductEntity(1L)).thenReturn(product);
        when(warehouseInventoryRepository.findByWarehouseIdAndProductId(1L, 1L))
                .thenReturn(Optional.of(sourceInventory));
        when(warehouseRepository.findById(2L)).thenReturn(Optional.of(targetWarehouse));

        assertThrows(IllegalStateException.class, () ->
                stockTransferService.transferStock(request));

        verify(stockTransferRepository, never()).save(any(StockTransfer.class));
    }

    @Test
    void transferStock_TargetCapacityExceeded() {
        targetInventory.setAvailableQuantity(490);

        when(productService.findProductEntity(1L)).thenReturn(product);
        when(warehouseInventoryRepository.findByWarehouseIdAndProductId(1L, 1L))
                .thenReturn(Optional.of(sourceInventory));
        when(warehouseRepository.findById(2L)).thenReturn(Optional.of(targetWarehouse));
        when(warehouseInventoryRepository.findByWarehouseIdAndProductId(2L, 1L))
                .thenReturn(Optional.of(targetInventory));

        assertThrows(IllegalStateException.class, () ->
                stockTransferService.transferStock(request));

        verify(stockTransferRepository, never()).save(any(StockTransfer.class));
    }

    @Test
    void getAllTransfers_Success() {
        when(stockTransferRepository.findAll()).thenReturn(List.of(stockTransfer));

        List<StockTransferResponse> responses = stockTransferService.getAllTransfers();

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("Warehouse A", responses.get(0).getSourceWarehouseName());
        assertEquals("Warehouse B", responses.get(0).getTargetWarehouseName());
    }

    @Test
    void getTransfersByWarehouse_Success() {
        when(stockTransferRepository.findAllTransfersByWarehouseId(1L))
                .thenReturn(List.of(stockTransfer));

        List<StockTransferResponse> responses =
                stockTransferService.getTransfersByWarehouse(1L);

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("iPhone 15", responses.get(0).getProductName());
    }

}