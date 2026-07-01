package com.example.demo.Repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.example.demo.Entity.WarehouseInventory;

@Repository
public interface WarehouseInventoryRepository extends JpaRepository<WarehouseInventory, Long> {

    Optional<WarehouseInventory> findByWarehouseIdAndProductId(Long warehouseId, Long productId);

    List<WarehouseInventory> findByWarehouseId(Long warehouseId);

    List<WarehouseInventory> findByProductId(Long productId);

    @Query("SELECT wi FROM WarehouseInventory wi " +
           "WHERE wi.product.id = :productId " +
           "AND wi.availableQuantity >= :quantity " +
           "AND wi.warehouse.status = 'ACTIVE' " +
           "ORDER BY wi.availableQuantity DESC")
    List<WarehouseInventory> findAvailableWarehousesForProduct(
            @Param("productId") Long productId,
            @Param("quantity") Integer quantity);

    @Query("SELECT SUM(wi.availableQuantity) FROM WarehouseInventory wi " +
           "WHERE wi.product.id = :productId")
    Integer getTotalStockForProduct(@Param("productId") Long productId);
    
    @Query("SELECT COALESCE(SUM(wi.availableQuantity), 0) FROM WarehouseInventory wi " +
           "WHERE wi.warehouse.id = :warehouseId")
    Integer getTotalStockInWarehouse(@Param("warehouseId") Long warehouseId);

}