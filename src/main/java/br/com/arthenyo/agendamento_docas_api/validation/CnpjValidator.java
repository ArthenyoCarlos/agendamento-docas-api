package br.com.arthenyo.agendamento_docas_api.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CnpjValidator implements ConstraintValidator<CNPJ, String> {


    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true; // Use @NotNull ou @NotBlank separadamente se for obrigatório
        }

        // Remove caracteres não numéricos
        String cnpj = value.replaceAll("\\D", "");

        if (cnpj.length() != 14) {
            return false;
        }

        // Rejeita sequências repetidas (ex: 00.000.000/0000-00)
        if (cnpj.matches("(\\d)\\1{13}")) {
            return false;
        }

        try {
            // Cálculo do primeiro dígito verificador
            int soma = 0;
            int peso = 2;
            for (int i = 11; i >= 0; i--) {
                int num = Character.getNumericValue(cnpj.charAt(i));
                soma += num * peso;
                peso = (peso + 1) > 9 ? 2 : peso + 1;
            }
            int digito1 = 11 - (soma % 11);
            digito1 = (digito1 > 9) ? 0 : digito1;

            if (digito1 != Character.getNumericValue(cnpj.charAt(12))) {
                return false;
            }

            // Cálculo do segundo dígito verificador
            soma = 0;
            peso = 2;
            for (int i = 12; i >= 0; i--) {
                int num = Character.getNumericValue(cnpj.charAt(i));
                soma += num * peso;
                peso = (peso + 1) > 9 ? 2 : peso + 1;
            }
            int digito2 = 11 - (soma % 11);
            digito2 = (digito2 > 9) ? 0 : digito2;

            return digito2 == Character.getNumericValue(cnpj.charAt(13));
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
