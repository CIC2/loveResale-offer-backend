package com.resale.resaleoffer.feign;

import com.resale.resaleoffer.component.dto.UserResponseDTO;
import com.resale.resaleoffer.utils.ReturnObject;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name = "user-service",
        url = "${user.url}",
        configuration = UserFeignConfig.class)
public interface UserFeignClient {
    @GetMapping("/id")
    ResponseEntity<ReturnObject<UserResponseDTO>> getUserProfile(
            @RequestParam("userId") Long userId);
}


