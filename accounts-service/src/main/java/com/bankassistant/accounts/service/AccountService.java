package com.bankassistant.accounts.service;

import static com.bankassistant.accounts.repository.AccountTransactionSpecifications.bookedAtOrAfter;
import static com.bankassistant.accounts.repository.AccountTransactionSpecifications.bookedBefore;
import static com.bankassistant.accounts.repository.AccountTransactionSpecifications.inCategory;
import static com.bankassistant.accounts.repository.AccountTransactionSpecifications.ofAccount;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import com.bankassistant.accounts.exception.AccountNotFoundException;
import com.bankassistant.accounts.exception.InvalidFilterException;
import com.bankassistant.accounts.model.dto.AccountResponse;
import com.bankassistant.accounts.model.dto.BalanceResponse;
import com.bankassistant.accounts.model.dto.TransactionResponse;
import com.bankassistant.accounts.model.dto.TransactionsResponse;
import com.bankassistant.accounts.model.entity.Account;
import com.bankassistant.accounts.model.entity.AccountTransaction;
import com.bankassistant.accounts.repository.AccountRepository;
import com.bankassistant.accounts.repository.AccountTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Accounts, balances and transactions of one customer; another customer's account is "not found". */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccountService {

    private static final Sort NEWEST_FIRST = Sort.by(Sort.Direction.DESC, "bookedAt", "id");
    private static final int MAX_LIMIT = 1000;

    private final AccountRepository accountRepository;
    private final AccountTransactionRepository transactionRepository;

    public List<AccountResponse> getAccounts(String customerId) {
        log.debug("getAccounts customerId={}", customerId);
        return accountRepository.findByCustomerIdOrderById(customerId).stream()
                .map(account -> new AccountResponse(account.getId(), account.getName(), account.getType(),
                        account.getCurrency(), toDecimal(account.getBalanceMinor())))
                .toList();
    }

    public BalanceResponse getBalance(String customerId, String accountId) {
        log.debug("getBalance customerId={} accountId={}", customerId, accountId);
        Account account = findOwnedAccount(customerId, accountId);
        return new BalanceResponse(account.getId(), toDecimal(account.getBalanceMinor()), account.getCurrency());
    }

    /**
     * Newest first, at most {@code limit} rows. Dates are UTC days; from and to are inclusive; every filter is
     * optional. truncated tells the caller that more rows matched, so a sum over the list would be incomplete.
     */
    public TransactionsResponse getTransactions(String customerId, String accountId,
                                                LocalDate from, LocalDate to, String category, int limit) {
        log.debug("getTransactions customerId={} accountId={} from={} to={} category={} limit={}",
                customerId, accountId, from, to, category, limit);
        if (limit < 1 || limit > MAX_LIMIT) {
            throw new InvalidFilterException("limit must be between 1 and " + MAX_LIMIT);
        }
        if (from != null && to != null && from.isAfter(to)) {
            throw new InvalidFilterException("from must not be after to");
        }
        findOwnedAccount(customerId, accountId);

        Specification<AccountTransaction> search = ofAccount(accountId);
        if (from != null) {
            search = search.and(bookedAtOrAfter(from.atStartOfDay().atOffset(ZoneOffset.UTC)));
        }
        if (to != null) {
            search = search.and(bookedBefore(to.plusDays(1).atStartOfDay().atOffset(ZoneOffset.UTC)));
        }
        if (category != null) {
            search = search.and(inCategory(category));
        }
        // One extra row tells whether more matched, without a count query
        List<AccountTransaction> rows = transactionRepository.findBy(search,
                query -> query.sortBy(NEWEST_FIRST).limit(limit + 1).all());
        List<TransactionResponse> transactions = rows.stream()
                .limit(limit)
                .map(t -> new TransactionResponse(t.getId(), t.getBookedAt(), toDecimal(t.getAmountMinor()),
                        t.getCurrency(), t.getCategory(), t.getDescription(), t.getCounterparty()))
                .toList();
        return new TransactionsResponse(transactions, rows.size() > limit);
    }

    private Account findOwnedAccount(String customerId, String accountId) {
        return accountRepository.findByIdAndCustomerId(accountId, customerId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));
    }

    // shortcut: assumes 2 decimal places (EUR only); look up the currency's digits if other currencies appear
    private static String toDecimal(long minor) {
        return BigDecimal.valueOf(minor, 2).toPlainString();
    }
}
