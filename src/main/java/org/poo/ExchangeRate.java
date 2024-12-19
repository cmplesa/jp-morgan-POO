package org.poo;

import lombok.Data;

@Data
public class ExchangeRate {
    private String currencyFrom;
    private String currencyTo;
    private double rate;

    public ExchangeRate(String currencyFrom, String currencyTo, double rate) {
        this.currencyFrom = currencyFrom;
        this.currencyTo = currencyTo;
        this.rate = rate;
    }


}
