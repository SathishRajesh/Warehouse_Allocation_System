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

import com.example.demo.Entity.Warehouse;
import com.example.demo.Entity.WarehouseStatus;
import com.example.demo.Repository.WarehouseRepository;
import com.example.demo.dto.request.WarehouseRequest;
import com.example.demo.dto.response.WarehouseResponse;
import com.example.demo.exception.WarehouseNotFoundException;

@ExtendWith(MockitoExtension.class)
class WarehouseServiceTest {

    @Mock
    private WarehouseRepository warehouseRepository;

    @InjectMocks
    private WarehouseService warehouseService;

    private Warehouse warehouse;
    private WarehouseRequest request;

    @BeforeEach
    void setUp() {
        warehouse = Warehouse.builder()
                .id(1L)
                .name("Warehouse A")
                .location("Chennai")
                .capacity(1000)
                .status(WarehouseStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();

        request = WarehouseRequest.builder()
                .name("Warehouse A")
                .location("Chennai")
                .capacity(1000)
                .build();
    }

    @Test
    void createWarehouse_Success() {
        when(warehouseRepository.save(any(Warehouse.class))).thenReturn(warehouse);

        WarehouseResponse response = warehouseService.createWarehouse(request);

        assertNotNull(response);
        assertEquals("Warehouse A", response.getName());
        assertEquals("Chennai", response.getLocation());
        assertEquals(1000, response.getCapacity());
        assertEquals(WarehouseStatus.ACTIVE, response.getStatus());
        verify(warehouseRepository, times(1)).save(any(Warehouse.class));
    }

    @Test
    void getWarehouseById_Success() {
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));

        WarehouseResponse response = warehouseService.getWarehouseById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Warehouse A", response.getName());
    }

    @Test
    void getWarehouseById_NotFound() {
        when(warehouseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(WarehouseNotFoundException.class, () ->
                warehouseService.getWarehouseById(99L));
    }

    @Test
    void getAllWarehouses_Success() {
        when(warehouseRepository.findAll()).thenReturn(List.of(warehouse));

        List<WarehouseResponse> responses = warehouseService.getAllWarehouses();

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("Warehouse A", responses.get(0).getName());
    }

    @Test
    void getActiveWarehouses_Success() {
        when(warehouseRepository.findByStatus(WarehouseStatus.ACTIVE))
                .thenReturn(List.of(warehouse));

        List<WarehouseResponse> responses = warehouseService.getActiveWarehouses();

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(WarehouseStatus.ACTIVE, responses.get(0).getStatus());
    }

    @Test
    void updateWarehouse_Success() {
        WarehouseRequest updateRequest = WarehouseRequest.builder()
                .name("Warehouse A Updated")
                .location("Bangalore")
                .capacity(2000)
                .build();

        Warehouse updatedWarehouse = Warehouse.builder()
                .id(1L)
                .name("Warehouse A Updated")
                .location("Bangalore")
                .capacity(2000)
                .status(WarehouseStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();

        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));
        when(warehouseRepository.save(any(Warehouse.class))).thenReturn(updatedWarehouse);

        WarehouseResponse response = warehouseService.updateWarehouse(1L, updateRequest);

        assertNotNull(response);
        assertEquals("Warehouse A Updated", response.getName());
        assertEquals("Bangalore", response.getLocation());
        assertEquals(2000, response.getCapacity());
    }

    @Test
    void updateWarehouse_NotFound() {
        when(warehouseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(WarehouseNotFoundException.class, () ->
                warehouseService.updateWarehouse(99L, request));
    }

    @Test
    void activateWarehouse_Success() {
        warehouse.setStatus(WarehouseStatus.INACTIVE);

        Warehouse activatedWarehouse = Warehouse.builder()
                .id(1L)
                .name("Warehouse A")
                .location("Chennai")
                .capacity(1000)
                .status(WarehouseStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();

        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));
        when(warehouseRepository.save(any(Warehouse.class))).thenReturn(activatedWarehouse);

        WarehouseResponse response = warehouseService.activateWarehouse(1L);

        assertEquals(WarehouseStatus.ACTIVE, response.getStatus());
    }

    @Test
    void deactivateWarehouse_Success() {
        Warehouse deactivatedWarehouse = Warehouse.builder()
                .id(1L)
                .name("Warehouse A")
                .location("Chennai")
                .capacity(1000)
                .status(WarehouseStatus.INACTIVE)
                .createdAt(LocalDateTime.now())
                .build();

        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));
        when(warehouseRepository.save(any(Warehouse.class))).thenReturn(deactivatedWarehouse);

        WarehouseResponse response = warehouseService.deactivateWarehouse(1L);

        assertEquals(WarehouseStatus.INACTIVE, response.getStatus());
    }

    @Test
    void activateWarehouse_NotFound() {
        when(warehouseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(WarehouseNotFoundException.class, () ->
                warehouseService.activateWarehouse(99L));
    }

    @Test
    void deactivateWarehouse_NotFound() {
        when(warehouseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(WarehouseNotFoundException.class, () ->
                warehouseService.deactivateWarehouse(99L));
    }

}