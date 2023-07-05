package com.github.AndrewAlbizati.exceptions.claim;

public class ActiveClaimNotFoundException extends ClaimNotFoundException {
    public ActiveClaimNotFoundException(String errorMessage) {
        super(errorMessage);
    }
}
