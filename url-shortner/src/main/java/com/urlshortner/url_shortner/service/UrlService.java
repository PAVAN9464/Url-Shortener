package com.urlshortner.url_shortner.service;

import com.urlshortner.url_shortner.dto.CreateUrlRequest;
import com.urlshortner.url_shortner.dto.CreateUrlResponse;
import com.urlshortner.url_shortner.entity.Url;
import com.urlshortner.url_shortner.repository.UrlRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;

@Service
public class UrlService {

    private static final int SHORT_CODE_LENGTH = 7;
    private static final int MAX_GENERATION_ATTEMPTS = 5;

    private final UrlRepository urlRepository;
    private final String baseUrl;
    private final SecureRandom secureRandom = new SecureRandom();

    public UrlService(
            UrlRepository urlRepository,
            @Value("${app.base-url}") String baseUrl
    ) {
        this.urlRepository = urlRepository;
        this.baseUrl = baseUrl.replaceAll("/+$", "");
    }

    public CreateUrlResponse createShortUrl(CreateUrlRequest request) {
        String originalUrl = request.originalUrl();
        validateHttpUrl(originalUrl);

        for (int attempt = 0; attempt < MAX_GENERATION_ATTEMPTS; attempt++) {
            String shortCode = generateShortCode();
            if (urlRepository.existsByShortCode(shortCode)) {
                continue;
            }

            try {
                Url savedUrl = urlRepository.saveAndFlush(new Url(originalUrl, shortCode));
                return new CreateUrlResponse(
                        savedUrl.getOriginalUrl(),
                        savedUrl.getShortCode(),
                        baseUrl + "/" + savedUrl.getShortCode()
                );
            } catch (DataIntegrityViolationException exception) {
                if (!urlRepository.existsByShortCode(shortCode)) {
                    throw exception;
                }
            }
        }

        throw new IllegalStateException("Could not generate a unique short code. Please try again.");
    }

    public String getOriginalUrl(String shortCode) {
        Url url = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new UrlNotFoundException(shortCode));
        if (url.getExpiresAt() != null && !url.getExpiresAt().isAfter(Instant.now())) {
            throw new UrlExpiredException(shortCode);
        }
        return url.getOriginalUrl();
    }

    private void validateHttpUrl(String value) {
        try {
            URI uri = URI.create(value);
            String scheme = uri.getScheme();
            if (!uri.isAbsolute()
                    || uri.isOpaque()
                    || uri.getHost() == null
                    || !("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme))) {
                throw new InvalidUrlException("originalUrl must be a valid HTTP or HTTPS URL");
            }
        } catch (IllegalArgumentException exception) {
            throw new InvalidUrlException("originalUrl must be a valid HTTP or HTTPS URL");
        }
    }

    private String generateShortCode() {
        byte[] randomBytes = new byte[6];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes)
                .substring(0, SHORT_CODE_LENGTH);
    }
}
