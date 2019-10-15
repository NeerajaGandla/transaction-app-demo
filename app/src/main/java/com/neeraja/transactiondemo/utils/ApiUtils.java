package com.neeraja.transactiondemo.utils;

public class ApiUtils {
    public static String baseUrl = "https://interviewer-api.herokuapp.com/";
    public static String loginTag = "login";
    public static String spendTag = "spend";
    public static String transactionsTag = "transactions";
    public static String balanceTag = "balance";

    public static String getLoginUrl() {
        return baseUrl + loginTag;
    }

    public static String getSpendUrl() {
        return baseUrl + spendTag;
    }

    public static String getBalanceUrl() {
        return baseUrl + balanceTag;
    }

    public static String getTransactionsUrl() {
        return baseUrl + transactionsTag;
    }
}
