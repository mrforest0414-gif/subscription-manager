package com.example.submanager.subscription;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "subscriptions")
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Currency currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BillingCycle billingCycle;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate nextRenewalDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus paymentStatus;

    @Column(nullable = false)
    private String url = "";

    @Column(columnDefinition = "TEXT", nullable = false)
    private String memo = "";

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public Subscription() {
    }

    @PrePersist
    void onCreate() {
        createdAt = updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ---------- 計算ロジック ----------

    /** 解約済み以外は集計の対象 */
    public boolean isCounted() {
        return paymentStatus != PaymentStatus.CANCELLED;
    }

    /** 円換算した月あたり金額(年額プランは12分割) */
    public BigDecimal getMonthlyJpy() {
        BigDecimal monthly = price.divide(BigDecimal.valueOf(billingCycle.getMonths()), 6, RoundingMode.HALF_UP);
        return monthly.multiply(currency.getJpyRate());
    }

    /** 登録された更新日が過去なら、課金サイクル分だけ先へ進めた「実際の次回更新日」 */
    public LocalDate effectiveRenewalDate(LocalDate today) {
        LocalDate d = nextRenewalDate;
        for (int i = 1; d.isBefore(today) && i < 600; i++) {
            d = nextRenewalDate.plusMonths((long) billingCycle.getMonths() * i);
        }
        return d;
    }

    public long daysUntilRenewal(LocalDate today) {
        return ChronoUnit.DAYS.between(today, effectiveRenewalDate(today));
    }

    /** 画面表示用: ¥1,490 / $20.00 */
    public String getPriceLabel() {
        String pattern = currency.getFractionDigits() == 0 ? "#,##0" : "#,##0.00";
        return currency.getSymbol() + new DecimalFormat(pattern).format(price);
    }

    // ---------- accessors ----------

    public Long getId() { return id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public Currency getCurrency() { return currency; }
    public void setCurrency(Currency currency) { this.currency = currency; }

    public BillingCycle getBillingCycle() { return billingCycle; }
    public void setBillingCycle(BillingCycle billingCycle) { this.billingCycle = billingCycle; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getNextRenewalDate() { return nextRenewalDate; }
    public void setNextRenewalDate(LocalDate nextRenewalDate) { this.nextRenewalDate = nextRenewalDate; }

    public PaymentStatus getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(PaymentStatus paymentStatus) { this.paymentStatus = paymentStatus; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getMemo() { return memo; }
    public void setMemo(String memo) { this.memo = memo; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
