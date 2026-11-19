package br.com.arthenyo.agendamento_docas_api.repository;

import br.com.arthenyo.agendamento_docas_api.entity.Fornecedor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FornecedorRepository extends JpaRepository<Fornecedor, Long> {

    boolean existsByCnpj(String cnpj);
}
