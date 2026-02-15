package com.resale.homeflyoffer.repos;

import com.resale.homeflyoffer.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer> {

    boolean existsByOfferIdAndAmountGreaterThan(Integer offerId, Integer amount);
}


