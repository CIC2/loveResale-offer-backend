package com.resale.homeflyoffer.feign;

import com.resale.homeflyoffer.component.dto.UserResponseDTO;
import com.resale.homeflyoffer.utils.ReturnObject;
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


