package com.urlshortner.url_shortner.dto;

public record CreateUrlResponse(String originalUrl, String shortCode, String shortUrl) {
}
