package com.estudiHambre.exception;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorRespuesta> recursoNoEncontrado(
            RecursoNoEncontradoException ex,
            HttpServletRequest request) {

        return crearRespuesta(
                HttpStatus.NOT_FOUND,
                ex.getMessage(),
                request
        );
    }

    @ExceptionHandler(RecursoDuplicadoException.class)
    public ResponseEntity<ErrorRespuesta> recursoDuplicado(
            RecursoDuplicadoException ex,
            HttpServletRequest request) {

        return crearRespuesta(
                HttpStatus.CONFLICT,
                ex.getMessage(),
                request
        );
    }

    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<ErrorRespuesta> reglaNegocio(
            ReglaNegocioException ex,
            HttpServletRequest request) {

        return crearRespuesta(
                HttpStatus.BAD_REQUEST,
                ex.getMessage(),
                request
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorRespuesta> manejarValidaciones(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        String mensaje = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .distinct()
                .collect(Collectors.joining("; "));

        return crearRespuesta(
                HttpStatus.BAD_REQUEST,
                mensaje,
                request
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorRespuesta> accesoDenegado(
            AccessDeniedException ex,
            HttpServletRequest request) {

        return crearRespuesta(
                HttpStatus.FORBIDDEN,
                "No tienes permisos para realizar esta operación",
                request
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorRespuesta> errorInesperado(
            Exception ex,
            HttpServletRequest request) {

        logger.error(
                "Error inesperado en {}",
                request.getRequestURI(),
                ex
        );

        return crearRespuesta(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocurrió un error interno en el servidor",
                request
        );
    }

    private ResponseEntity<ErrorRespuesta> crearRespuesta(
            HttpStatus estado,
            String mensaje,
            HttpServletRequest request) {

        ErrorRespuesta error = new ErrorRespuesta(
                java.time.LocalDateTime.now(),
                estado.value(),
                estado.getReasonPhrase(),
                mensaje,
                request.getRequestURI()
        );

        return ResponseEntity.status(estado).body(error);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorRespuesta> credencialesIncorrectas(
            BadCredentialsException ex,
            HttpServletRequest request) {

        return crearRespuesta(
                HttpStatus.UNAUTHORIZED,
                "Correo o contraseña incorrectos",
                request
        );
    }


    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorRespuesta> jsonInvalido(
            HttpMessageNotReadableException ex,
            HttpServletRequest request) {

        return crearRespuesta(
                HttpStatus.BAD_REQUEST,
                "El contenido JSON enviado no es válido",
                request
        );
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorRespuesta> formatoNoSoportado(
            HttpMediaTypeNotSupportedException ex,
            HttpServletRequest request) {

        return crearRespuesta(
                HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                "El formato de la solicitud no es compatible",
                request
        );
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<ErrorRespuesta> parteFaltante(
            MissingServletRequestPartException ex,
            HttpServletRequest request) {

        return crearRespuesta(
                HttpStatus.BAD_REQUEST,
                "Falta una parte obligatoria de la solicitud: "
                        + ex.getRequestPartName(),
                request
        );
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorRespuesta> archivoDemasiadoGrande(
            MaxUploadSizeExceededException ex,
            HttpServletRequest request) {

        return crearRespuesta(
                HttpStatus.PAYLOAD_TOO_LARGE,
                "El archivo supera el tamaño máximo permitido",
                request
        );
    }

}