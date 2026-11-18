package br.com.arthenyo.agendamento_docas_api.dto;

import br.com.arthenyo.agendamento_docas_api.validation.CNPJ;
import jakarta.validation.constraints.NotBlank;

public record FornecedorRequestDto(
        @NotBlank(message = "O CNPJ é obrigatório")
        @CNPJ(message = "CNPJ informado é inválido")
        String cnpj,

        @NotBlank(message = "O campo razão social e obrigatorio")
        String razaoSocial
) {
}
