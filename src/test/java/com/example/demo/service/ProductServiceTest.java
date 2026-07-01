package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.demo.Entity.Product;
import com.example.demo.Repository.ProductRepository;
import com.example.demo.dto.request.ProductRequest;
import com.example.demo.dto.response.ProductResponse;
import com.example.demo.exception.ProductNotFoundException;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Product product;
    private ProductRequest request;

    @BeforeEach
    void setUp() {
        product = Product.builder()
                .id(1L)
                .name("iPhone 15")
                .sku("APPL-IPH-15")
                .totalStock(500)
                .build();

        request = ProductRequest.builder()
                .name("iPhone 15")
                .sku("APPL-IPH-15")
                .totalStock(500)
                .build();
    }

    @Test
    void createProduct_Success() {
        when(productRepository.existsBySku("APPL-IPH-15")).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenReturn(product);

        ProductResponse response = productService.createProduct(request);

        assertNotNull(response);
        assertEquals("iPhone 15", response.getName());
        assertEquals("APPL-IPH-15", response.getSku());
        assertEquals(500, response.getTotalStock());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void createProduct_DuplicateSku() {
        when(productRepository.existsBySku("APPL-IPH-15")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () ->
                productService.createProduct(request));

        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void getProductById_Success() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        ProductResponse response = productService.getProductById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("iPhone 15", response.getName());
    }

    @Test
    void getProductById_NotFound() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () ->
                productService.getProductById(99L));
    }

    @Test
    void getAllProducts_Success() {
        when(productRepository.findAll()).thenReturn(List.of(product));

        List<ProductResponse> responses = productService.getAllProducts();

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("iPhone 15", responses.get(0).getName());
    }

    @Test
    void searchByName_Success() {
        when(productRepository.findByNameContainingIgnoreCase("iphone"))
                .thenReturn(List.of(product));

        List<ProductResponse> responses = productService.searchByName("iphone");

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("iPhone 15", responses.get(0).getName());
    }

    @Test
    void searchByName_NoResults() {
        when(productRepository.findByNameContainingIgnoreCase("xyz"))
                .thenReturn(List.of());

        List<ProductResponse> responses = productService.searchByName("xyz");

        assertNotNull(responses);
        assertEquals(0, responses.size());
    }

    @Test
    void updateProduct_Success() {
        ProductRequest updateRequest = ProductRequest.builder()
                .name("iPhone 16")
                .sku("APPL-IPH-16")
                .totalStock(300)
                .build();

        Product updatedProduct = Product.builder()
                .id(1L)
                .name("iPhone 16")
                .sku("APPL-IPH-16")
                .totalStock(300)
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(updatedProduct);

        ProductResponse response = productService.updateProduct(1L, updateRequest);

        assertNotNull(response);
        assertEquals("iPhone 16", response.getName());
        assertEquals("APPL-IPH-16", response.getSku());
        assertEquals(300, response.getTotalStock());
    }

    @Test
    void updateProduct_NotFound() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () ->
                productService.updateProduct(99L, request));
    }

    @Test
    void deleteProduct_Success() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        doNothing().when(productRepository).delete(product);

        assertDoesNotThrow(() -> productService.deleteProduct(1L));

        verify(productRepository, times(1)).delete(product);
    }

    @Test
    void deleteProduct_NotFound() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () ->
                productService.deleteProduct(99L));

        verify(productRepository, never()).delete(any(Product.class));
    }

    @Test
    void findProductEntity_Success() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        Product found = productService.findProductEntity(1L);

        assertNotNull(found);
        assertEquals(1L, found.getId());
        assertEquals("iPhone 15", found.getName());
    }

    @Test
    void findProductEntity_NotFound() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () ->
                productService.findProductEntity(99L));
    }

}