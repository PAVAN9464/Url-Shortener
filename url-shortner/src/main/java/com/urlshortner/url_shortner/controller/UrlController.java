package com.urlshortner.url_shortner.controller;

import com.urlshortner.url_shortner.dto.CreateUrlRequest;
import com.urlshortner.url_shortner.dto.CreateUrlResponse;
import com.urlshortner.url_shortner.service.UrlService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/urls")
public class UrlController {

    private final UrlService urlService;

    public UrlController(UrlService urlService) {
        this.urlService = urlService;
    }

    @PostMapping
    public ResponseEntity<CreateUrlResponse> createUrl(@Valid @RequestBody CreateUrlRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(urlService.createShortUrl(request));
    }
}
