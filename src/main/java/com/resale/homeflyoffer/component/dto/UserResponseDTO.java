package com.resale.homeflyoffer.component.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserResponseDTO {
    private Integer id;
    private String fullName;
    private String email;
    private String mobile;
    private Boolean isActive;
    private Integer status;
    private String sapId;
}


