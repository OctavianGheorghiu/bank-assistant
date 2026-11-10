package com.bankassistant.accounts.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** A customer's account. The balance is stored; it must change in the same transaction as each ledger row. */
@Entity
@Table(name = "account")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Account {

    @Id
    @Column(length = 64)
    private String id;

    /** The Keycloak username, passed on by the gateway as X-User-Id. */
    @Column(length = 64)
    private String customerId;

    @Enumerated(EnumType.STRING)
    @Column(length = 16)
    private AccountType type;

    @Column(length = 100)
    private String name;

    @Column(length = 3)
    private String currency;

    /** Cents; equals the sum of the account's transactions. */
    private long balanceMinor;
}
