package br.com.arthenyo.agendamento_docas_api.controller;

import br.com.arthenyo.agendamento_docas_api.exceptions.ConflitoException;
import br.com.arthenyo.agendamento_docas_api.exceptions.ObjetoNaoEncontradoException;
import br.com.arthenyo.agendamento_docas_api.exceptions.RegraNegocioException;
import br.com.arthenyo.agendamento_docas_api.service.AgendamentoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AgendamentoController.class)
class AgendamentoControllerTest {

    @Autowired
    private MockMvc mvc;
    @MockitoBean
    private AgendamentoService service;

    @ParameterizedTest
    @ValueSource(ints = {-1, 0, 27})
    void rejeitaQuantidadeForaDosLimites(int paletes) throws Exception {
        mvc.perform(post("/agendamentos").contentType(MediaType.APPLICATION_JSON).content("""
                {
                  "fornecedorId": 1, "docaId": 2, "placaVeiculo": "ABC1D23",
                  "inicio": "2026-11-23T10:00:00", "tipoCarga": "SECA", "quantidadePaletes": %d
                }
                """.formatted(paletes)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.erros[*].campo", hasItem("quantidadePaletes")));
        verifyNoInteractions(service);
    }

    @Test
    void rejeitaCriacaoSemInicio() throws Exception {
        mvc.perform(post("/agendamentos").contentType(MediaType.APPLICATION_JSON).content("""
                {
                  "fornecedorId": 1, "docaId": 2, "placaVeiculo": "ABC1D23",
                  "tipoCarga": "SECA", "quantidadePaletes": 6
                }
                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros[*].campo", hasItem("inicio")));
        verifyNoInteractions(service);
    }

    @Test
    void rejeitaMotivoCurtoMesmoComEspacos() throws Exception {
        mvc.perform(patch("/agendamentos/1/cancelar").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"motivo": "       curto       "}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros[*].campo", hasItem("motivo")));
        verifyNoInteractions(service);
    }

    @Test
    void responde422ParaRegraDeNegocio() throws Exception {
        verificarProblema(new RegraNegocioException("Check-in fora da janela."), 422, "Check-in fora da janela.");
    }

    @Test
    void responde404ParaAgendamentoInexistente() throws Exception {
        verificarProblema(new ObjetoNaoEncontradoException("Agendamento não encontrado."), 404,
                "Agendamento não encontrado.");
    }

    @Test
    void responde409ParaConflito() throws Exception {
        verificarProblema(new ConflitoException("Conflito de horário."), 409, "Conflito de horário.");
    }

    @Test
    void responde409ParaViolacaoDeIntegridadeSemExporDetalhesDoBanco() throws Exception {
        verificarProblema(new DataIntegrityViolationException("Mensagem interna"), 409,
                "Registro duplicado ou inconsistente.");
    }

    private void verificarProblema(RuntimeException excecao, int codigo, String detalhe) throws Exception {
        doThrow(excecao).when(service).realizarCheckIn(1L);
        mvc.perform(patch("/agendamentos/1/check-in"))
                .andExpect(status().is(codigo))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(codigo))
                .andExpect(jsonPath("$.detail").value(detalhe))
                .andExpect(jsonPath("$.instance").value("/agendamentos/1/check-in"));
    }
}
