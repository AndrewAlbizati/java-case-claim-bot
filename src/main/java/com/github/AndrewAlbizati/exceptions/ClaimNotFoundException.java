package com.github.AndrewAlbizati.exceptions;

public class ClaimNotFoundException extends RuntimeException {
    public ClaimNotFoundException(String errorMessage) {
        super(errorMessage);
    }
}