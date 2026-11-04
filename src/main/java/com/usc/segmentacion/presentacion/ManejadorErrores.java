package com.usc.segmentacion.presentacion;

import com.usc.segmentacion.negocio.excepcion.EstructuraInvalidaException;
import com.usc.segmentacion.negocio.excepcion.SinRegistrosValidosException;
import com.usc.segmentacion.presentacion.dto.RespuestaError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

@RestControllerAdvice
public class ManejadorErrores {

    @ExceptionHandler(FormatoNoAdmitidoException.class)
    public ResponseEntity<RespuestaError> formatoNoAdmitido(FormatoNoAdmitidoException e) {
        return respuesta(HttpStatus.BAD_REQUEST, "formato_no_admitido", e.getMessage());
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<RespuestaError> archivoFaltante(MissingServletRequestPartException e) {
        return respuesta(HttpStatus.BAD_REQUEST, "archivo_requerido",
                "Debe enviar el archivo en el campo 'archivo'");
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<RespuestaError> archivoGrande(MaxUploadSizeExceededException e) {
        return respuesta(HttpStatus.PAYLOAD_TOO_LARGE, "archivo_demasiado_grande",
                "El archivo supera el tamaño máximo permitido (10 MB)");
    }

    @ExceptionHandler(EstructuraInvalidaException.class)
    public ResponseEntity<RespuestaError> estructuraInvalida(EstructuraInvalidaException e) {
        return respuesta(HttpStatus.UNPROCESSABLE_ENTITY, "estructura_invalida", e.getMessage());
    }

    @ExceptionHandler(SinRegistrosValidosException.class)
    public ResponseEntity<RespuestaError> sinRegistrosValidos(SinRegistrosValidosException e) {
        return respuesta(HttpStatus.UNPROCESSABLE_ENTITY, "sin_registros_validos", e.getMessage());
    }

    private static ResponseEntity<RespuestaError> respuesta(HttpStatus estado, String error, String detalle) {
        return ResponseEntity.status(estado).body(new RespuestaError(error, detalle));
    }
}