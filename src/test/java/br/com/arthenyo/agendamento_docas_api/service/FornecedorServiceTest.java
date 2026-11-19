package br.com.arthenyo.agendamento_docas_api.service;

import br.com.arthenyo.agendamento_docas_api.dto.FornecedorRequestDto;
import br.com.arthenyo.agendamento_docas_api.entity.Fornecedor;
import br.com.arthenyo.agendamento_docas_api.exceptions.ConflitoException;
import br.com.arthenyo.agendamento_docas_api.repository.FornecedorRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FornecedorServiceTest {

    @Mock
    private FornecedorRepository repository;

    @Test
    void salvaCnpjNormalizado() {
        when(repository.save(any(Fornecedor.class))).thenAnswer(invocacao -> invocacao.getArgument(0));
        FornecedorService service = new FornecedorService(repository);

        assertThat(service.criarFornecedor(new FornecedorRequestDto("12.abc.345/01de-35", "Fornecedor")).cnpj())
                .isEqualTo("12ABC34501DE35");
        verify(repository).existsByCnpj("12ABC34501DE35");
    }

    @Test
    void rejeitaCnpjDuplicadoMesmoComMascara() {
        when(repository.existsByCnpj("24292765000100")).thenReturn(true);
        FornecedorService service = new FornecedorService(repository);

        assertThatThrownBy(() -> service.criarFornecedor(new FornecedorRequestDto("24.292.765/0001-00", "Fornecedor")))
                .isInstanceOf(ConflitoException.class);
        verify(repository, never()).save(any(Fornecedor.class));
    }
}
