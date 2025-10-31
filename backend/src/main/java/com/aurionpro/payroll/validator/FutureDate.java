package com.aurionpro.payroll.validator;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Documented
@Constraint(validatedBy = FutureDateValidatorImpl.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface FutureDate {
    String message() default "Date cannot be more than 30 days in the future";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    int maxDays() default 30;
}