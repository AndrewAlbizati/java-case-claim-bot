package com.github.AndrewAlbizati.exceptions;

public class IncompleteConfigException extends RuntimeException {
    public IncompleteConfigException() {
        super("config.properties is incomplete. Please check README.md for instructions.");
    }
}
