package br.com.arthenyo.agendamento_docas_api.service;

import br.com.arthenyo.agendamento_docas_api.dto.AgendamentoRequestDto;
import br.com.arthenyo.agendamento_docas_api.dto.AgendamentoResponseDto;
import br.com.arthenyo.agendamento_docas_api.entity.Agendamento;
import br.com.arthenyo.agendamento_docas_api.entity.Doca;
import br.com.arthenyo.agendamento_docas_api.entity.Fornecedor;
import br.com.arthenyo.agendamento_docas_api.entity.enums.TipoCarga;
import br.com.arthenyo.agendamento_docas_api.entity.enums.TipoDoca;
import br.com.arthenyo.agendamento_docas_api.exceptions.ConflitoException;
import br.com.arthenyo.agendamento_docas_api.exceptions.RegraNegocioException;
import br.com.arthenyo.agendamento_docas_api.repository.AgendamentoRepository;
import br.com.arthenyo.agendamento_docas_api.repository.DocaRepository;
import br.com.arthenyo.agendamento_docas_api.repository.FornecedorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import static br.com.arthenyo.agendamento_docas_api.entity.enums.StatusAgendamento.AGENDADO;
import static br.com.arthenyo.agendamento_docas_api.entity.enums.StatusAgendamento.CANCELADO;
import static br.com.arthenyo.agendamento_docas_api.entity.enums.StatusAgendamento.CHEGOU;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgendamentoServiceTest {

    private static final long FORNECEDOR_ID = 1L;
    private static final long DOCA_ID = 2L;
    private static final long AGENDAMENTO_ID = 3L;
    private static final LocalDateTime AGORA = LocalDateTime.of(2026, 11, 19, 9, 0);
    private static final LocalDateTime SEGUNDA_AS_DEZ = LocalDateTime.of(2026, 11, 23, 10, 0);

    @Mock
    private AgendamentoRepository agendamentoRepository;
    @Mock
    private FornecedorRepository fornecedorRepository;
    @Mock
    private DocaRepository docaRepository;

    private AgendamentoService service;

    @BeforeEach
    void configurarService() {
        Clock clock = Clock.fixed(Instant.parse("2026-11-19T12:00:00Z"), ZoneId.of("America/Sao_Paulo"));
        service = new AgendamentoService(agendamentoRepository, fornecedorRepository, docaRepository, clock);
    }

    @ParameterizedTest(name = "RN01 - {0} paletes levam {1} minutos")
    @CsvSource({"1, 35", "6, 60", "12, 90", "26, 160"})
    void calculaDuracaoEmFuncaoDaQuantidadeDePaletes(int paletes, long duracaoEsperada) {
        simularFornecedorEDocaSeca();
        when(agendamentoRepository.save(any(Agendamento.class))).thenAnswer(invocacao -> invocacao.getArgument(0));

        AgendamentoResponseDto resposta = service.criarAgendamento(pedido(SEGUNDA_AS_DEZ, paletes, TipoCarga.SECA));

        LocalDateTime fimEsperado = SEGUNDA_AS_DEZ.plusMinutes(duracaoEsperada);
        ArgumentCaptor<Agendamento> agendamentoSalvo = ArgumentCaptor.forClass(Agendamento.class);
        verify(agendamentoRepository).save(agendamentoSalvo.capture());
        assertThat(agendamentoSalvo.getValue().getFim()).isEqualTo(fimEsperado);
        assertThat(agendamentoSalvo.getValue().getCriadoEm()).isEqualTo(AGORA);
        assertThat(resposta.fim()).isEqualTo(fimEsperado);
        assertThat(resposta.placaVeiculo()).isEqualTo("ABC1D23");
    }

    @Test
    void rn05RejeitaSobreposicaoComAgendamentoQueTerminaAsDez() {
        simularFornecedorEDocaSeca();
        LocalDateTime inicio = SEGUNDA_AS_DEZ.minusMinutes(15);
        when(agendamentoRepository.existeConflitoHorario(DOCA_ID, inicio, inicio.plusHours(1), CANCELADO))
                .thenReturn(true);

        assertThatThrownBy(() -> service.criarAgendamento(pedido(inicio, 6, TipoCarga.SECA)))
                .isInstanceOf(ConflitoException.class)
                .hasMessageContaining("possui um agendamento neste intervalo");
        verify(agendamentoRepository, never()).save(any(Agendamento.class));
    }

    @Test
    void rn05AceitaInicioAsDezQuandoAgendamentoAnteriorTerminaAsDez() {
        simularFornecedorEDocaSeca();
        LocalDateTime fim = SEGUNDA_AS_DEZ.plusHours(1);
        when(agendamentoRepository.existeConflitoHorario(DOCA_ID, SEGUNDA_AS_DEZ, fim, CANCELADO))
                .thenReturn(false);
        when(agendamentoRepository.save(any(Agendamento.class))).thenAnswer(invocacao -> invocacao.getArgument(0));

        AgendamentoResponseDto resposta = service.criarAgendamento(pedido(SEGUNDA_AS_DEZ, 6, TipoCarga.SECA));

        verify(agendamentoRepository).existeConflitoHorario(DOCA_ID, SEGUNDA_AS_DEZ, fim, CANCELADO);
        verify(agendamentoRepository).save(any(Agendamento.class));
        assertThat(resposta.inicio()).isEqualTo(SEGUNDA_AS_DEZ);
        assertThat(resposta.status()).isEqualTo(AGENDADO);
    }

    @Test
    void rn06RejeitaCargaRefrigeradaEmDocaSeca() {
        simularFornecedorEDocaSeca();

        assertThatThrownBy(() -> service.criarAgendamento(pedido(SEGUNDA_AS_DEZ, 6, TipoCarga.REFRIGERADA)))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("Carga refrigerada exige doca refrigerada");
        verifyNoInteractions(agendamentoRepository);
    }

    @Test
    void rn06AceitaCargaSecaEmDocaRefrigerada() {
        simularFornecedorEDoca(TipoDoca.REFRIGERADA, true);
        when(agendamentoRepository.save(any(Agendamento.class))).thenAnswer(invocacao -> invocacao.getArgument(0));

        assertThat(service.criarAgendamento(pedido(SEGUNDA_AS_DEZ, 6, TipoCarga.SECA)).tipoCarga())
                .isEqualTo(TipoCarga.SECA);
    }

    @Test
    void rn07RejeitaDocaInativa() {
        simularFornecedorEDoca(TipoDoca.SECA, false);

        assertThatThrownBy(() -> service.criarAgendamento(pedido(SEGUNDA_AS_DEZ, 6, TipoCarga.SECA)))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("inativa");
        verifyNoInteractions(agendamentoRepository);
    }

    @Test
    void rn02AceitaAntecedenciaDeExatamente24Horas() {
        simularFornecedorEDocaSeca();
        when(agendamentoRepository.save(any(Agendamento.class))).thenAnswer(invocacao -> invocacao.getArgument(0));
        LocalDateTime inicio = AGORA.plusHours(24);

        assertThat(service.criarAgendamento(pedido(inicio, 6, TipoCarga.SECA)).inicio()).isEqualTo(inicio);
    }

    @Test
    void rn02RejeitaAntecedenciaMenorQue24Horas() {
        simularFornecedorEDocaSeca();

        assertThatThrownBy(() -> service.criarAgendamento(pedido(AGORA.plusHours(24).minusMinutes(15), 6, TipoCarga.SECA)))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("24 horas");
        verifyNoInteractions(agendamentoRepository);
    }

    @ParameterizedTest
    @ValueSource(strings = {"2026-11-23T10:01", "2026-11-23T10:15:37", "2026-11-23T10:15:00.001"})
    void rn04ExigeIntervalosDeQuinzeMinutos(String inicio) {
        simularFornecedorEDocaSeca();

        assertThatThrownBy(() -> service.criarAgendamento(pedido(LocalDateTime.parse(inicio), 6, TipoCarga.SECA)))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("15 minutos");
        verifyNoInteractions(agendamentoRepository);
    }

    @Test
    void rn03AceitaDescargaNoSabadoTerminandoExatamenteAsDezoito() {
        simularFornecedorEDocaSeca();
        when(agendamentoRepository.save(any(Agendamento.class))).thenAnswer(invocacao -> invocacao.getArgument(0));
        LocalDateTime inicio = LocalDateTime.of(2026, 11, 21, 17, 0);

        assertThat(service.criarAgendamento(pedido(inicio, 6, TipoCarga.SECA)).fim()).isEqualTo(inicio.plusHours(1));
    }

    @Test
    void rn03RejeitaDescargaQueUltrapassaOFechamento() {
        simularFornecedorEDocaSeca();

        assertThatThrownBy(() -> service.criarAgendamento(pedido(SEGUNDA_AS_DEZ.withHour(17).withMinute(15), 6, TipoCarga.SECA)))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("fechamento");
        verifyNoInteractions(agendamentoRepository);
    }

    @ParameterizedTest(name = "RN03 - rejeita {0}")
    @CsvSource({
            "2026-11-23T05:45, Horário inválido",
            "2026-11-23T18:15, Horário inválido",
            "2026-11-21T18:15, Horário inválido",
            "2026-11-22T10:00, estabelecimento não abre neste dia"
    })
    void rejeitaHorarioForaDoExpedienteEDomingo(String inicio, String mensagemEsperada) {
        simularFornecedorEDocaSeca();
        AgendamentoRequestDto dto = pedido(LocalDateTime.parse(inicio), 6, TipoCarga.SECA);

        assertThatThrownBy(() -> service.criarAgendamento(dto))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining(mensagemEsperada);
        verifyNoInteractions(agendamentoRepository);
    }

    @Test
    void rn11RejeitaFinalizacaoDeAgendamentoSemCheckIn() {
        Agendamento agendamento = new Agendamento(AGORA);
        when(agendamentoRepository.findById(AGENDAMENTO_ID)).thenReturn(Optional.of(agendamento));

        assertThatThrownBy(() -> service.finalizar(AGENDAMENTO_ID))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("finalizar com status AGENDADO");
        assertThat(agendamento.getStatus()).isEqualTo(AGENDADO);
        verify(agendamentoRepository, never()).save(any(Agendamento.class));
    }

    @ParameterizedTest
    @ValueSource(ints = {-30, 60})
    void rn10AceitaCheckInNosLimitesDaJanela(int minutosAteInicio) {
        Agendamento agendamento = new Agendamento(AGORA.minusDays(2));
        agendamento.setInicio(AGORA.plusMinutes(minutosAteInicio));
        when(agendamentoRepository.findById(AGENDAMENTO_ID)).thenReturn(Optional.of(agendamento));

        service.realizarCheckIn(AGENDAMENTO_ID);

        assertThat(agendamento.getStatus()).isEqualTo(CHEGOU);
    }

    @ParameterizedTest
    @ValueSource(ints = {-31, 61})
    void rn10RejeitaCheckInForaDaJanela(int minutosAteInicio) {
        Agendamento agendamento = new Agendamento(AGORA.minusDays(2));
        agendamento.setInicio(AGORA.plusMinutes(minutosAteInicio));
        when(agendamentoRepository.findById(AGENDAMENTO_ID)).thenReturn(Optional.of(agendamento));

        assertThatThrownBy(() -> service.realizarCheckIn(AGENDAMENTO_ID))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("Check-in permitido");
        assertThat(agendamento.getStatus()).isEqualTo(AGENDADO);
    }

    @Test
    void cancelaAgendamentoComMotivoValido() {
        Agendamento agendamento = new Agendamento(AGORA);
        when(agendamentoRepository.findById(AGENDAMENTO_ID)).thenReturn(Optional.of(agendamento));

        service.cancelar(AGENDAMENTO_ID, "  Entrega desmarcada  ");

        assertThat(agendamento.getStatus()).isEqualTo(CANCELADO);
        assertThat(agendamento.getMotivoCancelamento()).isEqualTo("Entrega desmarcada");
    }

    @Test
    void rejeitaCancelamentoComMotivoCurto() {
        Agendamento agendamento = new Agendamento(AGORA);
        when(agendamentoRepository.findById(AGENDAMENTO_ID)).thenReturn(Optional.of(agendamento));

        assertThatThrownBy(() -> service.cancelar(AGENDAMENTO_ID, "  curto  "))
                .isInstanceOf(RegraNegocioException.class);
        assertThat(agendamento.getStatus()).isEqualTo(AGENDADO);
        assertThat(agendamento.getMotivoCancelamento()).isNull();
    }

    private void simularFornecedorEDocaSeca() {
        simularFornecedorEDoca(TipoDoca.SECA, true);
    }

    private void simularFornecedorEDoca(TipoDoca tipo, boolean ativa) {
        Doca doca = new Doca(DOCA_ID, "D02", tipo, ativa);
        when(fornecedorRepository.findById(FORNECEDOR_ID)).thenReturn(Optional.of(new Fornecedor()));
        when(docaRepository.findById(DOCA_ID)).thenReturn(Optional.of(doca));
    }

    private AgendamentoRequestDto pedido(LocalDateTime inicio, int paletes, TipoCarga tipoCarga) {
        return new AgendamentoRequestDto(FORNECEDOR_ID, DOCA_ID, "abc1d23", inicio, paletes, tipoCarga);
    }
}
