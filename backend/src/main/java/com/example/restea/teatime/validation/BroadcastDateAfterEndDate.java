package com.example.restea.teatime.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = BroadcastDateAfterEndDateValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface BroadcastDateAfterEndDate {
    String message() default "broadcastDate must be after endDate.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
