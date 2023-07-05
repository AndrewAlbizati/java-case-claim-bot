package com.github.AndrewAlbizati.exceptions.claim;

public class CheckedClaimNotFoundException extends ClaimNotFoundException {
    public CheckedClaimNotFoundException(String errorMessage) {
        super(errorMessage);
    }
}
