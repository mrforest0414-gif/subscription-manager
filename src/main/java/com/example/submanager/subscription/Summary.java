package com.example.submanager.subscription;

import java.math.BigDecimal;
import java.util.List;

/** ダッシュボードの集計結果(金額はすべて円換算・整数に丸め済み) */
public record Summary(
        BigDecimal monthlyTotal,
        BigDecimal yearlyForecast,
        int activeCount,
        int upcomingCount,
        List<CategoryShare> byCategory) {

    public record CategoryShare(Category category, BigDecimal amount, int percent) {
    }
}
