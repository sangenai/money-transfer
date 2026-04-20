package com.example;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@RestController
public class AccountController {

    // ---------- In-memory store ----------
    private final Map<Long, Account> accounts = new ConcurrentHashMap<>();
    private final AtomicLong idCounter = new AtomicLong(1);

    // ---------- Request bodies ----------
    record CreateAccountRequest(String owner, BigDecimal balance) {}
    record TransferRequest(Long fromAccountId, Long toAccountId, BigDecimal amount) {}

    // ---------- Endpoints ----------

    // GET /accounts — list all accounts
    @GetMapping("/accounts")
    public Collection<Account> getAllAccounts() {
        return accounts.values();
    }

    // GET /accounts/{id} — get one account
    @GetMapping("/accounts/{id}")
    public ResponseEntity<?> getAccount(@PathVariable Long id) {
        Account account = accounts.get(id);
        if (account == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(account);
    }

    // POST /accounts — create an account
    @PostMapping("/accounts")
    public ResponseEntity<?> createAccount(@RequestBody CreateAccountRequest request) {
        if (request.owner() == null || request.owner().isBlank()) {
            return ResponseEntity.badRequest().body("Owner is required");
        }
        if (request.balance() == null || request.balance().compareTo(BigDecimal.ZERO) < 0) {
            return ResponseEntity.badRequest().body("Balance must be zero or positive");
        }

        Long id = idCounter.getAndIncrement();
        Account account = new Account(id, request.owner(), request.balance());
        accounts.put(id, account);
        return ResponseEntity.status(201).body(account);
    }

    // POST /transfers — transfer money between accounts
    @PostMapping("/transfers")
    public ResponseEntity<?> transfer(@RequestBody TransferRequest request) {
        if (request.amount() == null || request.amount().compareTo(BigDecimal.ZERO) <= 0) {
            return ResponseEntity.badRequest().body("Amount must be positive");
        }
        if (request.fromAccountId().equals(request.toAccountId())) {
            return ResponseEntity.badRequest().body("Cannot transfer to the same account");
        }

        Account from = accounts.get(request.fromAccountId());
        Account to   = accounts.get(request.toAccountId());

        if (from == null) return ResponseEntity.status(404).body("Source account not found");
        if (to   == null) return ResponseEntity.status(404).body("Destination account not found");

        // Lock both accounts in ID order to prevent deadlock
        Account first  = from.getId() < to.getId() ? from : to;
        Account second = from.getId() < to.getId() ? to   : from;

        synchronized (first) {
            synchronized (second) {
                if (from.getBalance().compareTo(request.amount()) < 0) {
                    return ResponseEntity.status(409).body("Insufficient funds");
                }
                from.setBalance(from.getBalance().subtract(request.amount()));
                to.setBalance(to.getBalance().add(request.amount()));
            }
        }

        return ResponseEntity.ok(Map.of(
            "message",     "Transfer successful",
            "transferred", request.amount(),
            "from",        from,
            "to",          to
        ));
    }
}
