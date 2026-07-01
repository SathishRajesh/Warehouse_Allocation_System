package com.example.demo.dto.response;

import java.time.LocalDateTime;
import com.example.demo.Entity.AllocationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AllocationResponse {

    private Long id;
    private Long productId;
    private String productName;
    private Long warehouseId;
    private String warehouseName;
    private Integer quantity;
    private LocalDateTime allocatedAt;
    private AllocationStatus status;

}