package com.trade.platform.security;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class UserContext {

    private static final ThreadLocal<UserInfo> holder = new ThreadLocal<>();

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserInfo {
        private Long userId;
        private String username;
        private String role;
        private Long tenantId;
    }

    public static void set(UserInfo info) {
        holder.set(info);
    }

    public static UserInfo get() {
        return holder.get();
    }

    public static void clear() {
        holder.remove();
    }

    public static Long getUserId() {
        return get() != null ? get().getUserId() : null;
    }

    public static String getUsername() {
        return get() != null ? get().getUsername() : null;
    }

    public static String getRole() {
        return get() != null ? get().getRole() : null;
    }

    public static Long getTenantId() {
        return get() != null ? get().getTenantId() : null;
    }
}
