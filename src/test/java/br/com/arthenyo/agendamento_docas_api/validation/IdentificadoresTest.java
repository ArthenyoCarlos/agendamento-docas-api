package br.com.arthenyo.agendamento_docas_api.validation;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class IdentificadoresTest {

    @Test
    void validaENormalizaCnpjNumericoEAlfanumerico() {
        CnpjValidator validator = new CnpjValidator();

        assertThat(validator.isValid("24.292.765/0001-00", null)).isTrue();
        assertThat(validator.isValid("12.abc.345/01de-35", null)).isTrue();
        assertThat(validator.isValid("12.ABC.345/01DE-36", null)).isFalse();
        assertThat(CnpjValidator.normalizar(" 12.abc.345/01de-35 ")).isEqualTo("12ABC34501DE35");
    }

    @Test
    void aceitaHifenApenasNaPlacaAntiga() {
        PlacaValidator validator = new PlacaValidator();

        assertThat(validator.isValid("abc-1234", null)).isTrue();
        assertThat(validator.isValid("abc1d23", null)).isTrue();
        assertThat(validator.isValid("ABC-1D23", null)).isFalse();
        assertThat(PlacaValidator.normalizar(" abc-1234 ")).isEqualTo("ABC1234");
    }
}
