package com.resale.resaleoffer.component.offer;

import com.resale.resaleoffer.model.OfferStatus;
import com.resale.resaleoffer.utils.ReturnObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
public class OfferAdminController {
    @Autowired
    private OfferService offerService;

    @GetMapping("/bank-transfer/pending")
    public ResponseEntity<ReturnObject<?>>
    getBankTransferOffers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) OfferStatus status
    ) {
        return offerService.getBankTransferOffers(page, size, status);
    }

    @GetMapping("/bank-transfer/{bankTransferId}")
    public ResponseEntity<ReturnObject<?>>
    getBankTransferDetails(
            @PathVariable Integer bankTransferId
    ) {
        return offerService.getBankTransferDetails(bankTransferId);
    }


    /* ===============================
       1️⃣ Admin Approval
       =============================== */
    @PutMapping("/bank-transfer/{offerId}/approve")
    public ResponseEntity<ReturnObject<?>>
    approveBankTransfer(
            @PathVariable Integer offerId,
            @RequestParam Integer adminUserId
    ) {
        return offerService.approveBankTransfer(offerId, adminUserId);
    }

    /* ===============================
       2️⃣ FI Approval
       =============================== */
    @PutMapping("/bank-transfer/{offerId}/fi-approve")
    public ResponseEntity<ReturnObject<?>>
    fiApproveBankTransfer(
            @PathVariable Integer offerId,
            @RequestParam Integer fiUserId
    ) {
        return offerService.fiApproveBankTransfer(offerId, fiUserId);
    }

    /* ===============================
       3️⃣ Release Approval
       =============================== */
    @PutMapping("/bank-transfer/{offerId}/release")
    public ResponseEntity<ReturnObject<?>>
    releaseBankTransfer(
            @PathVariable Integer offerId,
            @RequestParam Integer releaseUserId
    ) {
        return offerService.releaseBankTransfer(offerId, releaseUserId);
    }
}


