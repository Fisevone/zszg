package com.zszg.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.Map;

@Slf4j
@Component
public class JwtUtil {
    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.expiration-ms}")
    private long expirationMs;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(String username, String role) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);
        
        log.info("🔐 生成Token - 用户: {}, 角色: {}, 过期时间: {} ({}天后)", 
            username, role, expiry, expirationMs / (1000 * 60 * 60 * 24));
        
        return Jwts.builder()
                .setSubject(username)
                .addClaims(Map.of("role", role))
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractUsername(String token) {
        try {
            return getClaims(token).getSubject();
        } catch (Exception e) {
            log.error("❌ 提取用户名失败: {}", e.getMessage());
            return null;
        }
    }

    public String extractRole(String token) {
        try {
            Object role = getClaims(token).get("role");
            return role == null ? null : role.toString();
        } catch (Exception e) {
            log.error("❌ 提取角色失败: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * 验证Token是否有效
     */
    public boolean validateToken(String token) {
        try {
            Claims claims = getClaims(token);
            Date expiration = claims.getExpiration();
            boolean isValid = expiration.after(new Date());
            
            if (isValid) {
                log.debug("✅ Token验证成功 - 用户: {}, 过期时间: {}", 
                    claims.getSubject(), expiration);
            } else {
                log.warn("⚠️ Token已过期 - 用户: {}, 过期时间: {}", 
                    claims.getSubject(), expiration);
            }
            
            return isValid;
        } catch (ExpiredJwtException e) {
            log.warn("⚠️ Token已过期: {}", e.getMessage());
            return false;
        } catch (MalformedJwtException e) {
            log.error("❌ Token格式错误: {}", e.getMessage());
            return false;
        } catch (SignatureException e) {
            log.error("❌ Token签名验证失败: {}", e.getMessage());
            return false;
        } catch (UnsupportedJwtException e) {
            log.error("❌ 不支持的Token: {}", e.getMessage());
            return false;
        } catch (IllegalArgumentException e) {
            log.error("❌ Token为空或格式不正确: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 获取Token中的Claims
     */
    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    
    /**
     * 获取Token剩余有效时间（秒）
     */
    public long getTokenRemainingTime(String token) {
        try {
            Date expiration = getClaims(token).getExpiration();
            long remaining = (expiration.getTime() - System.currentTimeMillis()) / 1000;
            return Math.max(0, remaining);
        } catch (Exception e) {
            return 0;
        }
    }
}


