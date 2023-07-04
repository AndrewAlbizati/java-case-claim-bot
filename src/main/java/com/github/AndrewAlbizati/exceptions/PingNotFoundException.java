package com.github.AndrewAlbizati.exceptions;

public class PingNotFoundException extends RuntimeException {
    public PingNotFoundException(String errorMessage) {
        super(errorMessage);
    }
}
