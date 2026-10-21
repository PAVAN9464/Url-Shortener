package com.urlshortner.url_shortner.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateUrlRequest(
        @NotBlank(message = "originalUrl is required")
        @Size(max = 2048, message = "originalUrl must not exceed 2048 characters")
        String originalUrl
) {
}
