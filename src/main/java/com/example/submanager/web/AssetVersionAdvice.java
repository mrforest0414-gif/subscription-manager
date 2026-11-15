package com.example.submanager.web;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/** CSS/JS の URL に埋め込むキャッシュバスター(起動ごとに値が変わる)。blog-system と同じ仕組み。 */
@ControllerAdvice
public class AssetVersionAdvice {

    private static final String VERSION = Long.toString(System.currentTimeMillis());

    @ModelAttribute("v")
    public String assetVersion() {
        return VERSION;
    }
}
