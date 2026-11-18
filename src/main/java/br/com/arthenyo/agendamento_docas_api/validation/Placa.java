package br.com.arthenyo.agendamento_docas_api.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = PlacaValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Placa {
    String message() default "Placa: Invalida";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
