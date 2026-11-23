package cr.ac.ucr.if0009.insecurepay.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String BASE_TIPO = "https://insecurepay.cr/errors/";

    @ExceptionHandler(AccesoDenegadoException.class)
    public ResponseEntity<ProblemDetail> manejarAccesoDenegado(AccesoDenegadoException ex, HttpServletRequest request) {
        return construir(HttpStatus.FORBIDDEN, "Acceso no autorizado a recurso ajeno", ex.getMessage(), request);
    }

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ProblemDetail> manejarNoEncontrado(RecursoNoEncontradoException ex, HttpServletRequest request) {
        return construir(HttpStatus.NOT_FOUND, "Recurso no encontrado", ex.getMessage(), request);
    }

    @ExceptionHandler(TokenInvalidoException.class)
    public ResponseEntity<ProblemDetail> manejarTokenInvalido(TokenInvalidoException ex, HttpServletRequest request) {
        return construir(HttpStatus.UNAUTHORIZED, "Autenticacion requerida", ex.getMessage(), request);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ProblemDetail> manejarCredenciales(BadCredentialsException ex, HttpServletRequest request) {
        return construir(HttpStatus.UNAUTHORIZED, "Fallo de Autenticacion", "Usuario o clave incorrectos.", request);
    }

    @ExceptionHandler(ValidacionException.class)
    public ResponseEntity<ProblemDetail> manejarValidacion(ValidacionException ex, HttpServletRequest request) {
        return construir(HttpStatus.BAD_REQUEST, "Datos de entrada invalidos", ex.getMessage(), request);
    }

    private ResponseEntity<ProblemDetail> construir(HttpStatus estado, String titulo, String detalle, HttpServletRequest request) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(estado, detalle);
        pd.setTitle(titulo);
        pd.setType(URI.create(BASE_TIPO + estado.value()));
        pd.setInstance(URI.create(request.getRequestURI()));
        pd.setProperty("timestamp", Instant.now());
        return ResponseEntity.status(estado).body(pd);
    }
}
