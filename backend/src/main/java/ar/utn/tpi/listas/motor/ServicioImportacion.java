package ar.utn.tpi.listas.motor;

import java.io.InputStream;
import java.util.List;

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
        List<Diferencia> diferencias = ControlEstructura.verificar(perfil, libro);
        if (!diferencias.isEmpty()) {
            return new ResultadoImportacion(perfil.id(), nombreArchivo, EstadoImportacion.RECHAZADA, diferencias,
                    new Resumen(0, 0, 0, 0, 0), List.of());
        }
        List<FilaResultado> filas = MotorTransformacion.procesar(perfil, repositorio.formatoDe(perfil), libro.hoja().orElseThrow());
        return new ResultadoImportacion(perfil.id(), nombreArchivo, EstadoImportacion.PREVIEW, List.of(),
                Resumen.de(filas), filas);
    }
}
