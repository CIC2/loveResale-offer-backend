package com.resale.homeflyoffer.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Offer {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id")
    private int id;
    @Basic
    @Column(name = "unit_id")
    private Integer unitId;
    @Basic
    @Column(name = "customer_id")
    private Integer customerId;
    @Basic
    @Column(name = "user_id")
    private int userId;
    @Basic
    @Column(name = "appointment_id")
    private int appointmentId;
    @Basic
    @Column(name = "reservation_amount")
    private String reservationAmount;
    @Basic
    @Column(name = "unit_price")
    private String unitPrice;
    @Basic
    @Column(name = "reserved_at")
    private Timestamp reservedAt;
    @Basic
    @Column(name = "expires_at")
    private Timestamp expiresAt;
    @Basic
    @Column(name = "payment_plan")
    private String paymentPlan;
    @Basic
    @Column(name = "club_plan")
    private String clubPlan;
    @Basic
    @Column(name = "club_plan_description_en")
    private String clubPlanDescriptionEn;
    @Basic
    @Column(name = "club_plan_description_ar")
    private String clubPlanDescriptionAr;
    @Basic
    @Column(name = "payment_plan_description_en")
    private String paymentPlanDescriptionEn;
    @Basic
    @Column(name = "payment_plan_description_ar")
    private String paymentPlanDescriptionAr;
    @Basic
    @Column(name = "finishing")
    private String finishing;
    @Basic
    @Column(name = "maintenance_plan")
    private String maintenancePlan;
    @Basic
    @Column(name = "maintenance_plan_description_en")
    private String maintenancePlanDescriptionEn;
    @Basic
    @Column(name = "maintenance_plan_description_ar")
    private String maintenancePlanDescriptionAr;
    @Basic
    @Column(name = "sap_offer_number")
    private String sapOfferNumber;
    @Basic
    @Column(name = "paid_amount")
    private String paidAmount;
    @Basic
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private OfferStatus status;
    @Basic
    @Column(name = "status_text_en")
    private String statusTextEn;
    @Basic
    @Column(name = "status_text_ar")
    private String statusTextAr;
    @Basic
    @Column(name = "approve_flag")
    private String approveFlag;
    @Basic
    @Column(name = "approve_user")
    private Integer approveUser;
    @Basic
    @Column(name = "approve_datetime")
    private Timestamp approveDatetime;
    @Basic
    @Column(name = "fi_approve_flag")
    private String fiApproveFlag;
    @Basic
    @Column(name = "fi_approve_user")
    private Integer fiApproveUser;
    @Basic
    @Column(name = "fi_approve_datetime")
    private Timestamp fiApproveDatetime;
    @Basic
    @Column(name = "release_flag")
    private String releaseFlag;
    @Basic
    @Column(name = "release_user")
    private Integer releaseUser;
    @Basic
    @Column(name = "release_datetime")
    private Timestamp releaseDatetime;
}


