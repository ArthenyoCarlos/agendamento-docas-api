package br.com.arthenyo.agendamento_docas_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.arthenyo.agendamento_docas_api.entity.Doca;

public interface DocaRepository extends JpaRepository<Doca, Long>{

    Doca findByAtivaTrue();
    
}
