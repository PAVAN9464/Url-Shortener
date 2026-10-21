package com.urlshortner.url_shortner.service;

public class UrlNotFoundException extends RuntimeException {

    public UrlNotFoundException(String shortCode) {
        super("No URL found for short code: " + shortCode);
    }
}
