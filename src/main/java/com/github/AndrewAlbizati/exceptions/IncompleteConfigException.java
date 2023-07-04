package com.github.AndrewAlbizati.exceptions;

public class IncompleteConfigException extends RuntimeException {
    public IncompleteConfigException(String errorMessage) {
        super(errorMessage);
    }
}
