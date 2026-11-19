package br.com.arthenyo.agendamento_docas_api.service;

import br.com.arthenyo.agendamento_docas_api.dto.FornecedorRequestDto;
import br.com.arthenyo.agendamento_docas_api.dto.FornecedorResponseDto;
import br.com.arthenyo.agendamento_docas_api.entity.Fornecedor;
import br.com.arthenyo.agendamento_docas_api.exceptions.ConflitoException;
import br.com.arthenyo.agendamento_docas_api.repository.FornecedorRepository;
import br.com.arthenyo.agendamento_docas_api.validation.CnpjValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FornecedorService {

    private final FornecedorRepository fornecedorRepository;

    public FornecedorService(FornecedorRepository fornecedorRepository) {
        this.fornecedorRepository = fornecedorRepository;
    }

    @Transactional
    public FornecedorResponseDto criarFornecedor(FornecedorRequestDto dto) {
        String cnpj = CnpjValidator.normalizar(dto.cnpj());
        if (fornecedorRepository.existsByCnpj(cnpj)) {
            throw new ConflitoException("Fornecedor já cadastrado com esse CNPJ: " + cnpj);
        }
        Fornecedor fornecedor = new Fornecedor(null, cnpj, dto.razaoSocial());
        return new FornecedorResponseDto(fornecedorRepository.save(fornecedor));
    }
}
