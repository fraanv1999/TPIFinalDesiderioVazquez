package ar.utn.tpi.listas.api;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import ar.utn.tpi.listas.importacion.ImportacionResumen;
import ar.utn.tpi.listas.importacion.Importaciones;
import ar.utn.tpi.listas.motor.ResultadoImportacion;

@RestController
@RequestMapping("/api/importaciones")
public class ImportacionController {

    /** Usuario del seed. Se reemplaza por el usuario autenticado cuando se agrega el login. */
    private static final long USUARIO_PROVISORIO = 1;

    private final Importaciones importaciones;

    public ImportacionController(Importaciones importaciones) {
        this.importaciones = importaciones;
    }

    /** Procesa el archivo y guarda la vista previa (o el rechazo, si la estructura no coincide). */
    @PostMapping(path = "/preview", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResultadoImportacion preview(@RequestParam long perfil, @RequestParam MultipartFile archivo) throws IOException {
        try (InputStream in = archivo.getInputStream()) {
            return importaciones.preview(perfil, archivo.getOriginalFilename(), in, USUARIO_PROVISORIO);
        }
    }

    @GetMapping
    public List<ImportacionResumen> historial() {
        return importaciones.historial();
    }

    @GetMapping("/{id}")
    public ResultadoImportacion detalle(@PathVariable long id) {
        return importaciones.detalle(id);
    }

    @PostMapping("/{id}/confirmar")
    public ImportacionResumen confirmar(@PathVariable long id) {
        return importaciones.confirmar(id);
    }

    @PostMapping("/{id}/cancelar")
    public ImportacionResumen cancelar(@PathVariable long id) {
        return importaciones.cancelar(id);
    }
}
