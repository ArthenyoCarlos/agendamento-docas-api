package br.com.arthenyo.agendamento_docas_api.service;

import br.com.arthenyo.agendamento_docas_api.dto.AgendamentoRequestDto;
import br.com.arthenyo.agendamento_docas_api.dto.AgendamentoResponseDto;
import br.com.arthenyo.agendamento_docas_api.entity.Agendamento;
import br.com.arthenyo.agendamento_docas_api.entity.Doca;
import br.com.arthenyo.agendamento_docas_api.entity.Fornecedor;
import br.com.arthenyo.agendamento_docas_api.entity.enums.StatusAgendamento;
import br.com.arthenyo.agendamento_docas_api.exceptions.ConflitosDeDadosExceptions;
import br.com.arthenyo.agendamento_docas_api.exceptions.ObjetoNaoEncontradoException;
import br.com.arthenyo.agendamento_docas_api.repository.AgendamentoRepository;
import br.com.arthenyo.agendamento_docas_api.repository.DocaRepository;
import br.com.arthenyo.agendamento_docas_api.repository.FornecerdorRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AgendamentoService {

    private final AgendamentoRepository agendamentoRepository;
    private final FornecerdorRepository fornecerdorRepository;
    private final DocaRepository docaRepository;

    public AgendamentoService(AgendamentoRepository agendamentoRepository,
                              FornecerdorRepository fornecerdorRepository,
                              DocaRepository docaRepository) {
        this.agendamentoRepository = agendamentoRepository;
        this.fornecerdorRepository = fornecerdorRepository;
        this.docaRepository = docaRepository;
    }

    @Transactional
    public AgendamentoResponseDto criarAgendamento(AgendamentoRequestDto dto){
        Fornecedor fornecedor = fornecerdorRepository.findById(dto.fornecedorId())
                .orElseThrow(() -> new ObjetoNaoEncontradoException("Fornecedor com o codigo informado não existe " + dto.fornecedorId()));

        Doca doca = docaRepository.findById(dto.docaId())
                .orElseThrow(() -> new ObjetoNaoEncontradoException("Doca com o codigo informado não existe" + dto.docaId()));

        Agendamento agendamento = new Agendamento();
        agendamento.setFornecedor(fornecedor);
        if(!doca.getTipo().toString().equals(dto.tipoCarga().toString())){
            throw new ConflitosDeDadosExceptions("A doca não é do mesmo tipo");
        }
        agendamento.setDoca(doca);
        agendamento.setPlacaVeiculo(dto.placaVeiculo());
        agendamento.setQuantidadePaletes(dto.quantidadePaletes());
        agendamento.setTipoCarga(dto.tipoCarga());
        agendamento.setStatus(StatusAgendamento.AGENDADO);

        agendamento = agendamentoRepository.save(agendamento);

        return new AgendamentoResponseDto(agendamento);

    }

    private static boolean isDataValida(LocalDateTime inico){

        LocalDateTime agora = LocalDateTime.now();

        boolean maiorQue24Horas = inico.isAfter(agora.plusHours(24));

        int minutos = inico.getMinute();
        boolean minutosValidos = List.of(0, 15, 30, 45).contains(minutos);

        return maiorQue24Horas && minutosValidos;
    }
}
