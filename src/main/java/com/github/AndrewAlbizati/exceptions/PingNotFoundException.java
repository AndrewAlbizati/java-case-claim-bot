package com.github.AndrewAlbizati.exceptions;

public class PingNotFoundException extends RuntimeException {
    public PingNotFoundException() {
        super("Ping not found, please check the thread ID provided");
    }
}
