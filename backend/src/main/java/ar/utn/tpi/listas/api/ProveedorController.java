package ar.utn.tpi.listas.api;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import ar.utn.tpi.listas.proveedor.DatosProveedor;
import ar.utn.tpi.listas.proveedor.Proveedor;
import ar.utn.tpi.listas.proveedor.RepositorioProveedores;

@RestController
@RequestMapping("/api/proveedores")
public class ProveedorController {

    private final RepositorioProveedores proveedores;

    public ProveedorController(RepositorioProveedores proveedores) {
        this.proveedores = proveedores;
    }

    @GetMapping
    public List<Proveedor> listar() {
        return proveedores.listar();
    }

    @GetMapping("/{id}")
    public Proveedor buscar(@PathVariable long id) {
        return proveedores.buscar(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Proveedor crear(@RequestBody @Validated DatosProveedor datos) {
        return proveedores.crear(datos);
    }

    @PutMapping("/{id}")
    public Proveedor editar(@PathVariable long id, @RequestBody @Validated DatosProveedor datos) {
        return proveedores.editar(id, datos);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void borrar(@PathVariable long id) {
        proveedores.borrar(id);
    }
}
