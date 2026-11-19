package br.com.arthenyo.agendamento_docas_api.dto;

import br.com.arthenyo.agendamento_docas_api.entity.Fornecedor;

public record FornecedorResponseDto(
        Long id,
        String cnpj,
        String razaoSocial
) {
    public FornecedorResponseDto(Fornecedor entity){
        this(entity.getId(),
            entity.getCnpj(),
            entity.getRazaoSocial());
    }
}
