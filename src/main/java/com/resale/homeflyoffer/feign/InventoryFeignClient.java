package com.resale.homeflyoffer.feign;

import com.resale.homeflyoffer.component.dto.UnitResponseDTO;
import com.resale.homeflyoffer.security.InventoryFeignConfig;
import com.resale.homeflyoffer.utils.ReturnObject;
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

