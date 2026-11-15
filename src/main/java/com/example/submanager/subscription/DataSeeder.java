package com.example.submanager.subscription;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

/** DB が空のときだけ動作確認用のサンプルを投入する。 */
@Component
public class DataSeeder implements CommandLineRunner {

    private final SubscriptionRepository repository;

    public DataSeeder(SubscriptionRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) {
        if (repository.count() > 0) {
            return;
        }
        LocalDate today = LocalDate.now();
        repository.save(sample("Netflix", "1490", Currency.JPY, BillingCycle.MONTHLY, Category.VIDEO,
                LocalDate.of(2023, 4, 1), today.plusDays(3), PaymentStatus.ACTIVE, "https://www.netflix.com", ""));
        repository.save(sample("Spotify", "980", Currency.JPY, BillingCycle.MONTHLY, Category.MUSIC,
                LocalDate.of(2022, 10, 15), today.plusDays(12), PaymentStatus.ACTIVE, "https://www.spotify.com", ""));
        repository.save(sample("Adobe Creative Cloud", "72336", Currency.JPY, BillingCycle.YEARLY, Category.SOFTWARE,
                LocalDate.of(2024, 1, 10), today.plusDays(45), PaymentStatus.ACTIVE, "", ""));
        repository.save(sample("ChatGPT Plus", "20", Currency.USD, BillingCycle.MONTHLY, Category.SOFTWARE,
                LocalDate.of(2024, 6, 1), today.plusDays(6), PaymentStatus.ACTIVE, "https://chat.openai.com", ""));
        repository.save(sample("iCloud+ 200GB", "400", Currency.JPY, BillingCycle.MONTHLY, Category.CLOUD,
                LocalDate.of(2021, 2, 20), today.plusDays(20), PaymentStatus.ACTIVE, "", ""));
        repository.save(sample("ジム会費", "7800", Currency.JPY, BillingCycle.MONTHLY, Category.HEALTH,
                LocalDate.of(2025, 1, 5), today.minusDays(2), PaymentStatus.UNPAID, "", "使用頻度が低い。解約を検討"));
    }

    private static Subscription sample(String name, String price, Currency currency, BillingCycle cycle,
                                       Category category, LocalDate start, LocalDate renewal,
                                       PaymentStatus status, String url, String memo) {
        Subscription s = new Subscription();
        s.setName(name);
        s.setPrice(new BigDecimal(price));
        s.setCurrency(currency);
        s.setBillingCycle(cycle);
        s.setCategory(category);
        s.setStartDate(start);
        s.setNextRenewalDate(renewal);
        s.setPaymentStatus(status);
        s.setUrl(url);
        s.setMemo(memo);
        return s;
    }
}
