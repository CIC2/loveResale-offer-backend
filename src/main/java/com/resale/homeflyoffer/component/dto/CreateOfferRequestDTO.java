package com.resale.homeflyoffer.component.dto;

import lombok.*;

@Data
public class CreateOfferRequestDTO {

    private Integer customerId;
    private Integer appointmentId;
    private Integer unitId;
    private String partner;
    private String plan;
    private String paymentPlanDescriptionEn;
    private String paymentPlanDescriptionAr;
    private String clubPlan;
    private String clubPlanEn;
    private String clubPlanAr;
    private String maintenancePlan;
    private String maintenancePlanEn;
    private String maintenancePlanAr;
    private String unit;
    private String finishStatus;
    private String project;
    private String totalAmount;
    private String companyCode;
    private String paymentMethod;
    private String amount;
    private String currency;
    private String employee;
    private String isPdc;
    private String vipCode;
    private String funding;
}

