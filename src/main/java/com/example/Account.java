package com.example;

import java.math.BigDecimal;

public class Account {

    private Long id;
    private String owner;
    private BigDecimal balance;

    public Account(Long id, String owner, BigDecimal balance) {
        this.id = id;
        this.owner = owner;
        this.balance = balance;
    }

    public Long getId() { return id; }
    public String getOwner() { return owner; }
    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }
}
