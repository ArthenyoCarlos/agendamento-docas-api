package br.com.arthenyo.agendamento_docas_api.entity;

import java.time.LocalDateTime;

import br.com.arthenyo.agendamento_docas_api.entity.enums.StatusAgendamento;
import br.com.arthenyo.agendamento_docas_api.entity.enums.TipoCarga;
import br.com.arthenyo.agendamento_docas_api.exceptions.RegraNegocioException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity 
@Table(name = "tb_agendamento") 
public class Agendamento {


    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fornecedor_id",nullable = false) 
    private Fornecedor fornecedor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doca_id",nullable = false)
    private Doca doca;
 
    @Column(nullable = false)
    private String placaVeiculo;

    @Column(nullable = false)
    private LocalDateTime inicio;
    @Column(nullable = false)
    private LocalDateTime fim;

    private int quantidadePaletes;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TipoCarga tipoCarga;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private StatusAgendamento status;

    private String motivoCancelamento;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    public Agendamento() {
    }

    public Agendamento(LocalDateTime criadoEm) {
        this.criadoEm = criadoEm;
        this.status = StatusAgendamento.AGENDADO;
    }

    public void fazerCheckIn(LocalDateTime agora) {
        exigirStatus(StatusAgendamento.AGENDADO, "fazer check-in");
        LocalDateTime abre = inicio.minusMinutes(60);
        LocalDateTime fecha = inicio.plusMinutes(30);
        if (agora.isBefore(abre) || agora.isAfter(fecha)) {
            throw new RegraNegocioException("Check-in permitido de %s até %s.".formatted(abre, fecha));
        }
        status = StatusAgendamento.CHEGOU;
    }

    public void finalizar() {
        exigirStatus(StatusAgendamento.CHEGOU, "finalizar");
        status = StatusAgendamento.FINALIZADO;
    }

    public void cancelar(String motivo) {
        exigirStatus(StatusAgendamento.AGENDADO, "cancelar");
        String motivoNormalizado = motivo == null ? "" : motivo.trim();
        if (motivoNormalizado.length() < 10 || motivoNormalizado.length() > 255) {
            throw new RegraNegocioException("O motivo do cancelamento deve ter entre 10 e 255 caracteres.");
        }
        motivoCancelamento = motivoNormalizado;
        status = StatusAgendamento.CANCELADO;
    }

    private void exigirStatus(StatusAgendamento esperado, String acao) {
        if (status != esperado) {
            throw new RegraNegocioException("Não é possível %s com status %s.".formatted(acao, status));
        }
    }

    public Long getId() {
        return id;
    }

    public Fornecedor getFornecedor() {
        return fornecedor;
    }

    public void setFornecedor(Fornecedor fornecedor) {
        this.fornecedor = fornecedor;
    }

    public Doca getDoca() {
        return doca;
    }

    public void setDoca(Doca doca) {
        this.doca = doca;
    }

    public String getPlacaVeiculo() {
        return placaVeiculo;
    }

    public void setPlacaVeiculo(String placaVeiculo) {
        this.placaVeiculo = placaVeiculo;
    }

    public LocalDateTime getInicio() {
        return inicio;
    }

    public void setInicio(LocalDateTime inicio) {
        this.inicio = inicio;
    }

    public LocalDateTime getFim() {
        return fim;
    }

    public void setFim(LocalDateTime fim) {
        this.fim = fim;
    }

    public int getQuantidadePaletes() {
        return quantidadePaletes;
    }

    public void setQuantidadePaletes(int quantidadePaletes) {
        this.quantidadePaletes = quantidadePaletes;
    }

    public TipoCarga getTipoCarga() {
        return tipoCarga;
    }

    public void setTipoCarga(TipoCarga tipoCarga) {
        this.tipoCarga = tipoCarga;
    }

    public StatusAgendamento getStatus() {
        return status;
    }

    public String getMotivoCancelamento() {
        return motivoCancelamento;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }
}
