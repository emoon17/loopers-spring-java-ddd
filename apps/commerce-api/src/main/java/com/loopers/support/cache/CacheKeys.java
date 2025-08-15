package com.loopers.support.cache;

import org.springframework.data.domain.Pageable;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public final class CacheKeys {

    private CacheKeys() {}

    public static String normBrandName(String brandName) {
        if (brandName == null) return "ALL";
        String t = brandName.trim().toLowerCase();
        return t.isEmpty() ? "ALL" : t;
    }

    public static String likesGlobalVersionKey(){
        return "likes:global:ver";
    }

    public static String likesListKey(String brandName, Pageable pageable, long ver) {
        String bt = normBrandName(brandName);
        return String.format("product:list:likes:%s:%d:%d:v%d",
                bt, pageable.getPageNumber(), pageable.getPageSize(), ver);
    }

    private static String buildLikesListKey(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] d = md.digest(s.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 8; i++) sb.append(String.format("%02x", d[i]));
            return sb.toString();
        } catch (Exception e) {
            return "hasherr";
        }
    }

}
