package com.example.demo.Repository;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.example.demo.Entity.Allocation;
import com.example.demo.Entity.AllocationStatus;

@Repository
public interface AllocationRepository extends JpaRepository<Allocation, Long> {

    List<Allocation> findByProductId(Long productId);

    List<Allocation> findByWarehouseId(Long warehouseId);

    List<Allocation> findByStatus(AllocationStatus status);

    @Query("SELECT a FROM Allocation a " +
           "WHERE a.product.id = :productId " +
           "AND a.warehouse.id = :warehouseId")
    List<Allocation> findByProductIdAndWarehouseId(
            @Param("productId") Long productId,
            @Param("warehouseId") Long warehouseId);

    @Query("SELECT a FROM Allocation a " +
           "WHERE a.allocatedAt BETWEEN :startDate AND :endDate")
    List<Allocation> findByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT a FROM Allocation a " +
           "WHERE (:productId IS NULL OR a.product.id = :productId) " +
           "AND (:warehouseId IS NULL OR a.warehouse.id = :warehouseId) " +
           "AND (:status IS NULL OR a.status = :status) " +
           "AND (a.allocatedAt BETWEEN :startDate AND :endDate)")
    Page<Allocation> searchAllocations(
            @Param("productId") Long productId,
            @Param("warehouseId") Long warehouseId,
            @Param("status") AllocationStatus status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);
    Page<Allocation> findAll(Pageable pageable);

}