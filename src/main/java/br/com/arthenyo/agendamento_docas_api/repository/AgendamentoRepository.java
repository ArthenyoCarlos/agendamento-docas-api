package br.com.arthenyo.agendamento_docas_api.repository;

import br.com.arthenyo.agendamento_docas_api.entity.Agendamento;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgendamentoRepository extends JpaRepository<Agendamento, Long> {


}
