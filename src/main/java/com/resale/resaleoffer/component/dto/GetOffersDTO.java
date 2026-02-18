package com.resale.resaleoffer.component.dto;

import com.resale.resaleoffer.model.OfferStatus;
import lombok.Data;

import java.util.List;

@Data
public class GetOffersDTO {
    private Integer offerId;
    private Integer unitId;

    private String offerNumber;
    private String projectName;
    private String unitNumber;
    private String unitStatus;
    private String reservationAmount;
    private String remainingTime;
    private String orderDateTime;
    private String expirationDate;

    private String paymentPlan;
    private String paymentPlanDescriptionEn;
    private String paymentPlanDescriptionAr;
    private String finishing;
    private String maintenancePlan;
    private String maintenancePlanDescriptionEn;
    private String maintenancePlanDescriptionAr;

    private OfferStatus offerStatus;
    private String offerStatusTextEn;
    private String offerStatusTextAr;

    private Integer customerId;
    private String customerName;
    private String customerMobile;
    private String customerEmail;
    private String customerNationality;
    private String projectCode;
    private String unitModelCode;

    private List<ViewPaymentDTO> payments;
}


