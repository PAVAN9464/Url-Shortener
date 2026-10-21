package com.urlshortner.url_shortner.controller;

import com.urlshortner.url_shortner.service.UrlService;
import com.urlshortner.url_shortner.service.UrlExpiredException;
import com.urlshortner.url_shortner.service.UrlNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RedirectControllerTests {

    @Test
    void redirectsToUrlReturnedByService() {
        UrlService urlService = mock(UrlService.class);
        String originalUrl = "https://www.example.com/destination";
        when(urlService.getOriginalUrl("aB3xY7k")).thenReturn(originalUrl);
        RedirectController controller = new RedirectController(urlService);

        var response = controller.redirect("aB3xY7k");

        assertEquals(HttpStatus.FOUND, response.getStatusCode());
        assertEquals(originalUrl, response.getHeaders().getFirst(HttpHeaders.LOCATION));
        verify(urlService).getOriginalUrl("aB3xY7k");
    }

    @Test
    void mapsUnknownShortCodeToNotFound() {
        var response = new ApiExceptionHandler().handleUrlNotFound(new UrlNotFoundException("missing"));

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void mapsExpiredShortCodeToGone() {
        var response = new ApiExceptionHandler().handleUrlExpired(new UrlExpiredException("expired"));

        assertEquals(HttpStatus.GONE, response.getStatusCode());
    }
}
