package com.resale.resaleoffer.component.offer;

import com.resale.resaleoffer.component.dto.*;
import com.resale.resaleoffer.feign.*;
import com.resale.resaleoffer.model.Offer;
import com.resale.resaleoffer.model.OfferStatus;
import com.resale.resaleoffer.repos.OfferRepository;
import com.resale.resaleoffer.repos.PaymentRepository;
import com.resale.resaleoffer.utils.CustomerValidationService;
import com.resale.resaleoffer.utils.MessageUtil;
import com.resale.resaleoffer.utils.PaginatedResponseDTO;
import com.resale.resaleoffer.utils.ReturnObject;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OfferService {
    private final OfferRepository offerRepository;
    private final CustomerFeignClient customerFeignClient;
    private final UserFeignClient userFeignClient;
    private final InventoryFeignClient inventoryFeignClient;
    private final MessageUtil messageUtil;
    private final CustomerValidationService customerValidationService;

    @Autowired
    PaymentRepository paymentRepository;
    private final PaymentFeignClient paymentFeignClient;


    public OfferService(OfferRepository offerRepository,
                        CustomerFeignClient customerFeignClient,
                        UserFeignClient userFeignClient,
                        InventoryFeignClient inventoryFeignClient,
                        MessageUtil messageUtil,
                        CustomerValidationService customerValidationService, PaymentFeignClient paymentFeignClient) {
        this.offerRepository = offerRepository;
        this.customerFeignClient = customerFeignClient;
        this.userFeignClient = userFeignClient;
        this.inventoryFeignClient = inventoryFeignClient;
        this.messageUtil = messageUtil;
        this.customerValidationService = customerValidationService;
        this.paymentFeignClient = paymentFeignClient;
    }


    public ResponseEntity<?> createOffer(Long userId, CreateOfferRequestDTO request) {
        if(request.getCustomerId() == null){
            return ResponseEntity.badRequest().body("Missing Customer Id");
        }
        ResponseEntity<ReturnObject<CustomerValidationResultDTO>> validationResponse =
                customerFeignClient.validateCustomer(request.getCustomerId());

        // 2. run validation helper
        ReturnObject<CustomerValidationResultDTO> validationError =
                customerValidationService.extractCustomerValidationError(validationResponse);

        // 3. if error exists → return it
        if (validationError != null) {
            return ResponseEntity.badRequest().body(validationError);
        }

        try {
            // Call Customer Microservice securely
            ResponseEntity<ReturnObject<ProfileResponseDTO>> customerResponse =
                    customerFeignClient.getCustomerProfile(request.getCustomerId());
            if (!customerResponse.getStatusCode().is2xxSuccessful() || customerResponse.getBody().getData() == null) {
                throw new RuntimeException("Failed to fetch customer info from Customer Service");
            }

            ProfileResponseDTO customerProfile = customerResponse.getBody().getData();
            request.setPartner(customerProfile.getSapPartnerId());
            // Call Customer Microservice securely
            ResponseEntity<ReturnObject<UserResponseDTO>> userResponse =
                    userFeignClient.getUserProfile(userId);
            if (!userResponse.getStatusCode().is2xxSuccessful() || userResponse.getBody().getData() == null) {
                throw new RuntimeException("Failed to fetch user info from user Service");
            }

            UserResponseDTO userProfile = userResponse.getBody().getData();

            // ✅ Build new offer entity
            Offer newOffer = new Offer();
            newOffer.setFinishing(request.getFinishStatus());

            newOffer.setMaintenancePlan(request.getMaintenancePlan());
            newOffer.setMaintenancePlanDescriptionEn(request.getMaintenancePlanEn());
            newOffer.setMaintenancePlanDescriptionAr(request.getMaintenancePlanAr());

            newOffer.setPaymentPlan(request.getPlan());
            newOffer.setPaymentPlanDescriptionEn(request.getPaymentPlanDescriptionEn());
            newOffer.setPaymentPlanDescriptionAr(request.getPaymentPlanDescriptionAr());

            newOffer.setClubPlan(request.getClubPlan());
            newOffer.setClubPlanDescriptionEn(request.getClubPlanEn());
            newOffer.setClubPlanDescriptionAr(request.getClubPlanAr());

            newOffer.setCustomerId(request.getCustomerId());
            newOffer.setUserId(Math.toIntExact(userId));
            newOffer.setAppointmentId(request.getAppointmentId());
            newOffer.setUnitId(request.getUnitId());
            newOffer.setReservationAmount(request.getAmount());
            newOffer.setUnitPrice(request.getTotalAmount());
            newOffer.setReservedAt(Timestamp.valueOf(LocalDateTime.now()));
            newOffer.setExpiresAt(Timestamp.valueOf(LocalDateTime.now().plusHours(3)));
            newOffer.setStatus(OfferStatus.PROC);

            // ✅ Persist offer in database first to get ID
            offerRepository.save(newOffer);
            
            // Set SAP offer number to FAIL (SAP integration removed)
            newOffer.setSapOfferNumber("FAIL");

            // ✅ Update unit status
            try {
                ResponseEntity<ReturnObject<Object>> inventoryResponse =
                        inventoryFeignClient.changeUnitStatusToInProcess(request.getUnitId());

                if (!inventoryResponse.getBody().getStatus()) {
                    return ResponseEntity.status(HttpStatus.OK).body(
                            new ReturnObject<>("Offer created but unit change status failed: " +
                                    inventoryResponse.getBody().getMessage(), true, newOffer));
                }

            } catch (Exception lockEx) {
                return ResponseEntity.status(HttpStatus.OK).body(
                        new ReturnObject<>("Offer created but failed to change unit status in Inventory", true, newOffer));
            }

            return ResponseEntity.ok(new ReturnObject<>("Offer created successfully", true, newOffer));

        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity
                    .internalServerError()
                    .body(new ReturnObject<>("Error while creating offer", false, ex.getMessage()));
        }
    }


    public ResponseEntity<?> getUsersOffers(Integer page, Integer size, Long userId,
                                            String offerNumber, OfferStatus offerStatus) {
        try {
            System.out.println("➡️ Fetching offers for customerId: " + userId);

            Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

            Page<Offer> offers = offerRepository.findAllByUserIdWithFilters(
                    userId.intValue(),
                    offerNumber,
                    offerStatus,
                    pageable
            );

            if (offers == null || offers.isEmpty()) {
                System.out.println("⚠️ No offers found for customerId: " + userId);
                return ResponseEntity.ok(
                        new ReturnObject<>(
                                "No offers found for this customer",
                                true,
                                Collections.emptyList()
                        )
                );
            }

            List<String> unitList = offers.stream()
                    .map(Offer::getUnitId)
                    .filter(Objects::nonNull)
                    .map(String::valueOf)
                    .distinct()
                    .collect(Collectors.toList());

            List<Integer> customerIdsList = offers.stream()
                    .map(Offer::getCustomerId)
                    .distinct()
                    .collect(Collectors.toList());

            System.out.println("✅ Unique Unit IDs: " + unitList);

            List<UnitResponseDTO> unitDetails = new ArrayList<>();
            if (!unitList.isEmpty()) {
                System.out.println("➡️ Calling inventory service with Unit IDs: " + unitList);
                ResponseEntity<ReturnObject<List<UnitResponseDTO>>> unitResponse =
                        inventoryFeignClient.getUnitsByIds(unitList);

                if (unitResponse != null && unitResponse.getBody() != null) {
                    unitDetails = unitResponse.getBody().getData();
                    System.out.println("✅ Inventory response size: " + (unitDetails != null ? unitDetails.size() : 0));
                } else {
                    System.out.println("❌ Inventory service returned null response or empty body");
                }
            }
            List<CustomerResponseDTO> customersDetailsList = new ArrayList<>();
            if (!customerIdsList.isEmpty()) {
                System.out.println("➡️ Calling inventory service with Unit IDs: " + customersDetailsList);
                ResponseEntity<ReturnObject<List<CustomerResponseDTO>>> customerResponse =
                        customerFeignClient.getCustomersByIds(customerIdsList,null,null);

                if (customerResponse != null && customerResponse.getBody() != null) {
                    customersDetailsList = customerResponse.getBody().getData();
                    System.out.println("✅ Inventory response size: " + (customersDetailsList != null ? customersDetailsList.size() : 0));
                } else {
                    System.out.println("❌ Inventory service returned null response or empty body");
                }
            }

            Map<Integer, UnitResponseDTO> unitMap = unitDetails != null
                    ? unitDetails.stream().collect(Collectors.toMap(UnitResponseDTO::getId, u -> u))
                    : new HashMap<>();

            Map<Integer, CustomerResponseDTO> customerMap = customersDetailsList.stream()
                    .filter(c -> c.getCustomerId() != null) // <-- ignore invalid customers
                    .collect(Collectors.toMap(CustomerResponseDTO::getCustomerId, c -> c));

            System.out.println("✅ Unit Map Keys: " + unitMap.keySet());

            List<GetOffersDTO> responseList = offers.stream().map(o -> {
                UnitResponseDTO unit = unitMap.get(o.getUnitId());
                GetOffersDTO dto = new GetOffersDTO();

                dto.setOfferId(o.getId());
                dto.setUnitId(unit != null ? unit.getId() : null);
                dto.setOfferNumber(
                        "FAIL".equals(o.getSapOfferNumber()) ? String.valueOf(o.getId()) : o.getSapOfferNumber()
                );

                dto.setProjectName(unit != null ? unit.getProjectName() : null);
                dto.setUnitNumber(unit != null ? unit.getName() : null);
                dto.setReservationAmount(o.getReservationAmount());

                CustomerResponseDTO customer = customerMap.get(o.getCustomerId());

                dto.setCustomerId(customer != null ? customer.getCustomerId() : null);
                dto.setCustomerName(customer != null ? customer.getFullName() : null);
                dto.setCustomerMobile(customer != null ? customer.getMobile() : null);
                dto.setCustomerEmail(customer != null ? customer.getEmail() : null);
                dto.setCustomerNationality(customer != null ? customer.getNationality() : null);

                LocalDateTime reservedAt = o.getReservedAt() != null ? o.getReservedAt().toLocalDateTime() : null;
                LocalDateTime expiresAt = o.getExpiresAt() != null ? o.getExpiresAt().toLocalDateTime() : null;

                Duration duration = null;
                if (reservedAt != null && expiresAt != null) {
                    duration = Duration.between(LocalDateTime.now(), expiresAt);
                    long hours = Math.abs(duration.toHours());
                    long minutes = Math.abs(duration.toMinutesPart());
                    dto.setRemainingTime((duration.isNegative() ? "-" : "") + hours + "h " + minutes + "m");
                } else {
                    dto.setRemainingTime(null);
                }

                dto.setOrderDateTime(o.getReservedAt() != null ? o.getReservedAt().toString() : null);
                dto.setExpirationDate(String.valueOf(o.getExpiresAt()));

                // ✅ BUSINESS STATUS LOGIC
                Double paidAmount = o.getPaidAmount() != null ? Double.valueOf(o.getPaidAmount()) : 0.0;
                Double reservationAmount = o.getReservationAmount() != null ? Double.valueOf(o.getReservationAmount()) : 0.0;

                String status;
                if (paidAmount.equals(reservationAmount)) {
                    status = "SOLD";
                } else if (duration != null && duration.isNegative() && !paidAmount.equals(reservationAmount)) {
                    status = "EXPIRED";
                } else {
                    status = "INPROCESS";
                }

                dto.setUnitStatus(status);

                dto.setPaymentPlan(o.getPaymentPlan());
                dto.setPaymentPlanDescriptionEn(o.getPaymentPlanDescriptionEn());
                dto.setPaymentPlanDescriptionAr(o.getPaymentPlanDescriptionAr());

                dto.setMaintenancePlan(o.getMaintenancePlan());
                dto.setMaintenancePlanDescriptionEn(o.getMaintenancePlanDescriptionEn());
                dto.setMaintenancePlanDescriptionAr(o.getMaintenancePlanDescriptionAr());

                dto.setFinishing(o.getFinishing());

                dto.setOfferStatus(o.getStatus());
                dto.setOfferStatusTextEn(o.getStatusTextEn());
                dto.setOfferStatusTextAr(o.getStatusTextAr());

                System.out.println("🏷️ OfferId " + o.getId() + " → Status: " + status);

                return dto;
            }).collect(Collectors.toList());
            PaginatedResponseDTO<GetOffersDTO> paginatedResponse = new PaginatedResponseDTO<>();

            paginatedResponse.setContent(responseList);
            paginatedResponse.setPage(offers.getNumber());
            paginatedResponse.setSize(offers.getSize());
            paginatedResponse.setTotalElements(offers.getTotalElements());
            paginatedResponse.setTotalPages(offers.getTotalPages());
            paginatedResponse.setLast(offers.isLast());
            System.out.println("✅ Returning offers count: " + responseList.size());

            return ResponseEntity.ok(
                    new ReturnObject<>(
                            "Customer offers retrieved successfully",
                            true,
                            paginatedResponse
                    )
            );

        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("❌ Error fetching offers: " + ex.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(
                            new ReturnObject<>(
                                    "An error occurred while fetching customer offers",
                                    false,
                                    null
                            )
                    );
        }
    }

    public ResponseEntity<?> getAllCustomerOffers(Integer page,Integer size,Long customerId) {
        try {
            System.out.println("➡️ Fetching offers for customerId: " + customerId);
            Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

            Page<Offer> offers = offerRepository.findAllByCustomerId(customerId.intValue(), pageable);

            if (offers == null || offers.isEmpty()) {
                System.out.println("⚠️ No offers found for customerId: " + customerId);

                Map<String, Object> emptyData = new HashMap<>();
                PaginatedResponseDTO<GetOffersDTO> paginatedResponse = new PaginatedResponseDTO<>();

                paginatedResponse.setContent(Collections.emptyList());
                paginatedResponse.setPage(offers.getNumber());
                paginatedResponse.setSize(offers.getSize());
                paginatedResponse.setTotalElements(offers.getTotalElements());
                paginatedResponse.setTotalPages(offers.getTotalPages());
                paginatedResponse.setLast(offers.isLast());
                System.out.println("✅ Returning offers count: " + emptyData.size());

                return ResponseEntity.ok(
                        new ReturnObject<>(
                                "No offers found for this customer",
                                true,
                                paginatedResponse
                        )
                );
            }

            List<String> unitList = offers.stream()
                    .map(Offer::getUnitId)
                    .filter(Objects::nonNull)
                    .map(String::valueOf)
                    .distinct()
                    .collect(Collectors.toList());

            System.out.println("✅ Unique Unit IDs: " + unitList);

            List<UnitResponseDTO> unitDetails = new ArrayList<>();
            if (!unitList.isEmpty()) {
                System.out.println("➡️ Calling inventory service with Unit IDs: " + unitList);
                ResponseEntity<ReturnObject<List<UnitResponseDTO>>> unitResponse =
                        inventoryFeignClient.getUnitsByIds(unitList);

                if (unitResponse != null && unitResponse.getBody() != null) {
                    unitDetails = unitResponse.getBody().getData();
                    System.out.println("✅ Inventory response size: " + (unitDetails != null ? unitDetails.size() : 0));
                } else {
                    System.out.println("❌ Inventory service returned null response or empty body");
                }
            }

            Map<Integer, UnitResponseDTO> unitMap = unitDetails != null
                    ? unitDetails.stream().collect(Collectors.toMap(UnitResponseDTO::getId, u -> u))
                    : new HashMap<>();

            System.out.println("✅ Unit Map Keys: " + unitMap.keySet());

            List<GetOffersDTO> responseList = offers.stream().map(o -> {
                UnitResponseDTO unit = unitMap.get(o.getUnitId());
                GetOffersDTO dto = new GetOffersDTO();

                dto.setOfferId(o.getId());
                dto.setUnitId(unit != null ?unit.getId() : null);
                dto.setOfferNumber(
                        "FAIL".equals(o.getSapOfferNumber()) ? String.valueOf(o.getId()) : o.getSapOfferNumber()
                );

                dto.setProjectName(unit != null ? unit.getProjectName() : null);
                dto.setUnitNumber(unit != null ? unit.getName() : null);
                dto.setReservationAmount(o.getReservationAmount());

                LocalDateTime reservedAt = o.getReservedAt() != null ? o.getReservedAt().toLocalDateTime() : null;
                LocalDateTime expiresAt = o.getExpiresAt() != null ? o.getExpiresAt().toLocalDateTime() : null;

                Duration duration = null;
                if (reservedAt != null && expiresAt != null) {
                    duration = Duration.between(LocalDateTime.now(), expiresAt);
                    long hours = Math.abs(duration.toHours());
                    long minutes = Math.abs(duration.toMinutesPart());
                    dto.setRemainingTime((duration.isNegative() ? "-" : "") + hours + "h " + minutes + "m");
                } else {
                    dto.setRemainingTime(null);
                }

                dto.setOrderDateTime(o.getReservedAt() != null ? o.getReservedAt().toString() : null);
                dto.setExpirationDate(String.valueOf(o.getExpiresAt()));

                // ✅ BUSINESS STATUS LOGIC
                Double paidAmount = o.getPaidAmount() != null ? Double.valueOf(o.getPaidAmount()) : 0.0;
                Double reservationAmount = o.getReservationAmount() != null ? Double.valueOf(o.getReservationAmount()) : 0.0;

                String status;
                if (paidAmount.equals(reservationAmount)) {
                    status = "SOLD";
                } else if (duration != null && duration.isNegative() && !paidAmount.equals(reservationAmount)) {
                    status = "EXPIRED";
                } else {
                    status = String.valueOf(o.getStatus());
                }

                dto.setUnitStatus(status);

                dto.setPaymentPlan(o.getPaymentPlan());
                dto.setPaymentPlanDescriptionEn(o.getPaymentPlanDescriptionEn());
                dto.setPaymentPlanDescriptionAr(o.getPaymentPlanDescriptionAr());

                dto.setMaintenancePlan(o.getMaintenancePlan());
                dto.setMaintenancePlanDescriptionEn(o.getMaintenancePlanDescriptionEn());
                dto.setMaintenancePlanDescriptionAr(o.getMaintenancePlanDescriptionAr());

                dto.setFinishing(o.getFinishing());

                System.out.println("🏷️ OfferId " + o.getId() + " → Status: " + status);

                return dto;
            }).collect(Collectors.toList());

            PaginatedResponseDTO<GetOffersDTO> paginatedResponse = new PaginatedResponseDTO<>();

            paginatedResponse.setContent(responseList);
            paginatedResponse.setPage(offers.getNumber());
            paginatedResponse.setSize(offers.getSize());
            paginatedResponse.setTotalElements(offers.getTotalElements());
            paginatedResponse.setTotalPages(offers.getTotalPages());
            paginatedResponse.setLast(offers.isLast());
            System.out.println("✅ Returning offers count: " + responseList.size());

            return ResponseEntity.ok(
                    new ReturnObject<>(
                            "Customer offers retrieved successfully",
                            true,
                            paginatedResponse
                    )
            );


        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("❌ Error fetching offers: " + ex.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(
                            new ReturnObject<>(
                                    "An error occurred while fetching customer offers",
                                    false,
                                    null
                            )
                    );
        }
    }
    public ResponseEntity<?> getCustomerOfferById(Integer offerId, Long customerId) {

        try {
            System.out.println("➡️ Fetching offer by ID: " + offerId + " for customerId: " + customerId);

            Optional<Offer> offerOpt =
                    offerRepository.getOfferByIdAndCustomerId(offerId, customerId.intValue());

            if (offerOpt == null || offerOpt.isEmpty()) {
                System.out.println("⚠️ No offer found for offerId: " + offerId);

                return ResponseEntity.ok(
                        new ReturnObject<>(
                                "No offer found for this customer",
                                true,
                                null
                        )
                );
            }

            Offer offer = offerOpt.get();
            System.out.println("✅ Offer found with ID: " + offer.getId());

        /* ---------------------------------------------------
           Fetch Unit Details
        --------------------------------------------------- */
            UnitResponseDTO unit = null;

            if (offer.getUnitId() != null) {
                System.out.println("➡️ Fetching unit details for unitId: " + offer.getUnitId());

                ResponseEntity<ReturnObject<List<UnitResponseDTO>>> unitResponse =
                        inventoryFeignClient.getUnitsByIds(
                                List.of(String.valueOf(offer.getUnitId()))
                        );

                if (unitResponse.getStatusCode().is2xxSuccessful()
                        && unitResponse.getBody() != null
                        && unitResponse.getBody().getData() != null
                        && !unitResponse.getBody().getData().isEmpty()) {

                    unit = unitResponse.getBody().getData().get(0);
                    System.out.println("✅ Unit loaded successfully");
                } else {
                    System.out.println("⚠️ Unit service returned empty response");
                }
            }

        /* ---------------------------------------------------
           Build Offer DTO
        --------------------------------------------------- */
            GetOffersDTO dto = new GetOffersDTO();

            dto.setOfferId(offer.getId());
            dto.setUnitId(unit != null ? unit.getId() : null);

            dto.setOfferNumber(
                    "FAIL".equals(offer.getSapOfferNumber())
                            ? String.valueOf(offer.getId())
                            : offer.getSapOfferNumber()
            );

            dto.setProjectName(unit != null ? unit.getProjectName() : null);
            dto.setUnitNumber(unit != null ? unit.getName() : null);
            dto.setProjectCode(unit != null ? unit.getProjectCode() : null);
            dto.setUnitModelCode(unit != null ? unit.getUnitModelCode() : null);

            dto.setReservationAmount(offer.getReservationAmount());

            dto.setOrderDateTime(
                    offer.getReservedAt() != null
                            ? offer.getReservedAt().toString()
                            : null
            );

            dto.setExpirationDate(
                    offer.getExpiresAt() != null
                            ? offer.getExpiresAt().toString()
                            : null
            );

        /* ---------------------------------------------------
           Remaining Time Calculation
        --------------------------------------------------- */
            if (offer.getReservedAt() != null && offer.getExpiresAt() != null) {

                LocalDateTime expiresAt = offer.getExpiresAt().toLocalDateTime();
                Duration duration = Duration.between(LocalDateTime.now(), expiresAt);

                long hours = Math.abs(duration.toHours());
                long minutes = Math.abs(duration.toMinutesPart());

                dto.setRemainingTime(
                        (duration.isNegative() ? "-" : "") + hours + "h " + minutes + "m"
                );
            }

        /* ---------------------------------------------------
           Business Status Logic
        --------------------------------------------------- */
            double paidAmount =
                    offer.getPaidAmount() != null
                            ? Double.parseDouble(offer.getPaidAmount())
                            : 0.0;

            double reservationAmount =
                    offer.getReservationAmount() != null
                            ? Double.parseDouble(offer.getReservationAmount())
                            : 0.0;

            String status;
            if (paidAmount >= reservationAmount) {
                status = "SOLD";
            } else if (offer.getExpiresAt() != null
                    && offer.getExpiresAt().toLocalDateTime().isBefore(LocalDateTime.now())) {
                status = "EXPIRED";
            } else {
                status = "INPROCESS";
            }

            dto.setUnitStatus(status);
            System.out.println("🏷️ Offer status: " + status);

        /* ---------------------------------------------------
           Plans & Finishing
        --------------------------------------------------- */
            dto.setPaymentPlan(offer.getPaymentPlan());
            dto.setPaymentPlanDescriptionEn(offer.getPaymentPlanDescriptionEn());
            dto.setPaymentPlanDescriptionAr(offer.getPaymentPlanDescriptionAr());

            dto.setMaintenancePlan(offer.getMaintenancePlan());
            dto.setMaintenancePlanDescriptionEn(offer.getMaintenancePlanDescriptionEn());
            dto.setMaintenancePlanDescriptionAr(offer.getMaintenancePlanDescriptionAr());

            dto.setFinishing(offer.getFinishing());

        /* ---------------------------------------------------
           Customer Info
        --------------------------------------------------- */
            System.out.println("➡️ Fetching customer profile");

            ResponseEntity<ReturnObject<ProfileResponseDTO>> customerResponse =
                    customerFeignClient.getCustomerProfile(customerId.intValue());

            if (!customerResponse.getStatusCode().is2xxSuccessful()
                    || customerResponse.getBody() == null
                    || customerResponse.getBody().getData() == null) {

                throw new RuntimeException("Failed to fetch customer profile");
            }

            dto.setCustomerName(customerResponse.getBody().getData().getFullName());
            dto.setCustomerMobile(customerResponse.getBody().getData().getMobile());

        /* ---------------------------------------------------
           Payments (Payment Microservice)
        --------------------------------------------------- */
            System.out.println("➡️ Fetching payments for offerId: " + offer.getId());

            ResponseEntity<ReturnObject<List<ViewPaymentDTO>>> paymentsResponse =
                    paymentFeignClient.getAllCustomerPayments(
                            customerId.intValue(),
                            offer.getId()
                    );

            if (paymentsResponse.getStatusCode().is2xxSuccessful()
                    && paymentsResponse.getBody() != null) {

                dto.setPayments(paymentsResponse.getBody().getData());

                System.out.println("✅ Payments loaded: "
                        + (paymentsResponse.getBody().getData() != null
                        ? paymentsResponse.getBody().getData().size()
                        : 0));
            } else {
                System.out.println("⚠️ No payments found");
                dto.setPayments(Collections.emptyList());
            }

        /* ---------------------------------------------------
           Final Response
        --------------------------------------------------- */
            return ResponseEntity.ok(
                    new ReturnObject<>(
                            "Customer offer retrieved successfully",
                            true,
                            dto
                    )
            );

        } catch (Exception ex) {

            ex.printStackTrace();
            System.out.println("❌ Error in getCustomerOfferById: " + ex.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(
                            new ReturnObject<>(
                                    "An error occurred while fetching offer",
                                    false,
                                    null
                            )
                    );
        }
    }


    public ResponseEntity<?> updateOffer(Long userId,Integer id, UpdateOfferRequestDTO request) {
        ReturnObject returnObject = new ReturnObject();
        Optional<Offer> offerOptional = offerRepository.findById(id);
        if(offerOptional.isEmpty()){
            returnObject.setMessage(messageUtil.getMessage("no.offer.found.response"));
            returnObject.setStatus(false);
            returnObject.setData(null);
            return ResponseEntity
                    .internalServerError()
                    .body(returnObject);
        }
        Offer offer = offerOptional.get();

        if (offer.getStatus() != OfferStatus.PROC) {
            returnObject.setMessage("Offer cannot be updated unless it is in PROC status");
            returnObject.setStatus(false);
            returnObject.setData(offer.getStatus().name());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(returnObject);
        }

        if (paymentRepository.existsByOfferIdAndAmountGreaterThan(offer.getId(), 0)) {
            returnObject.setMessage("Cannot update offer: payment already made for this offer.");
            returnObject.setStatus(false);
            returnObject.setData(null);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(returnObject);
        }

        // Update offer directly without SAP integration
        offer.setPaymentPlan(request.getPlan());
        offer.setMaintenancePlan(request.getMaintenance());
        offerRepository.save(offer);
        
        returnObject.setMessage(messageUtil.getMessage("offer.updated.successfully"));
        returnObject.setStatus(true);
        returnObject.setData(offer);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(returnObject);
    }



    @Transactional
    public ResponseEntity<ReturnObject<?>> updateOfferAfterPayment(
            UpdateOfferPaymentDTO request
    ) {
        System.out.println(">> [OfferService] Updating offer after payment");
        System.out.println(">> Request: " + request);

        Offer offer = offerRepository.findById(request.getOfferId())
                .orElseThrow(() ->
                        new EntityNotFoundException("Offer not found with id: " + request.getOfferId())
                );

        if (offer.getStatus() != OfferStatus.PROC) {
            return ResponseEntity.badRequest().body(new ReturnObject<>(
                    "Offer Cannot be updated because its not PROC",
                    false,
                    offer
            ));
        }

        System.out.println(">> Current paid amount (DB): " + offer.getPaidAmount());

        int currentPaid = 0;
        if (offer.getPaidAmount() != null) {
            currentPaid = (int) Double.parseDouble(offer.getPaidAmount());
        }

        int newPaidAmount = currentPaid + request.getPaidAmount();
        offer.setPaidAmount(String.valueOf(newPaidAmount));

        System.out.println(">> New paid amount: " + newPaidAmount);

        // 🔹 Status logic
        if ("BANK_TRANSFER".equals(request.getPaymentMethod())) {
            offer.setStatus(OfferStatus.BANK_TRANSFER_APPROVAL_PENDING);
            offer.setStatusTextEn("Bank transfer pending approval");
            offer.setStatusTextAr("تحويل بنكي قيد المراجعة");
        } else {
            offer.setStatus(OfferStatus.DOWN);
            offer.setStatusTextEn("Down payment received");
            offer.setStatusTextAr("تم استلام دفعة");
        }

        int reservationAmount = 0;
        if (offer.getReservationAmount() != null) {
            reservationAmount = (int) Double.parseDouble(offer.getReservationAmount());
        }

        System.out.println(">> Reservation amount: " + reservationAmount);

        if (newPaidAmount >= reservationAmount) {
            offer.setStatus(OfferStatus.SOLD);
            offer.setStatusTextEn("Offer sold");
            offer.setStatusTextAr("تم بيع العرض");
            System.out.println(">> Offer marked as SOLD");
        }

        offerRepository.saveAndFlush(offer); // 🔥 important
        System.out.println(">> Offer saved successfully");

        return ResponseEntity.ok(
                new ReturnObject<>(
                        "Offer updated successfully",
                        true,
                        offer
                )
        );
    }

    public ResponseEntity<ReturnObject<?>>
    getBankTransferOffers(int page, int size, OfferStatus status) {

        System.out.println("➡️ Fetching bank transfer offers");
        System.out.println("➡️ Page: " + page + ", Size: " + size);
        System.out.println("➡️ Status filter: " + status);

        Pageable pageable =
                PageRequest.of(page, size, Sort.by("id").descending());

        Page<Offer> offers;

    /* =========================
       1️⃣ Status Filtering
       ========================= */

        if (status != null) {

            offers =
                    offerRepository.findAllByStatus(
                            status,
                            pageable
                    );

            System.out.println("🔎 Filtering by single status: " + status);

        } else {

            List<OfferStatus> bankTransferStatuses = List.of(
                    OfferStatus.BANK_TRANSFER_APPROVAL_PENDING,
                    OfferStatus.FI_APPROVAL_PENDING,
                    OfferStatus.RELEASE_PENDING,
                    OfferStatus.BANK_TRANSFER_APPROVED
            );

            offers =
                    offerRepository.findAllByStatusIn(
                            bankTransferStatuses,
                            pageable
                    );

            System.out.println("🔎 Fetching all bank-transfer related statuses");
        }

        if (offers.isEmpty()) {
            System.out.println("⚠️ No bank transfer offers found");
            return ResponseEntity.ok(
                    new ReturnObject<>(
                            "No bank transfer offers found",
                            true,
                            Collections.emptyList()
                    )
            );
        }

        System.out.println("✅ Found offers count: " + offers.getTotalElements());

    /* =========================
       2️⃣ Extract IDs
       ========================= */

        List<Integer> offerIds =
                offers.stream()
                        .map(Offer::getId)
                        .toList();

        List<Integer> unitIds =
                offers.stream()
                        .map(Offer::getUnitId)
                        .filter(Objects::nonNull)
                        .distinct()
                        .toList();

        List<Integer> customerIds =
                offers.stream()
                        .map(Offer::getCustomerId)
                        .filter(Objects::nonNull)
                        .distinct()
                        .toList();

        System.out.println("➡️ Offer IDs: " + offerIds);
        System.out.println("➡️ Unit IDs: " + unitIds);
        System.out.println("➡️ Customer IDs: " + customerIds);

    /* =========================
       3️⃣ Payment MS
       ========================= */

        BankTransferByOfferIdsRequest paymentRequest =
                new BankTransferByOfferIdsRequest();
        paymentRequest.setOfferIds(offerIds);

        ResponseEntity<ReturnObject<List<BankTransferByOfferResponse>>>
                paymentResponse =
                paymentFeignClient.getBankTransfersByOfferIds(paymentRequest);

        Map<Integer, BankTransferResponse> transferMap = new HashMap<>();

        if (paymentResponse.getBody() != null
                && paymentResponse.getBody().getData() != null) {

            paymentResponse.getBody().getData()
                    .forEach(bt ->
                            transferMap.put(
                                    bt.getOfferId(),
                                    bt.getBankTransfer()
                            )
                    );
        }

        System.out.println("✅ Bank transfers fetched: " + transferMap.size());

    /* =========================
       4️⃣ Inventory MS
       ========================= */

        Map<Integer, UnitResponseDTO> unitMap = new HashMap<>();

        if (!unitIds.isEmpty()) {

            List<String> unitIdStrings =
                    unitIds.stream()
                            .map(String::valueOf)
                            .toList();

            ResponseEntity<ReturnObject<List<UnitResponseDTO>>> unitResponse =
                    inventoryFeignClient.getUnitsByIds(unitIdStrings);

            if (unitResponse.getBody() != null
                    && unitResponse.getBody().getData() != null) {

                unitResponse.getBody().getData()
                        .forEach(unit ->
                                unitMap.put(unit.getId(), unit)
                        );
            }
        }

        System.out.println("✅ Units fetched: " + unitMap.size());

    /* =========================
       5️⃣ Customer MS
       ========================= */

        Map<Integer, CustomerResponseDTO> customerMap = new HashMap<>();

        if (!customerIds.isEmpty()) {

            ResponseEntity<ReturnObject<List<CustomerResponseDTO>>> customerResponse =
                    customerFeignClient.getCustomersByIds(
                            customerIds,
                            null,
                            null
                    );

            if (customerResponse.getBody() != null
                    && customerResponse.getBody().getData() != null) {

                customerResponse.getBody().getData()
                        .forEach(customer ->
                                customerMap.put(
                                        customer.getCustomerId(),
                                        customer
                                )
                        );
            }
        }

        System.out.println("✅ Customers fetched: " + customerMap.size());

    /* =========================
       6️⃣ Build Response
       ========================= */

        List<PendingBankTransferOfferDTO> response =
                offers.stream().map(o -> {

                    PendingBankTransferOfferDTO dto =
                            new PendingBankTransferOfferDTO();

                    dto.setOfferId(o.getId());
                    dto.setOfferNumber(
                            "FAIL".equals(o.getSapOfferNumber())
                                    ? String.valueOf(o.getId())
                                    : o.getSapOfferNumber()
                    );

                    dto.setUnitId(o.getUnitId());
                    dto.setCustomerId(o.getCustomerId());
                    dto.setStatus(o.getStatus());

                    dto.setBankTransfer(
                            transferMap.get(o.getId())
                    );

                    dto.setUnit(
                            unitMap.get(o.getUnitId())
                    );

                    dto.setCustomer(
                            customerMap.get(o.getCustomerId())
                    );

                    return dto;
                }).toList();

        System.out.println("✅ Final response size: " + response.size());

        return ResponseEntity.ok(
                new ReturnObject<>(
                        "Bank transfer offers fetched successfully",
                        true,
                        response
                )
        );
    }

    public ResponseEntity<ReturnObject<?>>
    getBankTransferDetails(Integer bankTransferId) {

        System.out.println("➡️ Fetching bank transfer details for ID: " + bankTransferId);

    /* =========================
       1️⃣ Payment MS
       ========================= */

        ResponseEntity<ReturnObject<BankTransferResponse>> paymentResponse =
                paymentFeignClient.getBankTransferById(bankTransferId);

        if (paymentResponse.getBody() == null
                || paymentResponse.getBody().getData() == null) {

            System.out.println("❌ Bank transfer not found in Payment MS");

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new ReturnObject<>(
                            "Bank transfer not found",
                            false,
                            null
                    )
            );
        }

        BankTransferResponse bankTransfer =
                paymentResponse.getBody().getData();

        Integer offerId = bankTransfer.getOfferId();

        System.out.println("✅ Bank transfer belongs to offerId: " + offerId);

    /* =========================
       2️⃣ Offer
       ========================= */

        Offer offer =
                offerRepository.findById(offerId)
                        .orElseThrow(() ->
                                new EntityNotFoundException(
                                        "Offer not found with id: " + offerId
                                )
                        );

    /* =========================
       3️⃣ Unit
       ========================= */

        UnitResponseDTO unit = null;

        if (offer.getUnitId() != null) {

            ResponseEntity<ReturnObject<List<UnitResponseDTO>>> unitResponse =
                    inventoryFeignClient.getUnitsByIds(
                            List.of(String.valueOf(offer.getUnitId()))
                    );

            if (unitResponse.getBody() != null
                    && unitResponse.getBody().getData() != null
                    && !unitResponse.getBody().getData().isEmpty()) {

                unit = unitResponse.getBody().getData().get(0);
            }
        }

    /* =========================
       4️⃣ Customer
       ========================= */

        CustomerResponseDTO customer = null;

        if (offer.getCustomerId() != null) {

            ResponseEntity<ReturnObject<List<CustomerResponseDTO>>> customerResponse =
                    customerFeignClient.getCustomersByIds(
                            List.of(offer.getCustomerId()),
                            null,
                            null
                    );

            if (customerResponse.getBody() != null
                    && customerResponse.getBody().getData() != null
                    && !customerResponse.getBody().getData().isEmpty()) {

                customer = customerResponse.getBody().getData().get(0);
            }
        }

    /* =========================
       5️⃣ Build Response
       ========================= */

        PendingBankTransferOfferDTO dto =
                new PendingBankTransferOfferDTO();

        dto.setOfferId(offer.getId());
        dto.setOfferNumber(
                "FAIL".equals(offer.getSapOfferNumber())
                        ? String.valueOf(offer.getId())
                        : offer.getSapOfferNumber()
        );

        dto.setUnitId(offer.getUnitId());
        dto.setCustomerId(offer.getCustomerId());
        dto.setStatus(offer.getStatus());

        dto.setBankTransfer(bankTransfer);
        dto.setUnit(unit);
        dto.setCustomer(customer);

        System.out.println("✅ Bank transfer details response built successfully");

        return ResponseEntity.ok(
                new ReturnObject<>(
                        "Bank transfer details fetched successfully",
                        true,
                        dto
                )
        );
    }


    /* ===============================
       1️⃣ Admin Approval
       =============================== */
    @Transactional
    public ResponseEntity<ReturnObject<?>>
    approveBankTransfer(Integer offerId, Integer adminUserId) {

        System.out.println("➡️ Admin approval started for offer: " + offerId);

        Offer offer = offerRepository.findById(offerId)
                .orElseThrow(() ->
                        new RuntimeException("Offer not found"));

        if (!OfferStatus.BANK_TRANSFER_APPROVAL_PENDING
                .equals(offer.getStatus())) {

            System.out.println("❌ Offer is not pending admin approval");
            throw new RuntimeException("Invalid offer status");
        }

        offer.setApproveFlag("DONE");
        offer.setApproveUser(adminUserId);
        offer.setApproveDatetime(Timestamp.from(Instant.now()));

        offer.setStatus(OfferStatus.FI_APPROVAL_PENDING);

        offerRepository.save(offer);

        System.out.println("✅ Admin approval completed");

        return ResponseEntity.ok(
                new ReturnObject<>(
                        "Bank transfer approved by admin",
                        true,
                        null
                )
        );
    }

    /* ===============================
       2️⃣ FI Approval
       =============================== */
    @Transactional
    public ResponseEntity<ReturnObject<?>>
    fiApproveBankTransfer(Integer offerId, Integer fiUserId) {

        System.out.println("➡️ FI approval started for offer: " + offerId);

        Offer offer = offerRepository.findById(offerId)
                .orElseThrow(() ->
                        new RuntimeException("Offer not found"));

        if (!"DONE".equals(offer.getApproveFlag())) {
            System.out.println("❌ Admin approval not done yet");
            throw new RuntimeException("Admin approval required first");
        }

        if (!OfferStatus.FI_APPROVAL_PENDING
                .equals(offer.getStatus())) {

            System.out.println("❌ Offer is not pending FI approval");
            throw new RuntimeException("Invalid offer status");
        }

        offer.setFiApproveFlag("DONE");
        offer.setFiApproveUser(fiUserId);
        offer.setFiApproveDatetime(Timestamp.from(Instant.now()));

        offer.setStatus(OfferStatus.RELEASE_PENDING);

        offerRepository.save(offer);

        System.out.println("✅ FI approval completed");

        return ResponseEntity.ok(
                new ReturnObject<>(
                        "Bank transfer approved by finance",
                        true,
                        null
                )
        );
    }

    /* ===============================
       3️⃣ Release Approval
       =============================== */
    @Transactional
    public ResponseEntity<ReturnObject<?>>
    releaseBankTransfer(Integer offerId, Integer releaseUserId) {

        System.out.println("➡️ Release started for offer: " + offerId);

        Offer offer = offerRepository.findById(offerId)
                .orElseThrow(() ->
                        new RuntimeException("Offer not found"));

        if (!"DONE".equals(offer.getFiApproveFlag())) {
            System.out.println("❌ FI approval not done yet");
            throw new RuntimeException("FI approval required first");
        }

        if (!OfferStatus.RELEASE_PENDING
                .equals(offer.getStatus())) {

            System.out.println("❌ Offer is not pending release");
            throw new RuntimeException("Invalid offer status");
        }

        offer.setReleaseFlag("DONE");
        offer.setReleaseUser(releaseUserId);
        offer.setReleaseDatetime(Timestamp.from(Instant.now()));

        offer.setStatus(OfferStatus.BANK_TRANSFER_APPROVED);

        offerRepository.save(offer);

        System.out.println("✅ Bank transfer fully approved");

        return ResponseEntity.ok(
                new ReturnObject<>(
                        "Bank transfer released successfully",
                        true,
                        null
                )
        );
    }


}


