package com.zszg.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) 
            throws ServletException, IOException {
        
        String path = request.getRequestURI();
        
        // 只跳过真正公开的路径（login和register）
        if (path.equals("/api/auth/login") || 
            path.equals("/api/auth/register") || 
            path.startsWith("/uploads/") ||
            path.startsWith("/actuator/")) {
            filterChain.doFilter(request, response);
            return;
        }
        
        final String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            
            try {
                // Token验证 - 宽松模式：只记录警告，不阻止请求
                boolean isValid = jwtUtil.validateToken(token);
                if (!isValid) {
                    log.warn("⚠️ Token可能有问题，但继续尝试认证 - 路径: {}", path);
                    // 不立即返回，继续尝试认证
                }
                
                String username = jwtUtil.extractUsername(token);
                
                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    log.debug("🔐 认证用户: {} - 路径: {}", username, path);
                    
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    
                    // 记录Token剩余时间（仅用于调试）
                    long remainingSeconds = jwtUtil.getTokenRemainingTime(token);
                    long remainingDays = remainingSeconds / (60 * 60 * 24);
                    log.debug("✅ 认证成功 - 用户: {}, Token剩余有效期: {}天", username, remainingDays);
                } else if (username == null) {
                    log.warn("⚠️ 无法从Token中提取用户名 - 路径: {}", path);
                }
                
            } catch (Exception e) {
                log.error("❌ JWT认证异常 - 路径: {}, 错误: {}", path, e.getMessage(), e);
                // 清除认证信息
                SecurityContextHolder.clearContext();
            }
        } else {
            // 需要认证但没有提供Token
            log.warn("⚠️ 请求未提供Token - 路径: {}, 方法: {}", path, request.getMethod());
        }
        
        filterChain.doFilter(request, response);
    }
}



