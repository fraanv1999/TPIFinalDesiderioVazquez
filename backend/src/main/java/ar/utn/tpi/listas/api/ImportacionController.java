package ar.utn.tpi.listas.api;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import ar.utn.tpi.listas.exportacion.ArchivoExportado;
import ar.utn.tpi.listas.exportacion.Exportaciones;
import ar.utn.tpi.listas.importacion.ImportacionResumen;
import ar.utn.tpi.listas.importacion.Importaciones;
import ar.utn.tpi.listas.motor.ResultadoImportacion;
import ar.utn.tpi.listas.seguridad.UsuarioAutenticado;

@RestController
@RequestMapping("/api/importaciones")
public class ImportacionController {

    private final Importaciones importaciones;
    private final Exportaciones exportaciones;

    public ImportacionController(Importaciones importaciones, Exportaciones exportaciones) {
        this.importaciones = importaciones;
        this.exportaciones = exportaciones;
    }

    /** Procesa el archivo y guarda la vista previa (o el rechazo, si la estructura no coincide). */
    @PostMapping(path = "/preview", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResultadoImportacion preview(@RequestParam long perfil, @RequestParam MultipartFile archivo,
                                        @AuthenticationPrincipal UsuarioAutenticado usuario) throws IOException {
        try (InputStream in = archivo.getInputStream()) {
            return importaciones.preview(perfil, archivo.getOriginalFilename(), in, usuario.id());
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

    /** Archivo para el sistema destino. Solo de una importación confirmada. */
    @GetMapping("/{id}/exportar")
    public ResponseEntity<byte[]> exportar(@PathVariable long id) {
        return descarga(exportaciones.archivoDestino(id));
    }

    /** Filas rechazadas con su motivo, diferencias de estructura y códigos repetidos. En cualquier estado. */
    @GetMapping("/{id}/errores")
    public ResponseEntity<byte[]> errores(@PathVariable long id) {
        return descarga(exportaciones.reporteErrores(id));
    }

    private static ResponseEntity<byte[]> descarga(ArchivoExportado archivo) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(archivo.nombre(), StandardCharsets.UTF_8).build().toString())
                .contentType(MediaType.parseMediaType(ArchivoExportado.TIPO))
                .body(archivo.contenido());
    }
}
