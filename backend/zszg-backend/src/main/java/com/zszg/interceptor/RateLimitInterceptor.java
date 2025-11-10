package com.zszg.interceptor;

import com.zszg.service.RateLimiterService;
import com.zszg.user.User;
import com.zszg.user.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * API限流拦截器
 * 
 * 拦截所有API请求,进行限流检查
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {
    
    private final RateLimiterService rateLimiterService;
    private final UserRepository userRepository;
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) 
            throws Exception {
        
        String uri = request.getRequestURI();
        
        // 获取当前用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            // 未认证用户,不限流（由安全配置控制访问）
            return true;
        }
        
        String username = authentication.getName();
        if ("anonymousUser".equals(username)) {
            return true;
        }
        
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return true;
        }
        
        Long userId = user.getId();
        boolean allowed = true;
        
        // 根据不同的API路径进行不同的限流
        if (uri.contains("/api/ai/analyze") || uri.contains("/api/errorbooks/analyze")) {
            allowed = rateLimiterService.allowAIAnalysis(userId);
            if (!allowed) {
                sendRateLimitResponse(response, "AI分析请求过于频繁，请稍后再试（限制：10次/分钟）");
                return false;
            }
        } else if (uri.contains("/api/ai/photo-search") || uri.contains("/api/ai/ocr")) {
            allowed = rateLimiterService.allowPhotoSearch(userId);
            if (!allowed) {
                sendRateLimitResponse(response, "拍照搜题请求过于频繁，请稍后再试（限制：5次/分钟）");
                return false;
            }
        } else if (uri.startsWith("/api/")) {
            // 其他API一般限流
            allowed = rateLimiterService.allowGeneralAPI(userId);
            if (!allowed) {
                sendRateLimitResponse(response, "请求过于频繁，请稍后再试（限制：60次/分钟）");
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * 发送限流响应
     */
    private void sendRateLimitResponse(HttpServletResponse response, String message) throws Exception {
        response.setStatus(429); // Too Many Requests
        response.setContentType("application/json;charset=UTF-8");
        
        String jsonResponse = String.format(
            "{\"code\":429,\"message\":\"%s\",\"data\":null}",
            message
        );
        
        response.getWriter().write(jsonResponse);
        response.getWriter().flush();
        
        log.warn("⚠️ 触发限流: {}", message);
    }
}























