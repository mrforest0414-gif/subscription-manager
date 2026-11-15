package com.example.submanager.subscription;

public enum SortKey {
    RENEWAL("更新日が近い順"),
    PRICE_DESC("料金が高い順"),
    PRICE_ASC("料金が安い順"),
    NAME("名前順");

    private final String label;

    SortKey(String label) {
        this.label = label;
    }

    public String getLabel() { return label; }
}
