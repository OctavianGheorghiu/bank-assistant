package com.bankassistant.accounts.model.dto;

import java.util.List;

/** GET /accounts/{accountId}/transactions; truncated is true when more rows matched than the limit allowed. */
public record TransactionsResponse(List<TransactionResponse> transactions, boolean truncated) {
}
