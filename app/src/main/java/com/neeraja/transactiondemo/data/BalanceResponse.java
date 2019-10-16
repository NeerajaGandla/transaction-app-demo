package com.neeraja.transactiondemo.data;

import com.google.gson.Gson;

public class BalanceResponse {
    private String balance;
    private String currency;

    public String getBalance() {
        return balance;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
