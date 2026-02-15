package com.resale.homeflyoffer.component.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CreateOfferResponseDTO {

    @JsonProperty("MESSAGE")
    private String message;

    @JsonProperty("MESSAGE2")
    private String message2;

    @JsonProperty("MSG_TYPE")
    private String msgType;
}

