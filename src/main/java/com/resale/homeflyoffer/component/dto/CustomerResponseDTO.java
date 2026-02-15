package com.resale.homeflyoffer.component.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomerResponseDTO {
    private Integer customerId;
    private String fullName;
    private String mobile;
    private String email;
    private String nationality;

    private String sapPartnerId;
    private String nationalId;
    private String passportNumber;
    private String address;
    private String country;
    private String city;
    private String area;
}


