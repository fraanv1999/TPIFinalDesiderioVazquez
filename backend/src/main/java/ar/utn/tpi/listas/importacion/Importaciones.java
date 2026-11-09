package ar.utn.tpi.listas.importacion;

import java.io.InputStream;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ar.utn.tpi.listas.motor.EstadoImportacion;
import ar.utn.tpi.listas.motor.FilaResultado;
import ar.utn.tpi.listas.motor.Resumen;
import ar.utn.tpi.listas.motor.ResultadoImportacion;
import ar.utn.tpi.listas.motor.ServicioImportacion;
import ar.utn.tpi.listas.perfil.CampoDestino;
import ar.utn.tpi.listas.perfil.RepositorioPerfiles;

/**
 * Ciclo de vida de una importación: vista previa (se guarda como PREVIEW o RECHAZADA),
 * confirmación o cancelación, e historial.
 */
@Service
public class Importaciones {

    private final ServicioImportacion motor;
    private final RepositorioImportaciones repositorio;
    private final RepositorioPerfiles perfiles;

    public Importaciones(ServicioImportacion motor, RepositorioImportaciones repositorio, RepositorioPerfiles perfiles) {
        this.motor = motor;
        this.repositorio = repositorio;
        this.perfiles = perfiles;
    }

    @Transactional
    public ResultadoImportacion preview(long perfilId, String nombreArchivo, InputStream archivo, long usuarioId) {
        ResultadoImportacion resultado = motor.preview(perfilId, nombreArchivo, archivo);
        return resultado.conId(repositorio.guardar(resultado, usuarioId));
    }

    public List<ImportacionResumen> historial() {
        return repositorio.listar();
    }

    public ImportacionResumen resumen(long id) {
        return repositorio.resumen(id).orElseThrow(() -> new ImportacionNoEncontradaException(id));
    }

    /** La importación completa, tal como se mostró en la vista previa. */
    public ResultadoImportacion detalle(long id) {
        ImportacionResumen cabecera = resumen(id);
        List<String> campos = perfiles.formatoDe(perfiles.perfil(cabecera.perfilId())).campos().stream()
                .map(CampoDestino::clave).toList();
        List<FilaResultado> filas = repositorio.filas(id, campos);
        return new ResultadoImportacion(id, cabecera.perfilId(), cabecera.archivo(), cabecera.estado(),
                repositorio.diferencias(id), Resumen.de(filas), ServicioImportacion.repetidos(filas, campoClave(cabecera)),
                filas);
    }

    public ImportacionResumen confirmar(long id) {
        ImportacionResumen r = resumen(id);
        if (r.estado() == EstadoImportacion.PREVIEW && r.validas() == 0) {
            throw new EstadoInvalidoException("La importación " + id + " no tiene filas válidas para confirmar");
        }
        return cambiar(r, EstadoImportacion.CONFIRMADA);
    }

    public ImportacionResumen cancelar(long id) {
        return cambiar(resumen(id), EstadoImportacion.CANCELADA);
    }

    private ImportacionResumen cambiar(ImportacionResumen r, EstadoImportacion hacia) {
        if (!repositorio.cambiarEstado(r.id(), EstadoImportacion.PREVIEW, hacia)) {
            throw new EstadoInvalidoException("La importación " + r.id() + " está " + r.estado()
                    + ": solo se puede confirmar o cancelar una vista previa");
        }
        return resumen(r.id());
    }

    private String campoClave(ImportacionResumen r) {
        return perfiles.perfil(r.perfilId()).campoClave();
    }
}
