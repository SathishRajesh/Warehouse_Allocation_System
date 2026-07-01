package com.example.demo.Repository;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.example.demo.Entity.StockTransfer;

@Repository
public interface StockTransferRepository extends JpaRepository<StockTransfer, Long> {

    List<StockTransfer> findBySourceWarehouseId(Long sourceWarehouseId);

    List<StockTransfer> findByTargetWarehouseId(Long targetWarehouseId);

    List<StockTransfer> findByProductId(Long productId);

    @Query("SELECT st FROM StockTransfer st " +
           "WHERE st.sourceWarehouse.id = :warehouseId " +
           "OR st.targetWarehouse.id = :warehouseId")
    List<StockTransfer> findAllTransfersByWarehouseId(
            @Param("warehouseId") Long warehouseId);

    @Query("SELECT st FROM StockTransfer st " +
           "WHERE st.transferDate BETWEEN :startDate AND :endDate")
    List<StockTransfer> findByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT st FROM StockTransfer st " +
           "WHERE st.product.id = :productId " +
           "AND st.transferDate BETWEEN :startDate AND :endDate")
    List<StockTransfer> findByProductIdAndDateRange(
            @Param("productId") Long productId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

}