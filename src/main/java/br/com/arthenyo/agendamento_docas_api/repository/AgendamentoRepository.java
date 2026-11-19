package br.com.arthenyo.agendamento_docas_api.repository;

import br.com.arthenyo.agendamento_docas_api.entity.Agendamento;
import br.com.arthenyo.agendamento_docas_api.entity.enums.StatusAgendamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface AgendamentoRepository extends JpaRepository<Agendamento, Long> {

    @Query("""
        SELECT COUNT(a) > 0
        FROM Agendamento a
        WHERE a.doca.id = :docaId
          AND a.status <> :statusCancelado
          AND a.inicio < :fimNovo
          AND a.fim > :inicioNovo
    """)
    boolean existeConflitoHorario(
            @Param("docaId") Long docaId,
            @Param("inicioNovo") LocalDateTime inicioNovo,
            @Param("fimNovo") LocalDateTime fimNovo,
            @Param("statusCancelado") StatusAgendamento statusCancelado
    );

    @Query("""
    SELECT a FROM Agendamento a
    WHERE a.doca.id = :docaId
      AND CAST(a.inicio AS date) = :data
    ORDER BY a.inicio ASC
""")
    List<Agendamento> listarAgendaPorDocaEData(
            @Param("docaId") Long docaId,
            @Param("data") LocalDate data
    );


}
