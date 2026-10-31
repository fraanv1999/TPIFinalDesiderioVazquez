package ar.utn.tpi.listas.motor;

import java.util.List;
import java.util.Map;

/**
 * Una fila del Excel ya clasificada. {@code datosResultado} solo está presente si la fila es VALIDA,
 * así una fila con error nunca llega al archivo final con datos parciales.
 */
public record FilaResultado(int numeroFila,
                           EstadoFila estado,
                           MotivoIgnorada motivoIgnorada,
                           Map<String, Object> datosCrudos,
                           Map<String, Object> datosResultado,
                           List<ErrorCampo> errores) {

    static FilaResultado ignorada(int numero, MotivoIgnorada motivo, Map<String, Object> crudos) {
        return new FilaResultado(numero, EstadoFila.IGNORADA, motivo, crudos, null, List.of());
    }
}
