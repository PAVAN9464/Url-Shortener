package com.urlshortner.url_shortner.service;

import com.urlshortner.url_shortner.dto.CreateUrlRequest;
import com.urlshortner.url_shortner.dto.CreateUrlResponse;
import com.urlshortner.url_shortner.entity.Url;
import com.urlshortner.url_shortner.repository.UrlRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UrlServiceTests {

    private UrlRepository urlRepository;
    private UrlService urlService;

    @BeforeEach
    void setUp() {
        urlRepository = mock(UrlRepository.class);
        urlService = new UrlService(urlRepository, "http://localhost:8080/");
    }

    @Test
    void createsShortUrlForHttpUrl() {
        String originalUrl = "https://www.example.com/some/very/long/path";
        when(urlRepository.existsByShortCode(any())).thenReturn(false);
        when(urlRepository.saveAndFlush(any(Url.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CreateUrlResponse response = urlService.createShortUrl(new CreateUrlRequest(originalUrl));

        assertEquals(originalUrl, response.originalUrl());
        assertEquals(7, response.shortCode().length());
        assertEquals("http://localhost:8080/" + response.shortCode(), response.shortUrl());
    }

    @Test
    void rejectsNonHttpOrMalformedUrlsWithoutSaving() {
        assertThrows(InvalidUrlException.class,
                () -> urlService.createShortUrl(new CreateUrlRequest("ftp://example.com/file")));
        assertThrows(InvalidUrlException.class,
                () -> urlService.createShortUrl(new CreateUrlRequest("https://")));

        verify(urlRepository, never()).saveAndFlush(any(Url.class));
    }

    @Test
    void retriesWhenDatabaseUniqueConstraintDetectsConcurrentCollision() {
        String originalUrl = "https://example.com";
        when(urlRepository.existsByShortCode(any())).thenReturn(false, true, false);
        when(urlRepository.saveAndFlush(any(Url.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate short code"))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CreateUrlResponse response = urlService.createShortUrl(new CreateUrlRequest(originalUrl));

        assertEquals(originalUrl, response.originalUrl());
        assertEquals(2, org.mockito.Mockito.mockingDetails(urlRepository).getInvocations().stream()
                .filter(invocation -> invocation.getMethod().getName().equals("saveAndFlush"))
                .count());
    }

    @Test
    void returnsSavedOriginalUrlForRedirect() {
        String originalUrl = "https://www.example.com/destination";
        when(urlRepository.findByShortCode("aB3xY7k"))
                .thenReturn(Optional.of(new Url(originalUrl, "aB3xY7k")));

        assertEquals(originalUrl, urlService.getOriginalUrl("aB3xY7k"));
    }

    @Test
    void rejectsUnknownShortCode() {
        when(urlRepository.findByShortCode("missing")).thenReturn(Optional.empty());

        assertThrows(UrlNotFoundException.class, () -> urlService.getOriginalUrl("missing"));
    }

    @Test
    void rejectsExpiredUrl() throws ReflectiveOperationException {
        Url url = new Url("https://www.example.com/expired", "expired");
        Field expiresAt = Url.class.getDeclaredField("expiresAt");
        expiresAt.setAccessible(true);
        expiresAt.set(url, Instant.now().minusSeconds(1));
        when(urlRepository.findByShortCode("expired")).thenReturn(Optional.of(url));

        assertThrows(UrlExpiredException.class, () -> urlService.getOriginalUrl("expired"));
    }
}
