package com.usc.segmentacion.negocio.modelo;

import java.util.List;

public record ConsultaSegmentos(Long idEjecucion, String estado, List<SegmentoConsultado> segmentos) {
}