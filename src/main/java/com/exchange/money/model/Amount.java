package com.exchange.money.model;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.exchange.money.utils.Assert;

import java.math.BigDecimal;
import java.util.Currency;

public class Amount {

    private BigDecimal value;
    private Currency currency;

    Amount() {
        // empty constructor for Jackson
    }

    public Amount(BigDecimal value, Currency currency) {
        Assert.checkNotNull(value, "Value can not be null");
        Assert.checkNotNull(currency, "Currency can not be null");
        Assert.checkIsTrue(value.compareTo(BigDecimal.ZERO) >= 0, "Cannot initialize amount to negative value");
        this.value = value;
        this.currency = currency;
    }

    public BigDecimal getValue() {
        return value;
    }

    public Currency getCurrency() {
        return currency;
    }

    public Amount add(Amount amount) {
        Assert.checkIsTrue(amount.getCurrency().equals(currency), "Can not add amounts with different currencies");
        return new Amount(amount.value.add(value), currency);
    }

    public Amount subtract(Amount amount) {
        Assert.checkIsTrue(amount.currency.equals(currency), "Can not subtract amounts with different currencies");
        Assert.checkIsTrue(amount.value.compareTo(value) <= 0, "Subtract smaller amounts only");
        return new Amount(value.subtract(amount.value), currency);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Amount amount = (Amount) o;
        return Objects.equal(value, amount.value) &&
                Objects.equal(currency, amount.currency);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value, currency);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("value", value)
                .add("currency", currency)
                .toString();
    }
}
