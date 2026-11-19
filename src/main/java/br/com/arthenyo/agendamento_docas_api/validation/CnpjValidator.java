package br.com.arthenyo.agendamento_docas_api.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Locale;
import java.util.regex.Pattern;

public class CnpjValidator implements ConstraintValidator<CNPJ, String> {

    private static final Pattern FORMATO = Pattern.compile(
            "[0-9A-Z]{12}[0-9]{2}|[0-9A-Z]{2}\\.[0-9A-Z]{3}\\.[0-9A-Z]{3}/[0-9A-Z]{4}-[0-9]{2}",
            Pattern.CASE_INSENSITIVE);

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true;
        }
        if (!FORMATO.matcher(value.trim()).matches()) {
            return false;
        }

        String cnpj = normalizar(value);
        return !cnpj.matches("([0-9])\\1{13}")
                && calcularDigito(cnpj, 12) == cnpj.charAt(12) - '0'
                && calcularDigito(cnpj, 13) == cnpj.charAt(13) - '0';
    }

    public static String normalizar(String cnpj) {
        return cnpj.trim().toUpperCase(Locale.ROOT).replaceAll("[./-]", "");
    }

    private int calcularDigito(String cnpj, int tamanhoBase) {
        int soma = 0;
        for (int i = tamanhoBase - 1; i >= 0; i--) {
            int peso = 2 + (tamanhoBase - 1 - i) % 8;
            soma += (cnpj.charAt(i) - '0') * peso;
        }
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }
}
