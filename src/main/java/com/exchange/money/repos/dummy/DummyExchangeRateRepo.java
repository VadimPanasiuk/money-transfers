package com.exchange.money.repos.dummy;

import com.exchange.money.repos.ExchangeRateRepo;

import java.math.BigDecimal;
import java.util.Currency;

class DummyExchangeRateRepo implements ExchangeRateRepo {
    public BigDecimal getExchangeRate(Currency src, Currency dest) {
        return BigDecimal.ONE;
    }

}
