package com.bankassistant.accounts.model.dto;

import java.time.OffsetDateTime;

/** One entry of GET /accounts/{accountId}/transactions; amount is a decimal string, negative for money out. */
public record TransactionResponse(long id, OffsetDateTime bookedAt, String amount, String currency,
                                  String category, String description, String counterparty) {
}
