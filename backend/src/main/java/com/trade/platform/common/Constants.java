package com.trade.platform.common;

public class Constants {

    // Role constants
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_GUOHE = "GUOHE";
    public static final String ROLE_ENTERPRISE = "ENTERPRISE";

    // User status
    public static final int STATUS_ACTIVE = 1;
    public static final int STATUS_DISABLED = 0;

    // Apply status
    public static final int APPLY_PENDING = 0;
    public static final int APPLY_APPROVED = 1;
    public static final int APPLY_REJECTED = 2;

    private Constants() {
        // prevent instantiation
    }
}
