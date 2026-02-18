package com.resale.resaleoffer.repos;

import com.resale.resaleoffer.model.Offer;
import com.resale.resaleoffer.model.OfferStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OfferRepository extends JpaRepository<Offer, Integer> {
    List<Offer> findAllByCustomerId(Integer customerId);
    Page<Offer> findAllByCustomerId(Integer userId, Pageable pageable);
    List<Offer> findAllByUserId(Integer customerId);
    Optional<Offer> getOfferByIdAndCustomerId(Integer offerId,Integer customerId);
    Page<Offer> findAllByUserId(Integer userId, Pageable pageable);

    @Query("SELECT o FROM Offer o WHERE o.userId = :userId " +
            "AND (:offerNumber IS NULL OR o.sapOfferNumber LIKE %:offerNumber% OR CAST(o.id AS string) LIKE %:offerNumber%) " +
            "AND (:status IS NULL OR o.status = :status)")
    Page<Offer> findAllByUserIdWithFilters(@Param("userId") Integer userId,
                                           @Param("offerNumber") String offerNumber,
                                           @Param("status") OfferStatus status,
                                           Pageable pageable);

    Page<Offer> findAllByStatusIn(
            List<OfferStatus> statuses,
            Pageable pageable
    );

    Page<Offer> findAllByStatus(
            OfferStatus status,
            Pageable pageable
    );
}

