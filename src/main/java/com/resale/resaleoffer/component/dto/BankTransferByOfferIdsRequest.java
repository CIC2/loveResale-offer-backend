package com.resale.resaleoffer.component.dto;

import lombok.Data;

import java.util.List;

@Data
public class BankTransferByOfferIdsRequest {
    private List<Integer> offerIds;
}


