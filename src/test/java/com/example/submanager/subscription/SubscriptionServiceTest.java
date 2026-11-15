package com.example.submanager.subscription;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

    static final LocalDate TODAY = LocalDate.of(2026, 5, 20);

    @Mock
    SubscriptionRepository repository;

    SubscriptionService service;

    @BeforeEach
    void setUp() {
        service = new SubscriptionService(repository);
    }

    private static Subscription sub(String name, String price, Category cat, LocalDate renewal, PaymentStatus status) {
        Subscription s = SubscriptionTest.sub(price, Currency.JPY, BillingCycle.MONTHLY, renewal, status);
        s.setName(name);
        s.setCategory(cat);
        return s;
    }

    private List<Subscription> data() {
        return List.of(
                sub("Netflix", "1500", Category.VIDEO, TODAY.plusDays(3), PaymentStatus.ACTIVE),
                sub("Spotify", "1000", Category.MUSIC, TODAY.plusDays(20), PaymentStatus.ACTIVE),
                sub("Hulu", "500", Category.VIDEO, TODAY.plusDays(10), PaymentStatus.ACTIVE),
                sub("Old", "9999", Category.GAME, TODAY.plusDays(1), PaymentStatus.CANCELLED));
    }

    @Test
    void summarizeExcludesCancelledAndGroupsByCategory() {
        Summary s = SubscriptionService.summarize(data(), TODAY);

        assertThat(s.monthlyTotal()).isEqualByComparingTo("3000");
        assertThat(s.yearlyForecast()).isEqualByComparingTo("36000");
        assertThat(s.activeCount()).isEqualTo(3);
        assertThat(s.upcomingCount()).isEqualTo(1); // Netflix のみ 7日以内(解約済みは除外)
        assertThat(s.byCategory()).hasSize(2);
        assertThat(s.byCategory().get(0).category()).isEqualTo(Category.VIDEO);
        assertThat(s.byCategory().get(0).amount()).isEqualByComparingTo("2000");
        assertThat(s.byCategory().get(0).percent()).isEqualTo(67);
    }

    @Test
    void summarizeOfEmptyListIsZero() {
        Summary s = SubscriptionService.summarize(List.of(), TODAY);
        assertThat(s.monthlyTotal()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(s.byCategory()).isEmpty();
    }

    @Test
    void searchFiltersByCategoryAndName() {
        when(repository.findAll()).thenReturn(data());

        assertThat(service.search(Category.VIDEO, SortKey.NAME, null, TODAY))
                .extracting(Subscription::getName).containsExactly("Hulu", "Netflix");
        assertThat(service.search(null, SortKey.NAME, "spot", TODAY))
                .extracting(Subscription::getName).containsExactly("Spotify");
    }

    @Test
    void searchSortsByRenewalAndPrice() {
        when(repository.findAll()).thenReturn(data());

        assertThat(service.search(null, SortKey.RENEWAL, null, TODAY))
                .extracting(Subscription::getName).containsExactly("Old", "Netflix", "Hulu", "Spotify");
        assertThat(service.search(null, SortKey.PRICE_DESC, null, TODAY))
                .extracting(Subscription::getName).containsExactly("Old", "Netflix", "Spotify", "Hulu");
        assertThat(service.search(null, SortKey.PRICE_ASC, null, TODAY))
                .extracting(Subscription::getName).containsExactly("Hulu", "Spotify", "Netflix", "Old");
    }

    @Test
    void createAppliesFormAndSaves() {
        SubscriptionForm form = new SubscriptionForm();
        form.setName("  Netflix ");
        form.setPrice(new BigDecimal("1490"));
        when(repository.save(any(Subscription.class))).thenAnswer(i -> i.getArgument(0));

        Subscription saved = service.create(form);

        assertThat(saved.getName()).isEqualTo("Netflix");
        assertThat(saved.getPrice()).isEqualByComparingTo("1490");
        verify(repository).save(saved);
    }

    @Test
    void updateReturnsEmptyWhenMissing() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThat(service.update(99L, new SubscriptionForm())).isEmpty();
    }

    @Test
    void deleteReportsWhetherTargetExisted() {
        when(repository.existsById(1L)).thenReturn(true);
        when(repository.existsById(2L)).thenReturn(false);

        assertThat(service.delete(1L)).isTrue();
        assertThat(service.delete(2L)).isFalse();
        verify(repository).deleteById(1L);
    }
}
