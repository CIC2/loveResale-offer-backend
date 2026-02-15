package com.resale.homeflyoffer.component.dto;

import com.resale.homeflyoffer.model.OfferStatus;
import lombok.Data;

@Data
public class PendingBankTransferOfferDTO {

    private Integer offerId;
    private String offerNumber;

    private Integer unitId;
    private UnitResponseDTO unit;

    private Integer customerId;
    private CustomerResponseDTO customer;

    private OfferStatus status;

    private BankTransferResponse bankTransfer;
}


