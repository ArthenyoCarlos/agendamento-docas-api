package br.com.arthenyo.agendamento_docas_api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CancelamentoRequestDto(
        @NotBlank
        @Size(min = 10, max = 255)
        String motivo
){
    public CancelamentoRequestDto {
        if (motivo != null) {
            motivo = motivo.trim();
        }
    }
}
