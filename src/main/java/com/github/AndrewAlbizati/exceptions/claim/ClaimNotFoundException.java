package com.github.AndrewAlbizati.exceptions.claim;

public abstract class ClaimNotFoundException extends RuntimeException {
    public ClaimNotFoundException(String errorMessage) {
        super(errorMessage);
    }
}