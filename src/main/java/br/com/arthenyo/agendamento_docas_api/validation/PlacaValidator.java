package br.com.arthenyo.agendamento_docas_api.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class PlacaValidator implements ConstraintValidator<Placa, String> {

    // Aceita AAA9999 (antigo) ou AAA9A99 (Mercosul), com ou sem hífen no modelo antigo
    private static final Pattern PLACA_PATTERN = Pattern.compile("^[A-Z]{3}-?[0-9][A-Z0-9][0-9]{2}$");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true; // Deixe o @NotBlank cuidar da obrigatoriedade
        }

        // Remove espaços nas pontas e converte para maiúsculo
        String placaLimpa = value.trim().toUpperCase();

        return PLACA_PATTERN.matcher(placaLimpa).matches();
    }
}
