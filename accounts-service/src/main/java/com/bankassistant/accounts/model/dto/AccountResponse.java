package com.bankassistant.accounts.model.dto;

import com.bankassistant.accounts.model.entity.AccountType;

/** One account in GET /accounts; balance is a decimal string such as "1234.56". */
public record AccountResponse(String id, String name, AccountType type, String currency, String balance) {
}
