package com.example.stockapp;

public class WatchModel {

    String companyTicker;
    String companyName;
    String companyC;
    String companyD;
    String companyDP;

    public WatchModel(String companyTicker, String companyName, String companyC, String companyD, String companyDP) {
        this.companyTicker = companyTicker;
        this.companyName = companyName;
        this.companyC = companyC;
        this.companyD = companyD;
        this.companyDP = companyDP;
    }

    public String getCompanyTicker() {
        return companyTicker;
    }

    public String getCompanyName() {
        return companyName;
    }

    public String getCompanyC() {
        return companyC;
    }

    public String getCompanyD() {
        return companyD;
    }

    public String getCompanyDP() {
        return companyDP;
    }
}
