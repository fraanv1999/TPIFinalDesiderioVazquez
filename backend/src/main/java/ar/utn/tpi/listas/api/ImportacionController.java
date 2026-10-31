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

import ar.utn.tpi.listas.motor.ResultadoImportacion;
import ar.utn.tpi.listas.motor.ServicioImportacion;
import ar.utn.tpi.listas.perfil.Perfil;
import ar.utn.tpi.listas.perfil.RepositorioPerfiles;

@RestController
@RequestMapping("/api")
public class ImportacionController {

    private final RepositorioPerfiles perfiles;
    private final ServicioImportacion importaciones;

    public ImportacionController(RepositorioPerfiles perfiles, ServicioImportacion importaciones) {
        this.perfiles = perfiles;
        this.importaciones = importaciones;
    }

    @GetMapping("/perfiles")
    public List<PerfilResumen> perfiles() {
        return perfiles.perfiles().stream().map(PerfilResumen::de).toList();
    }

    @GetMapping("/perfiles/{id}")
    public Perfil perfil(@PathVariable String id) {
        return perfiles.perfil(id);
    }

    @PostMapping(path = "/importaciones/preview", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResultadoImportacion preview(@RequestParam String perfil, @RequestParam MultipartFile archivo) throws IOException {
        try (InputStream in = archivo.getInputStream()) {
            return importaciones.preview(perfil, archivo.getOriginalFilename(), in);
        }
    }
}
