package com.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AccountControllerTest {

    @Autowired
    MockMvc mockMvc;

    // Create Rahul and Priya before each test
    @BeforeEach
    void setup() throws Exception {
        System.out.println("\n--- Setting up test accounts: Rahul (1000) and Priya (500) ---");

        mockMvc.perform(post("/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"owner": "Rahul", "balance": 1000}
                """));

        mockMvc.perform(post("/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"owner": "Priya", "balance": 500}
                """));

        System.out.println("--- Setup complete ---\n");
    }

    @Test
    void createAccount_success() throws Exception {
        System.out.println("TEST: Creating a new account for Anjali with balance 300...");

        mockMvc.perform(post("/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"owner": "Anjali", "balance": 300}
                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.owner").value("Anjali"))
                .andExpect(jsonPath("$.balance").value(300));

        System.out.println("PASS: Account created successfully — owner is Anjali, balance is 300");
    }

    @Test
    void createAccount_missingOwner_returns400() throws Exception {
        System.out.println("TEST: Trying to create an account with no owner name...");

        mockMvc.perform(post("/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"balance": 100}
                """))
                .andExpect(status().isBadRequest());

        System.out.println("PASS: Correctly rejected with 400 Bad Request — owner is required");
    }

    @Test
    void createAccount_negativeBalance_returns400() throws Exception {
        System.out.println("TEST: Trying to create an account with negative balance -100...");

        mockMvc.perform(post("/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"owner": "Vikram", "balance": -100}
                """))
                .andExpect(status().isBadRequest());

        System.out.println("PASS: Correctly rejected with 400 Bad Request — balance cannot be negative");
    }

    @Test
    void getAllAccounts_returnsList() throws Exception {
        System.out.println("TEST: Fetching all accounts — expecting a list with Rahul and Priya...");

        mockMvc.perform(get("/accounts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        System.out.println("PASS: Got 200 OK with a valid JSON array of accounts");
    }

    @Test
    void getAccount_notFound_returns404() throws Exception {
        System.out.println("TEST: Fetching account with ID 9999 which does not exist...");

        mockMvc.perform(get("/accounts/9999"))
                .andExpect(status().isNotFound());

        System.out.println("PASS: Correctly returned 404 Not Found for unknown account");
    }

    @Test
    void transfer_success_updatesBalances() throws Exception {
        System.out.println("TEST: Transferring 200 from Rahul (1000) to Priya (500)...");

        mockMvc.perform(post("/transfers")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"fromAccountId": 1, "toAccountId": 2, "amount": 200}
                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Transfer successful"));

        System.out.println("PASS: Transfer successful — Rahul now has 800, Priya now has 700");
    }

    @Test
    void transfer_insufficientFunds_returns409() throws Exception {
        System.out.println("TEST: Transferring 9999 from Rahul who only has 1000...");

        mockMvc.perform(post("/transfers")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"fromAccountId": 1, "toAccountId": 2, "amount": 9999}
                """))
                .andExpect(status().isConflict());

        System.out.println("PASS: Correctly rejected with 409 Conflict — insufficient funds");
    }

    @Test
    void transfer_sameAccount_returns400() throws Exception {
        System.out.println("TEST: Trying to transfer 100 from Rahul to Rahul (same account)...");

        mockMvc.perform(post("/transfers")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"fromAccountId": 1, "toAccountId": 1, "amount": 100}
                """))
                .andExpect(status().isBadRequest());

        System.out.println("PASS: Correctly rejected with 400 Bad Request — cannot transfer to same account");
    }

    @Test
    void transfer_unknownAccount_returns404() throws Exception {
        System.out.println("TEST: Transferring from account ID 9999 which does not exist...");

        mockMvc.perform(post("/transfers")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"fromAccountId": 9999, "toAccountId": 2, "amount": 100}
                """))
                .andExpect(status().isNotFound());

        System.out.println("PASS: Correctly returned 404 Not Found — source account does not exist");
    }

    @Test
    void transfer_zeroAmount_returns400() throws Exception {
        System.out.println("TEST: Trying to transfer amount 0 from Rahul to Priya...");

        mockMvc.perform(post("/transfers")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"fromAccountId": 1, "toAccountId": 2, "amount": 0}
                """))
                .andExpect(status().isBadRequest());

        System.out.println("PASS: Correctly rejected with 400 Bad Request — amount must be positive");
    }
}