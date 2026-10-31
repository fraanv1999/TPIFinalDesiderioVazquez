package ar.utn.tpi.listas.motor;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import ar.utn.tpi.listas.excel.LectorExcel;
import ar.utn.tpi.listas.excel.LibroExcel;
import ar.utn.tpi.listas.perfil.Perfil;
import ar.utn.tpi.listas.perfil.RepositorioPerfiles;

/** fuente → perfil → control de estructura → clasificación y transformación → preview. */
@Service
public class ServicioImportacion {

    private final RepositorioPerfiles repositorio;

    public ServicioImportacion(RepositorioPerfiles repositorio) {
        this.repositorio = repositorio;
    }

    public ResultadoImportacion preview(String perfilId, String nombreArchivo, InputStream archivo) {
        Perfil perfil = repositorio.perfil(perfilId);
        LibroExcel libro = LectorExcel.leer(archivo, perfil.hoja());
        Estructura estructura = ControlEstructura.verificar(perfil, libro);
        if (estructura.bloqueada()) {
            return new ResultadoImportacion(perfil.id(), nombreArchivo, EstadoImportacion.RECHAZADA,
                    estructura.diferencias(), new Resumen(0, 0, 0, 0, 0), List.of(), List.of());
        }
        List<FilaResultado> filas = MotorTransformacion.procesar(perfil, repositorio.formatoDe(perfil),
                libro.hoja().orElseThrow(), perfil.filaInicioDatos() + estructura.desplazamiento());
        return new ResultadoImportacion(perfil.id(), nombreArchivo, EstadoImportacion.PREVIEW,
                estructura.diferencias(), Resumen.de(filas), repetidos(filas, perfil.campoClave()), filas);
    }

    /** Valores del campo clave que aparecen en más de una fila válida. */
    private static List<CodigoRepetido> repetidos(List<FilaResultado> filas, String campoClave) {
        Map<String, List<Integer>> porCodigo = new LinkedHashMap<>();
        for (FilaResultado f : filas) {
            if (f.estado() == EstadoFila.VALIDA) {
                String codigo = String.valueOf(f.datosResultado().get(campoClave));
                porCodigo.computeIfAbsent(codigo, c -> new ArrayList<>()).add(f.numeroFila());
            }
        }
        return porCodigo.entrySet().stream()
                .filter(e -> e.getValue().size() > 1)
                .map(e -> new CodigoRepetido(e.getKey(), e.getValue()))
                .toList();
    }
}
