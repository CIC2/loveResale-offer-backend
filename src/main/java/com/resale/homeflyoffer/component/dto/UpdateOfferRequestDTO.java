package com.resale.homeflyoffer.component.dto;

import lombok.Data;

@Data
public class UpdateOfferRequestDTO {
    private String offerId;
    private String branch;
    private String plan;
    private String clubPlan;
    private String maintenance;
    private String vipCode;
    private String transeferred;
}

