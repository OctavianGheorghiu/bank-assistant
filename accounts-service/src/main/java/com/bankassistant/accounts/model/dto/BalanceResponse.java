package com.bankassistant.accounts.model.dto;

/** GET /accounts/{accountId}/balance; amount is a decimal string such as "1234.56". */
public record BalanceResponse(String accountId, String amount, String currency) {
}
