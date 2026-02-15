package com.resale.homeflyoffer.component.dto;

import lombok.Data;

@Data
public class BankTransferByOfferResponse {
    private Integer offerId;
    private BankTransferResponse bankTransfer;
}

