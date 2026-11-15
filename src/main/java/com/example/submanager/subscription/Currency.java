package com.example.submanager.subscription;

import java.math.BigDecimal;

/** 手動管理の固定レート(1単位あたりの円)。必要に応じて編集する。 */
public enum Currency {
    JPY("¥", 0, BigDecimal.ONE),
    USD("$", 2, new BigDecimal("150")),
    EUR("€", 2, new BigDecimal("163"));

    private final String symbol;
    private final int fractionDigits;
    private final BigDecimal jpyRate;

    Currency(String symbol, int fractionDigits, BigDecimal jpyRate) {
        this.symbol = symbol;
        this.fractionDigits = fractionDigits;
        this.jpyRate = jpyRate;
    }

    public String getSymbol() { return symbol; }
    public int getFractionDigits() { return fractionDigits; }
    public BigDecimal getJpyRate() { return jpyRate; }
}
