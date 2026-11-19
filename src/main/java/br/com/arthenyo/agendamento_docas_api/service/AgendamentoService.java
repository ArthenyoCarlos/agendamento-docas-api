package br.com.arthenyo.agendamento_docas_api.service;

import br.com.arthenyo.agendamento_docas_api.dto.AgendamentoRequestDto;
import br.com.arthenyo.agendamento_docas_api.dto.AgendamentoResponseDto;
import br.com.arthenyo.agendamento_docas_api.entity.Agendamento;
import br.com.arthenyo.agendamento_docas_api.entity.Doca;
import br.com.arthenyo.agendamento_docas_api.entity.Fornecedor;
import br.com.arthenyo.agendamento_docas_api.entity.enums.StatusAgendamento;
import br.com.arthenyo.agendamento_docas_api.exceptions.ConflitosDeDadosExceptions;
import br.com.arthenyo.agendamento_docas_api.exceptions.ObjetoNaoEncontradoException;
import br.com.arthenyo.agendamento_docas_api.exceptions.RegraNegocioException;
import br.com.arthenyo.agendamento_docas_api.repository.AgendamentoRepository;
import br.com.arthenyo.agendamento_docas_api.repository.DocaRepository;
import br.com.arthenyo.agendamento_docas_api.repository.FornecerdorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.List;
import java.util.Map;

@Service
public class AgendamentoService {

    private final AgendamentoRepository agendamentoRepository;
    private final FornecerdorRepository fornecerdorRepository;
    private final DocaRepository docaRepository;

    private static final ZoneId FUSO_PADRAO = ZoneId.of("America/Sao_Paulo");

    private static final Map<DayOfWeek, LocalTime[]> GRADE_HORARIOS = Map.of(
            DayOfWeek.MONDAY,    new LocalTime[]{LocalTime.of(6, 0), LocalTime.of(18, 0)},
            DayOfWeek.TUESDAY,   new LocalTime[]{LocalTime.of(6, 0), LocalTime.of(18, 0)},
            DayOfWeek.WEDNESDAY, new LocalTime[]{LocalTime.of(6, 0), LocalTime.of(18, 0)},
            DayOfWeek.THURSDAY,  new LocalTime[]{LocalTime.of(6, 0), LocalTime.of(18, 0)},
            DayOfWeek.FRIDAY,    new LocalTime[]{LocalTime.of(6, 0), LocalTime.of(18, 0)},
            DayOfWeek.SATURDAY,  new LocalTime[]{LocalTime.of(6, 0), LocalTime.of(12, 0)} // Ajuste o fechamento do sábado aqui
    );

    public AgendamentoService(AgendamentoRepository agendamentoRepository,
                              FornecerdorRepository fornecerdorRepository,
                              DocaRepository docaRepository) {
        this.agendamentoRepository = agendamentoRepository;
        this.fornecerdorRepository = fornecerdorRepository;
        this.docaRepository = docaRepository;
    }

    @Transactional(readOnly = true)
    public AgendamentoResponseDto buscarAgendamentoPorId(Long agendamentoId){
        Agendamento agendamento = agendamentoRepository.findById(agendamentoId)
                .orElseThrow(() -> new ObjetoNaoEncontradoException("Agendamento não encontrado"));

        return new AgendamentoResponseDto(agendamento);
    }

    @Transactional(readOnly = true)
    public List<AgendamentoResponseDto> listarAgendaDocaPorData(Long docaId, LocalDate data) {
        if (!docaRepository.existsById(docaId)) {
            throw new ObjetoNaoEncontradoException("Doca com o código informado não existe: " + docaId);
        }

        return agendamentoRepository.listarAgendaPorDocaEData(docaId, data)
                .stream()
                .map(AgendamentoResponseDto::new)
                .toList();
    }

    @Transactional
    public void realizarCheckIn(Long agendamentoId) {

        LocalDateTime agora = LocalDateTime.now(FUSO_PADRAO);

        Agendamento agendamento = agendamentoRepository.findById(agendamentoId)
                .orElseThrow(() -> new ObjetoNaoEncontradoException("Agendamento não encontrado"));


        if (agendamento.getStatus() != StatusAgendamento.AGENDADO) {
            throw new RegraNegocioException(
                    "Não é possível fazer check-in. Status atual: " + agendamento.getStatus()
            );
        }

        LocalDateTime limiteMinimo = agendamento.getInicio().minusMinutes(60);
        LocalDateTime limiteMaximo = agendamento.getInicio().plusMinutes(30);

        if (agora.isBefore(limiteMinimo) || agora.isAfter(limiteMaximo)) {
            throw new ConflitosDeDadosExceptions(
                    "Check-in fora da janela permitida (de " + limiteMinimo.toLocalTime() +
                            " até " + limiteMaximo.toLocalTime() + ")."
            );
        }

        agendamento.setStatus(StatusAgendamento.CHEGOU);
    }

    @Transactional
    public void finalizar(Long agendamentoId) {

        Agendamento agendamento = agendamentoRepository.findById(agendamentoId)
                .orElseThrow(() -> new ObjetoNaoEncontradoException("Agendamento não encontrado"));


        if (agendamento.getStatus() != StatusAgendamento.CHEGOU) {
            throw new RegraNegocioException(
                    "Não é possível finalizar o agendamento. O veículo precisa ter feito check-in (Status atual: " + agendamento.getStatus() + ")."
            );
        }

        agendamento.setStatus(StatusAgendamento.FINALIZADO);
    }

