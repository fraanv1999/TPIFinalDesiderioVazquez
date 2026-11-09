package ar.utn.tpi.listas.api;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ar.utn.tpi.listas.perfil.Perfil;
import ar.utn.tpi.listas.perfil.RepositorioPerfiles;

@RestController
@RequestMapping("/api/perfiles")
public class PerfilController {

    private final RepositorioPerfiles perfiles;

    public PerfilController(RepositorioPerfiles perfiles) {
        this.perfiles = perfiles;
    }

    @GetMapping
    public List<PerfilResumen> perfiles() {
        return perfiles.perfiles().stream().map(PerfilResumen::de).toList();
    }

    @GetMapping("/{id}")
    public Perfil perfil(@PathVariable long id) {
        return perfiles.perfil(id);
    }
}
