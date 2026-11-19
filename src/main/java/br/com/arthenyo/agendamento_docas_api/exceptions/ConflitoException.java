package br.com.arthenyo.agendamento_docas_api.exceptions;

public class ConflitoException extends RuntimeException {
    public ConflitoException(String message) {
        super(message);
    }
}
