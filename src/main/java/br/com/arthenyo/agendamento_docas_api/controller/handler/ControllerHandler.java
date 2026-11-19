package br.com.arthenyo.agendamento_docas_api.controller.handler;

import br.com.arthenyo.agendamento_docas_api.exceptions.ConflitoException;
import br.com.arthenyo.agendamento_docas_api.exceptions.ObjetoNaoEncontradoException;
import br.com.arthenyo.agendamento_docas_api.exceptions.RegraNegocioException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Map;
import java.util.Objects;

@RestControllerAdvice
public class ControllerHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(ObjetoNaoEncontradoException.class)
    public ProblemDetail handleObjetoNaoEncontrado(ObjetoNaoEncontradoException ex) {
        return problema(HttpStatus.NOT_FOUND, "Recurso não encontrado", ex.getMessage());
    }

    @ExceptionHandler(ConflitoException.class)
    public ProblemDetail handleConflito(ConflitoException ex) {
        return problema(HttpStatus.CONFLICT, "Conflito", ex.getMessage());
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ProblemDetail handleRegraNegocio(RegraNegocioException ex) {
        return problema(HttpStatus.UNPROCESSABLE_CONTENT, "Regra de negócio violada", ex.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleIntegridade(DataIntegrityViolationException ex) {
        return problema(HttpStatus.CONFLICT, "Conflito", "Registro duplicado ou inconsistente.");
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers, HttpStatusCode status,
                                                                  WebRequest request) {
        ProblemDetail resposta = problema(HttpStatus.BAD_REQUEST, "Dados inválidos", "Um ou mais dados são inválidos.");
        resposta.setProperty("erros", ex.getBindingResult().getAllErrors().stream()
                .map(erro -> Map.of(
                        "campo", erro instanceof FieldError campo ? campo.getField() : erro.getObjectName(),
                        "mensagem", Objects.requireNonNullElse(erro.getDefaultMessage(), "Inválido")))
                .toList());
        return handleExceptionInternal(ex, resposta, headers, status, request);
    }

    private ProblemDetail problema(HttpStatus status, String titulo, String detalhe) {
        ProblemDetail resposta = ProblemDetail.forStatusAndDetail(status, detalhe);
        resposta.setTitle(titulo);
        return resposta;
    }
}
