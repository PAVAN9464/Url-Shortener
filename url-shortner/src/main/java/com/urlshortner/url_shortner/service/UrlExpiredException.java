package com.urlshortner.url_shortner.service;

public class UrlExpiredException extends RuntimeException {

    public UrlExpiredException(String shortCode) {
        super("URL has expired for short code: " + shortCode);
    }
}
