package com.github.AndrewAlbizati.exceptions;

public class PingThreadNotFoundException extends RuntimeException {
    public PingThreadNotFoundException(String errorMessage) {
        super(errorMessage);
    }
}
