package com.aurionpro.payroll.validator;


import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.LocalDate;

public class FutureDateValidatorImpl implements ConstraintValidator<FutureDate, LocalDate> {
    
    private int maxDays;
    
    @Override
    public void initialize(FutureDate constraintAnnotation) {
        this.maxDays = constraintAnnotation.maxDays();
    }
    
    @Override
    public boolean isValid(LocalDate date, ConstraintValidatorContext context) {
        if (date == null) {
            return true; // Let @NotNull handle null validation
        }
        
        LocalDate today = LocalDate.now();
        LocalDate maxAllowedDate = today.plusDays(maxDays);
        
        // Date should be between today and 30 days from today
        return !date.isBefore(today) && !date.isAfter(maxAllowedDate);
    }
}