package org.example;

public final class Constants {
    // Private constructor to prevent instantiation
    private Constants() {
        throw new AssertionError("Cannot instantiate Constants class");
    }

    public static final int PORT = 6666;
    public static final String DOMAIN = "10.20.30.%d";
}
