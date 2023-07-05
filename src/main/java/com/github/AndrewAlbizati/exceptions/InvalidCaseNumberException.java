package com.github.AndrewAlbizati.exceptions;

public class InvalidCaseNumberException extends RuntimeException {
    public InvalidCaseNumberException() {
        super("Case number invalid; case numbers can only be 8 digits");
    }
}