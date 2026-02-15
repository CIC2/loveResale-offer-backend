package com.resale.homeflyoffer.component.offer;

import com.resale.homeflyoffer.component.dto.UpdateOfferPaymentDTO;
import com.resale.homeflyoffer.security.customer.CurrentCustomerId;
import com.resale.homeflyoffer.utils.ReturnObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/customer")
public class OfferCustomerController {

    @Autowired
    private OfferService offerService;

    @GetMapping
    public ResponseEntity<?> getAllCustomerOffers(@CurrentCustomerId Long customerId,
                                                  @RequestParam(defaultValue = "0") int page,
                                                  @RequestParam(defaultValue = "10") int size) {
        return offerService.getAllCustomerOffers(page,size,customerId);
    }
    @GetMapping("/{offerId}")
    public ResponseEntity<?> getCustomerOfferById(
            @PathVariable Integer offerId,
            @CurrentCustomerId Long customerId
    ) {
        System.out.println("➡️ Incoming request → offerId=" + offerId + ", customerId=" + customerId);
        return offerService.getCustomerOfferById(offerId, customerId);
    }
    @GetMapping("/getInternalOffer")
    public ResponseEntity<?> getCustomerOfferById(
            @RequestParam Long customerId,
            @RequestParam Integer offerId,
            @RequestHeader(value = "X-Internal-Auth", required = false) String internalToken
    ) {
        System.out.println("➡️ Incoming request → offerId=" + offerId + ", customerId=" + customerId);
        return offerService.getCustomerOfferById(offerId, customerId);
    }

    @PutMapping("/internal/offers/payment")
    public ResponseEntity<ReturnObject<?>> updateOfferAfterPayment(
            @RequestBody UpdateOfferPaymentDTO request
//            @RequestHeader(value = "X-Internal-Auth", required = false) String internalToken

    ) {
        return offerService.updateOfferAfterPayment(request);
    }

}


