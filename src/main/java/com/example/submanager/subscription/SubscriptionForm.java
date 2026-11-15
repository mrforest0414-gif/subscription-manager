package com.example.submanager.subscription;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

/** 登録・編集フォームの入力値。エンティティを直接バインドしないための DTO。 */
public class SubscriptionForm {

    @NotBlank(message = "サービス名は必須です")
    @Size(max = 80, message = "80文字以内で入力してください")
    private String name;

    @NotNull(message = "料金を入力してください")
    @DecimalMin(value = "0", message = "0以上で入力してください")
    private BigDecimal price;

    @NotNull
    private Currency currency = Currency.JPY;

    @NotNull
    private BillingCycle billingCycle = BillingCycle.MONTHLY;

    @NotNull
    private Category category = Category.VIDEO;

    @NotNull(message = "契約開始日を入力してください")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDate = LocalDate.now();

    @NotNull(message = "次回更新日を入力してください")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate nextRenewalDate = LocalDate.now();

    @NotNull
    private PaymentStatus paymentStatus = PaymentStatus.ACTIVE;

    @Pattern(regexp = "^$|^https?://\\S+$", message = "http:// または https:// から始まるURLを入力してください")
    private String url = "";

    @Size(max = 500, message = "500文字以内で入力してください")
    private String memo = "";

    public static SubscriptionForm from(Subscription s) {
        SubscriptionForm f = new SubscriptionForm();
        f.name = s.getName();
        f.price = s.getPrice();
        f.currency = s.getCurrency();
        f.billingCycle = s.getBillingCycle();
        f.category = s.getCategory();
        f.startDate = s.getStartDate();
        f.nextRenewalDate = s.getNextRenewalDate();
        f.paymentStatus = s.getPaymentStatus();
        f.url = s.getUrl();
        f.memo = s.getMemo();
        return f;
    }

    public void applyTo(Subscription s) {
        s.setName(name.trim());
        s.setPrice(price);
        s.setCurrency(currency);
        s.setBillingCycle(billingCycle);
        s.setCategory(category);
        s.setStartDate(startDate);
        s.setNextRenewalDate(nextRenewalDate);
        s.setPaymentStatus(paymentStatus);
        s.setUrl(url == null ? "" : url.trim());
        s.setMemo(memo == null ? "" : memo);
    }

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
}
