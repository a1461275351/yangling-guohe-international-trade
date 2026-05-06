package com.trade.platform.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    public String generateToken(Long userId, String username, String role, Long tenantId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration * 1000);

        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("userId", userId)
                .claim("username", username)
                .claim("role", role)
                .claim("tenantId", tenantId)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS256, secret)
                .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();
    }

    public Long getUserId(Claims claims) {
        return claims.get("userId", Long.class);
    }

    public String getUsername(Claims claims) {
        return claims.get("username", String.class);
    }

    public String getRole(Claims claims) {
        return claims.get("role", String.class);
    }

    public Long getTenantId(Claims claims) {
        return claims.get("tenantId", Long.class);
    }

    public boolean isTokenExpired(Claims claims) {
        return claims.getExpiration().before(new Date());
    }
}
