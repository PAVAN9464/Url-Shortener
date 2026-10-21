package com.urlshortner.url_shortner.service;

public class InvalidUrlException extends RuntimeException {

    public InvalidUrlException(String message) {
        super(message);
    }
}
