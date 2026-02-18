package com.resale.resaleoffer.component.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateOfferPaymentDTO {

    private Integer offerId;
    private Integer paidAmount;
    private String paymentMethod; // VISA | BANK_TRANSFER
    private String referenceId;   // paymentId or bankTransferId
}


