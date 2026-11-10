package com.bankassistant.accounts.controller;

import java.time.LocalDate;
import java.util.List;

import com.bankassistant.accounts.model.dto.AccountResponse;
import com.bankassistant.accounts.model.dto.BalanceResponse;
import com.bankassistant.accounts.model.dto.TransactionResponse;
import com.bankassistant.accounts.service.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** The caller is identified by X-User-Id, which only the gateway sets (from the validated token). */
@Slf4j
@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {

    public static final String USER_HEADER = "X-User-Id";

    private final AccountService accountService;

    @GetMapping
    public List<AccountResponse> getAccounts(@RequestHeader(USER_HEADER) String customerId) {
        log.info("GET /accounts customerId={}", customerId);
        return accountService.getAccounts(customerId);
    }

    @GetMapping("/{accountId}/balance")
    public BalanceResponse getBalance(@RequestHeader(USER_HEADER) String customerId,
                                      @PathVariable String accountId) {
        log.info("GET /accounts/{}/balance customerId={}", accountId, customerId);
        return accountService.getBalance(customerId, accountId);
    }

    @GetMapping("/{accountId}/transactions")
    public List<TransactionResponse> getTransactions(
            @RequestHeader(USER_HEADER) String customerId,
            @PathVariable String accountId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String category) {
        log.info("GET /accounts/{}/transactions customerId={} from={} to={} category={}",
                accountId, customerId, from, to, category);
        return accountService.getTransactions(customerId, accountId, from, to, category);
    }
}
