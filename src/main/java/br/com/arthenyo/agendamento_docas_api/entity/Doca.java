package br.com.arthenyo.agendamento_docas_api.entity;

import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

import br.com.arthenyo.agendamento_docas_api.entity.enums.TipoDoca;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity 
@Table(name = "tb_doca")
public class Doca {

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Generated(event = EventType.INSERT)
    @Column(name = "codigo", insertable = false, updatable = false)
    private String codigo;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TipoDoca tipo;

    @Column(nullable = false)
    private boolean ativa = true;

    public Doca() {
    }

    public Doca(Long id, String codigo, TipoDoca tipo, boolean ativa) {
        this.id = id;
        this.codigo = codigo;
        this.tipo = tipo;
        this.ativa = ativa;
    }

    public Long getId() {
        return id;
    }

    public String getCodigo() {
        return codigo;
    }

    public TipoDoca getTipo() {
        return tipo;
    }

    public void setTipo(TipoDoca tipo) {
        this.tipo = tipo;
    }

    public boolean isAtiva() {
        return ativa;
    }

    public void setAtiva(boolean ativa) {
        this.ativa = ativa;
    }
}
