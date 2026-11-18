package br.com.arthenyo.agendamento_docas_api.validation;

import jakarta.validation.Constraint;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = PlacaValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Placa {
}
