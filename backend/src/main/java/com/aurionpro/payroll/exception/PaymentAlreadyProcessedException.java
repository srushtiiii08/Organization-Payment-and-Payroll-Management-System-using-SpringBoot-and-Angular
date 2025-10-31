package com.aurionpro.payroll.exception;

public class PaymentAlreadyProcessedException extends RuntimeException {
    
    public PaymentAlreadyProcessedException(String message) {
        super(message);
    }
    
    public PaymentAlreadyProcessedException() {
        super("Payment request has already been processed");
    }
}