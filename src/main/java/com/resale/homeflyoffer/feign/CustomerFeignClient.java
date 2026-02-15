package com.resale.homeflyoffer.feign;

import com.resale.homeflyoffer.component.dto.CustomerResponseDTO;
import com.resale.homeflyoffer.component.dto.CustomerValidationResultDTO;
import com.resale.homeflyoffer.component.dto.ProfileResponseDTO;
import com.resale.homeflyoffer.utils.ReturnObject;
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


