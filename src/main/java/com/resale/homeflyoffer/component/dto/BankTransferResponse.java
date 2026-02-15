package com.resale.homeflyoffer.component.dto;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class BankTransferResponse {

    private Integer id;
    private Integer offerId;
    private Integer amount;
    private String country;
    private String transferDate;
    private String customerBankName;
    private String tmgBankName;
    private String tmgBankAccount;
    private String imageUrl;
    private String imageContentType;
    private String fileName;
    private Timestamp createdAt;

}


