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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** A customer's account. Its balance is not stored: it is the sum of its transactions. */
@Entity
@Table(name = "account")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Account {

    @Id
    private String id;

    /** The Keycloak username, passed on by the gateway as X-User-Id. */
    private String customerId;

    @Enumerated(EnumType.STRING)
    private AccountType type;

    private String name;

    @Column(length = 3)
    @JdbcTypeCode(SqlTypes.CHAR) // the column is CHAR(3); Hibernate assumes VARCHAR for String
    private String currency;
}
