package br.com.arthenyo.agendamento_docas_api.service;

import br.com.arthenyo.agendamento_docas_api.dto.FornecedorRequestDto;
import br.com.arthenyo.agendamento_docas_api.dto.FornecedorResponseDto;
import br.com.arthenyo.agendamento_docas_api.entity.Fornecedor;
import br.com.arthenyo.agendamento_docas_api.exceptions.ConflitosDeDadosExceptions;
import br.com.arthenyo.agendamento_docas_api.repository.FornecerdorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FornecedorService {

    private final FornecerdorRepository fornecerdorRepository;

    public FornecedorService(FornecerdorRepository fornecerdorRepository) {
        this.fornecerdorRepository = fornecerdorRepository;
    }

    @Transactional
    public FornecedorResponseDto criarFornecedor(FornecedorRequestDto dto){
        boolean existe = fornecerdorRepository.existsByCnpj(dto.cnpj());
        if(!existe){
            Fornecedor fornecedor = new Fornecedor();
            fornecedor.setCnpj(dto.cnpj());
            fornecedor.setRazao_social(dto.razaoSocial());
            fornecedor = fornecerdorRepository.save(fornecedor);
            return new FornecedorResponseDto(fornecedor);
        }else {
            throw new ConflitosDeDadosExceptions("Fornecedor ja cadastrado com esse CNPJ: " + dto.cnpj());
        }
    }
}
