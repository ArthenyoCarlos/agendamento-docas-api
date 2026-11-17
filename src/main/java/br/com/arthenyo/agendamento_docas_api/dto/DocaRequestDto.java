package br.com.arthenyo.agendamento_docas_api.dto;

import br.com.arthenyo.agendamento_docas_api.entity.enums.TipoDoca;

public record DocaRequestDto(
    TipoDoca tipo
) {
}
