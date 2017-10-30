package com.exchange.money.repos;

import java.math.BigDecimal;
import java.util.Currency;

public interface ExchangeRateRepo {
    BigDecimal getExchangeRate(Currency src, Currency dest);
}
