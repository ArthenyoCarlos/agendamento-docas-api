package br.com.arthenyo.agendamento_docas_api.repository;

import br.com.arthenyo.agendamento_docas_api.entity.Agendamento;
import br.com.arthenyo.agendamento_docas_api.entity.Doca;
import br.com.arthenyo.agendamento_docas_api.entity.Fornecedor;
import br.com.arthenyo.agendamento_docas_api.entity.enums.TipoCarga;
import br.com.arthenyo.agendamento_docas_api.entity.enums.TipoDoca;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.time.LocalDateTime;

import static br.com.arthenyo.agendamento_docas_api.entity.enums.StatusAgendamento.CANCELADO;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=validate")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers(disabledWithoutDocker = true)
class AgendamentoRepositoryTest {

    @Container
    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:18");

    @DynamicPropertySource
    static void configurarBanco(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired
    private AgendamentoRepository agendamentos;
    @Autowired
    private FornecedorRepository fornecedores;
    @Autowired
    private DocaRepository docas;

    @Test
    void rn05DetectaSobreposicaoEAceitaInicioNoMesmoInstanteDoFim() {
        LocalDateTime dezHoras = LocalDateTime.of(2026, 11, 23, 10, 0);
        Doca doca = docas.save(new Doca(null, null, TipoDoca.SECA, true));
        Agendamento existente = new Agendamento(dezHoras.minusDays(2));
        existente.setFornecedor(fornecedores.save(new Fornecedor(null, "24292765000100", "Fornecedor")));
        existente.setDoca(doca);
        existente.setPlacaVeiculo("ABC1D23");
        existente.setTipoCarga(TipoCarga.SECA);
        existente.setQuantidadePaletes(6);
        existente.setInicio(dezHoras.minusHours(1));
        existente.setFim(dezHoras);
        agendamentos.saveAndFlush(existente);

        assertThat(agendamentos.existeConflitoHorario(doca.getId(), dezHoras.minusMinutes(15),
                dezHoras.plusMinutes(45), CANCELADO)).isTrue();
        assertThat(agendamentos.existeConflitoHorario(doca.getId(), dezHoras,
                dezHoras.plusHours(1), CANCELADO)).isFalse();
        assertThat(agendamentos.listarAgendaPorDocaEData(doca.getId(), dezHoras.toLocalDate().atStartOfDay(),
                dezHoras.toLocalDate().plusDays(1).atStartOfDay())).containsExactly(existente);

        existente.cancelar("Entrega desmarcada");
        agendamentos.flush();
        assertThat(agendamentos.existeConflitoHorario(doca.getId(), dezHoras.minusMinutes(15),
                dezHoras.plusMinutes(45), CANCELADO)).isFalse();
    }
}
