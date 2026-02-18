package com.resale.resaleoffer.component.offer;

import com.resale.resaleoffer.component.dto.CreateOfferRequestDTO;
import com.resale.resaleoffer.component.dto.UpdateOfferRequestDTO;
import com.resale.resaleoffer.model.OfferStatus;
import com.resale.resaleoffer.security.CheckPermission;
import com.resale.resaleoffer.security.user.CurrentUserId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sales")
public class OfferSalesController {
    @Autowired
    private OfferService offerService;

    @PostMapping("")
    @CheckPermission(value = {"sales:login"})
    public ResponseEntity<?> createOffer(@CurrentUserId Long userId, @RequestBody CreateOfferRequestDTO request) {
        return offerService.createOffer(userId,request);
    }


    @PutMapping("{id}")
    @CheckPermission(value = {"sales:login"})
    public ResponseEntity<?> updateOffer(@CurrentUserId Long userId,@PathVariable("id") Integer id, @RequestBody UpdateOfferRequestDTO request) {
        return offerService.updateOffer(userId,id,request);
    }


    @GetMapping("")
    @CheckPermission(value = {"sales:login"})
    public ResponseEntity<?> findMyOffers(@CurrentUserId Long userId,
                                          @RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "10") int size,
                                          @RequestParam(required = false) String offerNumber,
                                          @RequestParam(required = false) String unitNumber,
                                          @RequestParam(required = false) String customerNumber,
                                          @RequestParam(required = false) OfferStatus status) {
        return offerService.getUsersOffers(page, size, userId, offerNumber, status);
    }

    @GetMapping("/customerOffers")
    @CheckPermission(value = {"sales:login"})
    public ResponseEntity<?> getAllCustomerOffers(@RequestParam Long customerId,
                                                  @RequestParam(defaultValue = "0") int page,
                                                  @RequestParam(defaultValue = "10") int size) {
        return offerService.getAllCustomerOffers(page,size,customerId);
    }

}


