package br.com.arthenyo.agendamento_docas_api.exceptions;

public class ObjetoNaoEncontradoException extends RuntimeException {
    public ObjetoNaoEncontradoException(String message) {
        super(message);
    }
}
