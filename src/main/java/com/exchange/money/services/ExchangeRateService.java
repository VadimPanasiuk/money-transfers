package com.exchange.money.services;

import com.exchange.money.model.Amount;

import java.util.Currency;

public interface ExchangeRateService {

    Amount convert(Amount amount, Currency target);

}
