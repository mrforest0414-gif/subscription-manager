package com.example.submanager.subscription;

public enum Category {
    VIDEO("動画"),
    MUSIC("音楽"),
    SOFTWARE("ソフトウェア"),
    CLOUD("クラウド"),
    NEWS_BOOKS("ニュース・書籍"),
    GAME("ゲーム"),
    HEALTH("健康・フィットネス"),
    OTHER("その他");

    private final String label;

    Category(String label) {
        this.label = label;
    }

    public String getLabel() { return label; }
}