    @Transactional
    public void cancelar(Long agendamentoId, String motivo) {

        Agendamento agendamento = agendamentoRepository.findById(agendamentoId)
                .orElseThrow(() -> new ObjetoNaoEncontradoException("Agendamento não encontrado"));

        if (agendamento.getStatus() != StatusAgendamento.AGENDADO) {
            throw new RegraNegocioException(
                    "Não é possível cancelar um agendamento com status " + agendamento.getStatus() + "."
            );
        }

        if (motivo == null || motivo.trim().length() < 10) {
            throw new RegraNegocioException(
                    "O motivo do cancelamento é obrigatório e deve ter no mínimo 10 caracteres."
            );
        }

        agendamento.setMotivoCancelamento(motivo.trim());
        agendamento.setStatus(StatusAgendamento.CANCELADO);
    }

    @Transactional
    public AgendamentoResponseDto criarAgendamento(AgendamentoRequestDto dto) {
        Fornecedor fornecedor = fornecerdorRepository.findById(dto.fornecedorId())
                .orElseThrow(() -> new ObjetoNaoEncontradoException("Fornecedor com o codigo informado não existe: " + dto.fornecedorId()));

        Doca doca = docaRepository.findById(dto.docaId())
                .orElseThrow(() -> new ObjetoNaoEncontradoException("Doca com o codigo informado não existe: " + dto.docaId()));

        if (!doca.getTipo().toString().equals(dto.tipoCarga().toString())) {
            throw new RegraNegocioException("A doca não é do mesmo tipo da carga.");
        }

        validarAntecedenciaEMinutos(dto.inicio());

        validarHorarioFuncionamento(dto.inicio());

        LocalDateTime fimDescarga = validarECalcularFimDescarga(dto.inicio(), dto.quantidadePaletes());

        boolean temConflito = agendamentoRepository.existeConflitoHorario(
                doca.getId(),
                dto.inicio(),
                fimDescarga,
                StatusAgendamento.CANCELADO
        );

        if (temConflito) {
            throw new ConflitosDeDadosExceptions("A doca selecionada já possui um agendamento neste intervalo de horário.");
        }

        Agendamento agendamento = new Agendamento();
        agendamento.setFornecedor(fornecedor);
        agendamento.setDoca(doca);
        agendamento.setPlacaVeiculo(dto.placaVeiculo());
        agendamento.setQuantidadePaletes(dto.quantidadePaletes());
        agendamento.setTipoCarga(dto.tipoCarga());
        agendamento.setStatus(StatusAgendamento.AGENDADO);

        agendamento.setInicio(dto.inicio());
        agendamento.setFim(fimDescarga);

        agendamento = agendamentoRepository.save(agendamento);

        return new AgendamentoResponseDto(agendamento);
    }

    private LocalDateTime validarECalcularFimDescarga(LocalDateTime inicio, int quantidadePaletes) {
        DayOfWeek diaAgendamento = inicio.getDayOfWeek();

        LocalTime fechamento = GRADE_HORARIOS.get(diaAgendamento)[1];

        long minutosTotais = 30L + (quantidadePaletes * 5L);
        LocalDateTime fimDescarga = inicio.plusMinutes(minutosTotais);

        boolean virouODia = !fimDescarga.toLocalDate().equals(inicio.toLocalDate());

        if (virouODia || fimDescarga.toLocalTime().isAfter(fechamento)) {
            throw new ConflitosDeDadosExceptions(
                    "A descarga terminaria às " + fimDescarga.toLocalTime() +
                            ", ultrapassando o horário de fechamento (" + fechamento + ")."
            );
        }

        return fimDescarga;
    }

    private void validarAntecedenciaEMinutos(LocalDateTime inicio) {
        LocalDateTime agora = LocalDateTime.now(FUSO_PADRAO);

        if (!inicio.isAfter(agora.plusHours(24))) {
            throw new RegraNegocioException("O agendamento deve ser feito com mais de 24 horas de antecedência.");
        }

        int minutos = inicio.getMinute();
        if (!List.of(0, 15, 30, 45).contains(minutos)) {
            throw new RegraNegocioException("Os minutos do agendamento devem ser 00, 15, 30 ou 45.");
        }
    }

    private void validarHorarioFuncionamento(LocalDateTime inicio) {
        DayOfWeek diaAgendamento = inicio.getDayOfWeek();

        if (!GRADE_HORARIOS.containsKey(diaAgendamento)) {
            throw new RegraNegocioException("O estabelecimento não abre neste dia.");
        }

        LocalTime abertura = GRADE_HORARIOS.get(diaAgendamento)[0];
        LocalTime fechamento = GRADE_HORARIOS.get(diaAgendamento)[1];
        LocalTime horaAgendamento = inicio.toLocalTime();

        if (horaAgendamento.isBefore(abertura) || horaAgendamento.isAfter(fechamento)) {
            throw new RegraNegocioException(
                    "Horário inválido. Neste dia funcionamos das " + abertura + " às " + fechamento + "."
            );
        }
    }
}
