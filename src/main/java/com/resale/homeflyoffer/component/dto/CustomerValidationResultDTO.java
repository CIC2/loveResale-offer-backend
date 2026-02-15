package com.resale.homeflyoffer.component.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerValidationResultDTO {
    private boolean valid;
    private List<String> missingFields;
}

