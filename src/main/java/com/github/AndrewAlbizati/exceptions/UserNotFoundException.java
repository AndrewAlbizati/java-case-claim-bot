package com.github.AndrewAlbizati.exceptions;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException() {
        super("User not found, please check the ID provided");
    }
}