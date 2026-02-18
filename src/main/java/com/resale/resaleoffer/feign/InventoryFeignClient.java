package com.resale.resaleoffer.feign;

import com.resale.resaleoffer.component.dto.UnitResponseDTO;
import com.resale.resaleoffer.security.InventoryFeignConfig;
import com.resale.resaleoffer.utils.ReturnObject;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(
        name = "inventory-service",
        url = "${inventory.url}",
        configuration = InventoryFeignConfig.class
)
public interface InventoryFeignClient {

    @PostMapping("/getUnitsByIds")
    ResponseEntity<ReturnObject<List<UnitResponseDTO>>> getUnitsByIds(
            @RequestBody List<String> unitIds
    );

    @PutMapping("/{unitId}/status")
    ResponseEntity<ReturnObject<Object>> changeUnitStatusToInProcess(
            @PathVariable("unitId") Integer unitId
    );

}

