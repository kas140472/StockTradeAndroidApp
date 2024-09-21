package com.example.stockapp;

public class PortfolioItemModel {

    String companyTicker;
    String companyShares;
    String companyMarketValue;
    String companyChange;
    String companyChangePercent;

    public PortfolioItemModel(String companyTicker, String companyShares, String companyMarketValue, String companyChange, String companyChangePercent) {
        this.companyTicker = companyTicker;
        this.companyShares = companyShares;
        this.companyMarketValue = companyMarketValue;
        this.companyChange = companyChange;
        this.companyChangePercent = companyChangePercent;
    }

    public String getCompanyTicker() {
        return companyTicker;
    }

    public String getCompanyShares() {
        return companyShares;
    }

    public String getCompanyMarketValue() {
        return companyMarketValue;
    }

    public String getCompanyChange() {
        return companyChange;
    }

    public String getCompanyChangePercent() {
        return companyChangePercent;
    }
}

