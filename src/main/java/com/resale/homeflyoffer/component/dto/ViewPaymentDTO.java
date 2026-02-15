package com.resale.homeflyoffer.component.dto;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class ViewPaymentDTO {

    private Integer id;
    private Integer offerId;
    private Integer customerId;
    private Double amount;
    private String paymentMethod;
    private String sapFiDocument;
    private String referenceId;
    private Timestamp createdAt;

    private int paymentId;


    private Integer paymentAmount;


    private Integer paymentStatus;

    private Timestamp paidAt;


    private String bankResponse;

    private Timestamp paymentCreatedAt;

    private Timestamp paymentUpdatedAt;

    private int unitId;


    private String reservationAmount;

    private String sapOfferNumber;

    private String offerPaidAmount;

    private String offerStatusTextEn;

    private String offerStatusTextAr;

    private String paymentPlan;

    private String finishing;

    private String maintenancePlan;

    private String clubPlan;

    private String customerName;

    private String customerNameAr;

    private String customerEmail;

    private String customerMobile;

    private String customerSapPartnerId;

    private String unitNameEn;

    private String unitNameAr;

    private Integer projectId;
}


