package com.example.demo.Repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.demo.Entity.Warehouse;
import com.example.demo.Entity.WarehouseStatus;

@Repository
public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {

    List<Warehouse> findByStatus(WarehouseStatus status);

    List<Warehouse> findByNameContainingIgnoreCase(String name);

    List<Warehouse> findByLocationContainingIgnoreCase(String location);

    List<Warehouse> findByStatusAndNameContainingIgnoreCase(WarehouseStatus status, String name);

}