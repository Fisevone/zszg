package com.zszg.exception;

/**
 * AI服务异常
 * 
 * AI相关服务调用失败时抛出
 */
public class AIServiceException extends RuntimeException {
    
    public AIServiceException(String message) {
        super(message);
    }
    
    public AIServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
















