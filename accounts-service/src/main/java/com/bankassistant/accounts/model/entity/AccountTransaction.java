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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "account_transaction")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AccountTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String accountId;

    private OffsetDateTime bookedAt;

    /** Cents; negative means money out. */
    private long amountMinor;

    @Column(length = 3)
    @JdbcTypeCode(SqlTypes.CHAR) // the column is CHAR(3); Hibernate assumes VARCHAR for String
    private String currency;

    private String category;

    private String description;

    private String counterparty;
}
