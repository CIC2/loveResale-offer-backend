package com.resale.homeflyoffer.model;

public enum OfferStatus {
    PROC,   // in process
    DOWN,   // Down payment
    SOLD,   // Sold
    OREJ,    //  Rejected for No Payment
    BANK_TRANSFER_APPROVAL_PENDING,    //  Bank Transfer Pending
    BANK_TRANSFER_APPROVED,    //  Bank Transfer Approved
    BANK_TRANSFER_REJECTED,    //  Bank Transfer Rejected

    FI_APPROVAL_PENDING,
    RELEASE_PENDING,

}



