package com.example.submanager.subscription;

public enum PaymentStatus {
    ACTIVE("支払い済み", "success"),
    TRIAL("無料トライアル", "info"),
    UNPAID("未払い", "danger"),
    CANCELLED("解約済み", "secondary");

    private final String label;
    /** Bootstrap の contextual color 名(badge の bg-* に使う) */
    private final String color;

    PaymentStatus(String label, String color) {
        this.label = label;
        this.color = color;
    }

    public String getLabel() { return label; }
    public String getColor() { return color; }
}
