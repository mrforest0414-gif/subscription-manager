package com.example.submanager.subscription;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Collator;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Service
public class SubscriptionService {

    /** 更新日がこの日数以内ならハイライト対象 */
    public static final int REMINDER_DAYS = 7;

    private final SubscriptionRepository repository;

    public SubscriptionService(SubscriptionRepository repository) {
        this.repository = repository;
    }

    // ---------- CRUD ----------

    public Optional<Subscription> find(Long id) {
        return repository.findById(id);
    }

    @Transactional
    public Subscription create(SubscriptionForm form) {
        Subscription s = new Subscription();
        form.applyTo(s);
        return repository.save(s);
    }

    @Transactional
    public Optional<Subscription> update(Long id, SubscriptionForm form) {
        return repository.findById(id).map(s -> {
            form.applyTo(s);
            return repository.save(s);
        });
    }

    @Transactional
    public boolean delete(Long id) {
        if (!repository.existsById(id)) {
            return false;
        }
        repository.deleteById(id);
        return true;
    }

    // ---------- 一覧(フィルタ・ソート) ----------

    public List<Subscription> search(Category category, SortKey sort, String query, LocalDate today) {
        String q = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        return repository.findAll().stream()
                .filter(s -> category == null || s.getCategory() == category)
                .filter(s -> q.isEmpty() || s.getName().toLowerCase(Locale.ROOT).contains(q))
                .sorted(comparator(sort == null ? SortKey.RENEWAL : sort, today))
                .toList();
    }

    private static Comparator<Subscription> comparator(SortKey sort, LocalDate today) {
        return switch (sort) {
            case RENEWAL -> Comparator.comparing(s -> s.effectiveRenewalDate(today));
            case PRICE_DESC -> Comparator.comparing(Subscription::getMonthlyJpy).reversed();
            case PRICE_ASC -> Comparator.comparing(Subscription::getMonthlyJpy);
            case NAME -> Comparator.comparing(Subscription::getName, Collator.getInstance(Locale.JAPANESE));
        };
    }

    // ---------- 集計 ----------

    public Summary summarize(LocalDate today) {
        return summarize(repository.findAll(), today);
    }

    static Summary summarize(List<Subscription> all, LocalDate today) {
        List<Subscription> counted = all.stream().filter(Subscription::isCounted).toList();

        BigDecimal monthlyTotal = counted.stream()
                .map(Subscription::getMonthlyJpy)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<Category, BigDecimal> byCategory = new EnumMap<>(Category.class);
        for (Subscription s : counted) {
            byCategory.merge(s.getCategory(), s.getMonthlyJpy(), BigDecimal::add);
        }
        List<Summary.CategoryShare> shares = byCategory.entrySet().stream()
                .sorted(Map.Entry.<Category, BigDecimal>comparingByValue().reversed())
                .map(e -> new Summary.CategoryShare(
                        e.getKey(),
                        yen(e.getValue()),
                        monthlyTotal.signum() == 0 ? 0
                                : e.getValue().multiply(BigDecimal.valueOf(100))
                                        .divide(monthlyTotal, 0, RoundingMode.HALF_UP).intValue()))
                .toList();

        int upcoming = (int) counted.stream()
                .filter(s -> s.daysUntilRenewal(today) <= REMINDER_DAYS)
                .count();

        return new Summary(
                yen(monthlyTotal),
                yen(monthlyTotal.multiply(BigDecimal.valueOf(12))),
                counted.size(),
                upcoming,
                shares);
    }

    private static BigDecimal yen(BigDecimal v) {
        return v.setScale(0, RoundingMode.HALF_UP);
    }
}
