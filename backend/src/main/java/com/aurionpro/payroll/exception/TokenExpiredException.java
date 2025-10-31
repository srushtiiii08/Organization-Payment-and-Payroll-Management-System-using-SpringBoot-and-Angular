package com.aurionpro.payroll.exception;

public class TokenExpiredException extends RuntimeException {
    
    public TokenExpiredException(String message) {
        super(message);
    }
    
    public TokenExpiredException() {
        super("JWT token has expired");
    }
}