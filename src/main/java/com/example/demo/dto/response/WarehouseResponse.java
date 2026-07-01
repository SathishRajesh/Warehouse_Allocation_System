package com.example.demo.dto.response;

import java.time.LocalDateTime;
import com.example.demo.Entity.WarehouseStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WarehouseResponse {

    private Long id;
    private String name;
    private String location;
    private Integer capacity;
    private WarehouseStatus status;
    private LocalDateTime createdAt;

}