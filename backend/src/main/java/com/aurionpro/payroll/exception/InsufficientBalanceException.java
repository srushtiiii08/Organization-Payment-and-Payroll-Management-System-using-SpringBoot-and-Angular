package com.aurionpro.payroll.exception;

public class InsufficientBalanceException extends RuntimeException {
    
    public InsufficientBalanceException(String message) {
        super(message);
    }
    
    public InsufficientBalanceException() {
        super("Insufficient balance in organization account");
    }
}