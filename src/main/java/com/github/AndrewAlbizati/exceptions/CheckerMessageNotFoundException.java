package com.github.AndrewAlbizati.exceptions;

public class CheckerMessageNotFoundException extends RuntimeException {
    public CheckerMessageNotFoundException(String errorMessage) {
        super(errorMessage);
    }
}
