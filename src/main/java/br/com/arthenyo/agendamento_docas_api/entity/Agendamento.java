package br.com.arthenyo.agendamento_docas_api.entity;

import java.time.LocalDateTime;

import br.com.arthenyo.agendamento_docas_api.entity.enums.StatusAgendamento;
import br.com.arthenyo.agendamento_docas_api.entity.enums.TipoCarga;
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
import jakarta.persistence.PrePersist;
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
 
    private String placaVeiculo;

    private LocalDateTime inicio;
    private LocalDateTime fim;

    private int quantidadePaletes;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TipoCarga tipoCarga;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private StatusAgendamento status;

    private String motivoCancelamento;

    private LocalDateTime criado_em;

    public Agendamento() {
    }

    @PrePersist
    public void prePersist() {
        this.criado_em = LocalDateTime.now();
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

    public void setStatus(StatusAgendamento status) {
        this.status = status;
    }

    public String getMotivoCancelamento() {
        return motivoCancelamento;
    }

    public void setMotivoCancelamento(String motivoCancelamento) {
        this.motivoCancelamento = motivoCancelamento;
    }

    public LocalDateTime getCriado_em() {
        return criado_em;
    }
}
