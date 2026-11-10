package com.bankassistant.accounts.model.entity;

import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "account_transaction")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AccountTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 64)
    private String accountId;

    private OffsetDateTime bookedAt;

    /** Cents; negative means money out. */
    private long amountMinor;

    @Column(length = 3)
    private String currency;

    @Column(length = 32)
    private String category;

    private String description;

    @Column(length = 100)
    private String counterparty;
}
