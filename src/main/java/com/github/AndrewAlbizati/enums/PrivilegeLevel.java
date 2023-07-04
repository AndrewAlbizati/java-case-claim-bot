package com.github.AndrewAlbizati.enums;

public enum PrivilegeLevel {
    TECH(0),
    LEAD(1),
    PA(2),
    MANAGER(3);

    private final int value;
    public int getValue() {
        return value;
    }

    public static PrivilegeLevel fromInt(int value) {
        return switch(value) {
            case 0 -> TECH;
            case 1 -> LEAD;
            case 2 -> PA;
            case 3 -> MANAGER;
            default -> throw new IllegalArgumentException("PrivilegeLevel not found");
        };
    }
    PrivilegeLevel(int value) {
        this.value = value;
    }
}
