package com.example.submanager.subscription;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class SubscriptionTest {

    static Subscription sub(String price, Currency cur, BillingCycle cycle, LocalDate renewal, PaymentStatus status) {
        Subscription s = new Subscription();
        s.setName("x");
        s.setPrice(new BigDecimal(price));
        s.setCurrency(cur);
        s.setBillingCycle(cycle);
        s.setCategory(Category.OTHER);
        s.setStartDate(LocalDate.of(2024, 1, 1));
        s.setNextRenewalDate(renewal);
        s.setPaymentStatus(status);
        return s;
    }

    @Test
    void monthlyPlanKeepsPrice() {
        Subscription s = sub("980", Currency.JPY, BillingCycle.MONTHLY, LocalDate.now(), PaymentStatus.ACTIVE);
        assertThat(s.getMonthlyJpy().intValue()).isEqualTo(980);
    }

    @Test
    void yearlyPlanIsDividedByTwelveAndConverted() {
        Subscription s = sub("120", Currency.USD, BillingCycle.YEARLY, LocalDate.now(), PaymentStatus.ACTIVE);
        // 120 USD / 12 * 150 = 1500
        assertThat(s.getMonthlyJpy().intValue()).isEqualTo(1500);
    }

    @Test
    void pastRenewalDateRollsForwardByCycle() {
        LocalDate today = LocalDate.of(2026, 5, 20);
        Subscription monthly = sub("100", Currency.JPY, BillingCycle.MONTHLY, LocalDate.of(2026, 3, 31), PaymentStatus.ACTIVE);
        Subscription yearly = sub("100", Currency.JPY, BillingCycle.YEARLY, LocalDate.of(2024, 6, 1), PaymentStatus.ACTIVE);

        assertThat(monthly.effectiveRenewalDate(today)).isEqualTo(LocalDate.of(2026, 5, 31));
        assertThat(yearly.effectiveRenewalDate(today)).isEqualTo(LocalDate.of(2026, 6, 1));
        assertThat(yearly.daysUntilRenewal(today)).isEqualTo(12);
    }

    @Test
    void futureRenewalDateIsUnchanged() {
        LocalDate today = LocalDate.of(2026, 5, 20);
        Subscription s = sub("100", Currency.JPY, BillingCycle.MONTHLY, today, PaymentStatus.ACTIVE);
        assertThat(s.effectiveRenewalDate(today)).isEqualTo(today);
        assertThat(s.daysUntilRenewal(today)).isZero();
    }

    @Test
    void cancelledIsNotCounted() {
        assertThat(sub("1", Currency.JPY, BillingCycle.MONTHLY, LocalDate.now(), PaymentStatus.CANCELLED).isCounted()).isFalse();
        assertThat(sub("1", Currency.JPY, BillingCycle.MONTHLY, LocalDate.now(), PaymentStatus.TRIAL).isCounted()).isTrue();
    }

    @Test
    void priceLabelUsesCurrencyFormat() {
        assertThat(sub("1490", Currency.JPY, BillingCycle.MONTHLY, LocalDate.now(), PaymentStatus.ACTIVE).getPriceLabel()).isEqualTo("¥1,490");
        assertThat(sub("20", Currency.USD, BillingCycle.MONTHLY, LocalDate.now(), PaymentStatus.ACTIVE).getPriceLabel()).isEqualTo("$20.00");
    }
}
