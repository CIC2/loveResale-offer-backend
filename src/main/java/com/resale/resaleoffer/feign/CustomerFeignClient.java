package com.resale.resaleoffer.feign;

import com.resale.resaleoffer.component.dto.CustomerResponseDTO;
import com.resale.resaleoffer.component.dto.CustomerValidationResultDTO;
import com.resale.resaleoffer.component.dto.ProfileResponseDTO;
import com.resale.resaleoffer.utils.ReturnObject;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(
        name = "customer-service",
        url = "${customer.url}",
        configuration = CustomerFeignConfig.class)
public interface CustomerFeignClient {
    @GetMapping("/id")
    ResponseEntity<ReturnObject<ProfileResponseDTO>> getCustomerProfile(
            @RequestParam("customerId") Integer customerId);

    @GetMapping("/listByIds")
    ResponseEntity<ReturnObject<List<CustomerResponseDTO>>> getCustomersByIds(
            @RequestParam("customerIds") List<Integer> customerIds,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "mobile", required = false) String mobile
//            @RequestHeader("X-Internal-Auth") String internalToken
    );

    @GetMapping("/validateFullyRegistered")
    ResponseEntity<ReturnObject<CustomerValidationResultDTO>> validateCustomer(
            @RequestParam("customerId") Integer customerId
    );
}


