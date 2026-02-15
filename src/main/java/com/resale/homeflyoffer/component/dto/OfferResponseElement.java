package com.resale.homeflyoffer.component.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class OfferResponseElement {
    @JsonProperty("Message")
    private String Message;
    @JsonProperty("OfferId")
    private String OfferId;
}


