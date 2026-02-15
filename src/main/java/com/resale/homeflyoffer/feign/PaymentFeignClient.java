package com.resale.homeflyoffer.feign;

import com.resale.homeflyoffer.component.dto.BankTransferByOfferIdsRequest;
import com.resale.homeflyoffer.component.dto.BankTransferByOfferResponse;
import com.resale.homeflyoffer.component.dto.BankTransferResponse;
import com.resale.homeflyoffer.component.dto.ViewPaymentDTO;
import com.resale.homeflyoffer.utils.ReturnObject;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(
        name = "payment-service",
        url = "${services.payment.url}"
)
public interface PaymentFeignClient {

    /* =========================
       Customer Payments
       ========================= */

    @GetMapping("/bank/customerPayments")
    ResponseEntity<ReturnObject<List<ViewPaymentDTO>>> getAllCustomerPayments(
            @RequestParam Integer customerId,
            @RequestParam Integer offerId
    );

    /* =========================
       Bank Transfers by Offer IDs
       ========================= */

    @PostMapping("/bank/internal/bank-transfers/by-offer-ids")
    ResponseEntity<ReturnObject<List<BankTransferByOfferResponse>>>
    getBankTransfersByOfferIds(
            @RequestBody BankTransferByOfferIdsRequest request
    );

    /* =========================
       Bank Transfer by ID ✅
       ========================= */

    @GetMapping("/bank/bank-transfer/{bankTransferId}")
    ResponseEntity<ReturnObject<BankTransferResponse>>
    getBankTransferById(
            @PathVariable Integer bankTransferId
    );
}


