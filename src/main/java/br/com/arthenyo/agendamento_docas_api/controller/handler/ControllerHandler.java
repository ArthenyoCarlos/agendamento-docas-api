package br.com.arthenyo.agendamento_docas_api.controller.handler;

import br.com.arthenyo.agendamento_docas_api.exceptions.CodigoErro;
import br.com.arthenyo.agendamento_docas_api.exceptions.ConflitosDeDadosExceptions;
import br.com.arthenyo.agendamento_docas_api.exceptions.ErroResposta;
import br.com.arthenyo.agendamento_docas_api.exceptions.ObjetoNaoEncontradoException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class ControllerHandler {

    @ExceptionHandler(ObjetoNaoEncontradoException.class)
    public ResponseEntity<ErroResposta> handleObjetoNaoEncontrado(ObjetoNaoEncontradoException ex) {
        ErroResposta erroResposta = new ErroResposta
                (
                        ex.getMessage(),
                        404,
                        System.currentTimeMillis(),
                        null,
                        CodigoErro.OBJETO_NAO_ENCONTRADO
                );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(erroResposta);
    }

    @ExceptionHandler(ConflitosDeDadosExceptions.class)
    public ResponseEntity<ErroResposta> handleComflitoDeDados(ConflitosDeDadosExceptions ex) {
        ErroResposta erroResposta = new ErroResposta
                (
                        ex.getMessage(),
                        409,
                        System.currentTimeMillis(),
                        null,
                        CodigoErro.CONFLITO_DADOS
                );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(erroResposta);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }
}
