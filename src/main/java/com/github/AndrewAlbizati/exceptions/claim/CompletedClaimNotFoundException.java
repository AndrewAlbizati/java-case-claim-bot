package com.github.AndrewAlbizati.exceptions.claim;

public class CompletedClaimNotFoundException extends ClaimNotFoundException {
    public CompletedClaimNotFoundException(String errorMessage) {
        super(errorMessage);
    }
}
