package br.com.arthenyo.agendamento_docas_api.service;

import br.com.arthenyo.agendamento_docas_api.dto.AgendamentoRequestDto;
import br.com.arthenyo.agendamento_docas_api.dto.AgendamentoResponseDto;
import br.com.arthenyo.agendamento_docas_api.entity.Agendamento;
import br.com.arthenyo.agendamento_docas_api.entity.Doca;
import br.com.arthenyo.agendamento_docas_api.entity.Fornecedor;
import br.com.arthenyo.agendamento_docas_api.entity.enums.StatusAgendamento;
import br.com.arthenyo.agendamento_docas_api.entity.enums.TipoCarga;
import br.com.arthenyo.agendamento_docas_api.entity.enums.TipoDoca;
import br.com.arthenyo.agendamento_docas_api.exceptions.ConflitoException;
import br.com.arthenyo.agendamento_docas_api.exceptions.ObjetoNaoEncontradoException;
import br.com.arthenyo.agendamento_docas_api.exceptions.RegraNegocioException;
import br.com.arthenyo.agendamento_docas_api.repository.AgendamentoRepository;
import br.com.arthenyo.agendamento_docas_api.repository.DocaRepository;
import br.com.arthenyo.agendamento_docas_api.repository.FornecedorRepository;
import br.com.arthenyo.agendamento_docas_api.validation.PlacaValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

@Service
public class AgendamentoService {

    private static final ZoneId FUSO_PADRAO = ZoneId.of("America/Sao_Paulo");
    private static final LocalTime ABERTURA = LocalTime.of(6, 0);
    private static final LocalTime FECHAMENTO = LocalTime.of(18, 0);

    private final AgendamentoRepository agendamentoRepository;
    private final FornecedorRepository fornecedorRepository;
    private final DocaRepository docaRepository;
    private final Clock clock;

    @Autowired
    public AgendamentoService(AgendamentoRepository agendamentoRepository,
                              FornecedorRepository fornecedorRepository,
                              DocaRepository docaRepository) {
        this(agendamentoRepository, fornecedorRepository, docaRepository, Clock.system(FUSO_PADRAO));
    }

    AgendamentoService(AgendamentoRepository agendamentoRepository,
                       FornecedorRepository fornecedorRepository,
                       DocaRepository docaRepository,
                       Clock clock) {
        this.agendamentoRepository = agendamentoRepository;
        this.fornecedorRepository = fornecedorRepository;
        this.docaRepository = docaRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public AgendamentoResponseDto buscarAgendamentoPorId(Long agendamentoId) {
        return new AgendamentoResponseDto(buscarEntidade(agendamentoId));
    }

    @Transactional(readOnly = true)
    public List<AgendamentoResponseDto> listarAgendaDocaPorData(Long docaId, LocalDate data) {
        if (!docaRepository.existsById(docaId)) {
            throw new ObjetoNaoEncontradoException("Doca com o código informado não existe: " + docaId);
        }

        return agendamentoRepository.listarAgendaPorDocaEData(docaId, data.atStartOfDay(), data.plusDays(1).atStartOfDay())
                .stream()
                .map(AgendamentoResponseDto::new)
                .toList();
    }

    @Transactional
    public void realizarCheckIn(Long agendamentoId) {
        buscarEntidade(agendamentoId).fazerCheckIn(LocalDateTime.now(clock));
    }

    @Transactional
    public void finalizar(Long agendamentoId) {
        buscarEntidade(agendamentoId).finalizar();
    }

    @Transactional
    public void cancelar(Long agendamentoId, String motivo) {
        buscarEntidade(agendamentoId).cancelar(motivo);
    }

    @Transactional
    public AgendamentoResponseDto criarAgendamento(AgendamentoRequestDto dto) {
        Fornecedor fornecedor = fornecedorRepository.findById(dto.fornecedorId())
                .orElseThrow(() -> new ObjetoNaoEncontradoException("Fornecedor não encontrado: " + dto.fornecedorId()));
        Doca doca = docaRepository.findById(dto.docaId())
                .orElseThrow(() -> new ObjetoNaoEncontradoException("Doca não encontrada: " + dto.docaId()));

        if (!doca.isAtiva()) {
            throw new RegraNegocioException("A doca informada está inativa.");
        }
        if (dto.tipoCarga() == TipoCarga.REFRIGERADA && doca.getTipo() != TipoDoca.REFRIGERADA) {
            throw new RegraNegocioException("Carga refrigerada exige doca refrigerada.");
        }

        LocalDateTime agora = LocalDateTime.now(clock);
        LocalDateTime inicio = dto.inicio();
        validarInicio(inicio, agora);
        LocalDateTime fim = calcularFim(inicio, dto.quantidadePaletes());

        if (agendamentoRepository.existeConflitoHorario(doca.getId(), inicio, fim, StatusAgendamento.CANCELADO)) {
            throw new ConflitoException("A doca selecionada já possui um agendamento neste intervalo de horário.");
        }

        Agendamento agendamento = new Agendamento(agora);
        agendamento.setFornecedor(fornecedor);
        agendamento.setDoca(doca);
        agendamento.setPlacaVeiculo(PlacaValidator.normalizar(dto.placaVeiculo()));
        agendamento.setQuantidadePaletes(dto.quantidadePaletes());
        agendamento.setTipoCarga(dto.tipoCarga());
        agendamento.setInicio(inicio);
        agendamento.setFim(fim);

        return new AgendamentoResponseDto(agendamentoRepository.save(agendamento));
    }

    private Agendamento buscarEntidade(Long id) {
        return agendamentoRepository.findById(id)
                .orElseThrow(() -> new ObjetoNaoEncontradoException("Agendamento não encontrado"));
    }

    private void validarInicio(LocalDateTime inicio, LocalDateTime agora) {
        if (inicio == null) {
            throw new RegraNegocioException("O início é obrigatório.");
        }
        if (inicio.isBefore(agora.plusHours(24))) {
            throw new RegraNegocioException("O agendamento exige pelo menos 24 horas de antecedência.");
        }
        if (inicio.getMinute() % 15 != 0 || inicio.getSecond() != 0 || inicio.getNano() != 0) {
            throw new RegraNegocioException("O início deve cair em um intervalo de 15 minutos, sem segundos ou frações.");
        }
        if (inicio.getDayOfWeek() == DayOfWeek.SUNDAY) {
            throw new RegraNegocioException("O estabelecimento não abre neste dia.");
        }
        if (inicio.toLocalTime().isBefore(ABERTURA) || inicio.toLocalTime().isAfter(FECHAMENTO)) {
            throw new RegraNegocioException("Horário inválido. Funcionamos das 06:00 às 18:00.");
        }
    }

    private LocalDateTime calcularFim(LocalDateTime inicio, int quantidadePaletes) {
        if (quantidadePaletes < 1 || quantidadePaletes > 26) {
            throw new RegraNegocioException("A quantidade deve estar entre 1 e 26 paletes.");
        }
        LocalDateTime fim = inicio.plusMinutes(30L + 5L * quantidadePaletes);
        if (fim.isAfter(inicio.toLocalDate().atTime(FECHAMENTO))) {
            throw new RegraNegocioException("A descarga terminaria após o fechamento às 18:00.");
        }
        return fim;
    }
}
