package com.resale.homeflyoffer.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "payment")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private Integer offerId;
    private Integer amount;
    private Integer method;
    private Integer status;
    private LocalDateTime paidAt;
    @Column(length = 100)
    private String sapFiDocument;
    @Column(length = 255)
    private String bankResponse;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}




