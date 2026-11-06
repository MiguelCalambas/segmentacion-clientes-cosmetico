package com.usc.segmentacion.presentacion;

import com.usc.segmentacion.negocio.excepcion.DatosInsuficientesException;
import com.usc.segmentacion.negocio.excepcion.EjecucionNoCompletadaException;
import com.usc.segmentacion.negocio.excepcion.EstructuraInvalidaException;
import com.usc.segmentacion.negocio.excepcion.ParametroInvalidoException;
import com.usc.segmentacion.negocio.excepcion.RecursoNoEncontradoException;
import com.usc.segmentacion.negocio.excepcion.SinRegistrosValidosException;
import com.usc.segmentacion.presentacion.dto.RespuestaError;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

@RestControllerAdvice
public class ManejadorErrores {

    // ----- 400: la solicitud está mal formada -----

    @ExceptionHandler(FormatoNoAdmitidoException.class)
    public ResponseEntity<RespuestaError> formatoNoAdmitido(FormatoNoAdmitidoException e) {
        return respuesta(400, "formato_no_admitido", e.getMessage());
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<RespuestaError> archivoFaltante(MissingServletRequestPartException e) {
        return respuesta(400, "archivo_requerido", "Debe enviar el archivo en el campo 'archivo'");
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<RespuestaError> jsonInvalido(HttpMessageNotReadableException e) {
        return respuesta(400, "json_invalido", "El cuerpo de la solicitud no es un JSON válido");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<RespuestaError> rutaInvalida(MethodArgumentTypeMismatchException e) {
        return respuesta(400, "parametro_de_ruta_invalido",
                "El valor del parámetro '" + e.getName() + "' no es válido");
    }

    // ----- 404 y 409 -----

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<RespuestaError> noEncontrado(RecursoNoEncontradoException e) {
        return respuesta(404, "no_encontrado", e.getMessage());
    }

    @ExceptionHandler(EjecucionNoCompletadaException.class)
    public ResponseEntity<RespuestaError> ejecucionNoCompletada(EjecucionNoCompletadaException e) {
        return respuesta(409, "ejecucion_no_completada", e.getMessage());
    }

    // ----- 413 -----

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<RespuestaError> archivoGrande(MaxUploadSizeExceededException e) {
        return respuesta(413, "archivo_demasiado_grande", "El archivo supera el tamaño máximo permitido (10 MB)");
    }

    // ----- 422: la solicitud es válida, pero no se puede procesar -----

    @ExceptionHandler(EstructuraInvalidaException.class)
    public ResponseEntity<RespuestaError> estructuraInvalida(EstructuraInvalidaException e) {
        return respuesta(422, "estructura_invalida", e.getMessage());
    }

    @ExceptionHandler(SinRegistrosValidosException.class)
    public ResponseEntity<RespuestaError> sinRegistrosValidos(SinRegistrosValidosException e) {
        return respuesta(422, "sin_registros_validos", e.getMessage());
    }

    @ExceptionHandler(ParametroInvalidoException.class)
    public ResponseEntity<RespuestaError> parametroInvalido(ParametroInvalidoException e) {
        return respuesta(422, "parametro_invalido", e.getMessage());
    }

    @ExceptionHandler(DatosInsuficientesException.class)
    public ResponseEntity<RespuestaError> datosInsuficientes(DatosInsuficientesException e) {
        return respuesta(422, "datos_insuficientes", e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RespuestaError> solicitudInvalida(MethodArgumentNotValidException e) {
        String detalle = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return respuesta(422, "solicitud_invalida", detalle);
    }

    private static ResponseEntity<RespuestaError> respuesta(int codigo, String error, String detalle) {
        return ResponseEntity.status(codigo).body(new RespuestaError(error, detalle));
    }
}