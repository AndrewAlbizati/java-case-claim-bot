package com.github.AndrewAlbizati.enums;

public enum Status {
    CHECKED("Checked"),
    PINGED("Pinged"),
    RESOLVED("Resolved");


    private final String value;
    public String value() {
        return value;
    }

    public static Status fromStr(String value) {
        return switch(value) {
            case "Checked" -> CHECKED;
            case "Pinged" -> PINGED;
            case "Resolved" -> RESOLVED;
            default -> throw new IllegalArgumentException("Status not found");
        };
    }
    Status(String value) {
        this.value = value;
    }
}
