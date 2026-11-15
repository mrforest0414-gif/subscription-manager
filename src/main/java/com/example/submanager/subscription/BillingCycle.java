package com.example.submanager.subscription;

public enum BillingCycle {
    MONTHLY("月額", 1),
    YEARLY("年額", 12);

    private final String label;
    private final int months;

    BillingCycle(String label, int months) {
        this.label = label;
        this.months = months;
    }

    public String getLabel() { return label; }

    /** 1回の課金でカバーする月数 */
    public int getMonths() { return months; }
}
