package ar.utn.tpi.listas.perfil;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import tools.jackson.databind.json.JsonMapper;

/**
 * Perfiles y formatos destino leídos de {@code resources/perfiles} y {@code resources/formatos}.
 * Reflejan database/seed.sql; se reemplaza por la base de datos en el bloque de persistencia.
 */
@Component
public class RepositorioPerfiles {

    private final Map<String, FormatoDestino> formatos = new LinkedHashMap<>();
    private final Map<String, Perfil> perfiles = new LinkedHashMap<>();

    public RepositorioPerfiles() {
        JsonMapper json = JsonMapper.builder().build();
        for (FormatoDestino f : leer(json, "classpath:formatos/*.json", FormatoDestino.class)) {
            formatos.put(f.nombre(), f);
        }
        for (Perfil p : leer(json, "classpath:perfiles/*.json", Perfil.class)) {
            validar(p);
            perfiles.put(p.id(), p);
        }
    }

    public List<Perfil> perfiles() {
        return List.copyOf(perfiles.values());
    }

    public Perfil perfil(String id) {
        Perfil p = perfiles.get(id);
        if (p == null) {
            throw new PerfilNoEncontradoException(id);
        }
        return p;
    }

    public FormatoDestino formatoDe(Perfil perfil) {
        return formatos.get(perfil.formato());
    }

    private void validar(Perfil p) {
        FormatoDestino formato = formatos.get(p.formato());
        if (formato == null) {
            throw new PerfilInvalidoException(p.id(), "formato destino inexistente: " + p.formato());
        }
        if (p.filaInicioDatos() <= p.filaEncabezado()) {
            throw new PerfilInvalidoException(p.id(), "la fila de inicio de datos debe ser posterior al encabezado");
        }
        for (MapeoColumna m : p.columnas()) {
            if (!m.ignorada() && formato.campo(m.campo()).isEmpty()) {
                throw new PerfilInvalidoException(p.id(), "la columna " + m.columna() + " apunta a un campo inexistente: " + m.campo());
            }
        }
        if (p.columnaDe(p.campoClave()).isEmpty()) {
            throw new PerfilInvalidoException(p.id(), "el campo clave '" + p.campoClave() + "' no está mapeado");
        }
        List<String> calculados = new ArrayList<>();
        for (ReglaCalculo r : p.reglas()) {
            for (String campo : new String[] {r.resultado(), r.base(), r.secundario()}) {
                if (campo != null && formato.campo(campo).isEmpty()) {
                    throw new PerfilInvalidoException(p.id(), "regla con campo inexistente: " + campo);
                }
            }
            Operacion op = r.operacion();
            if (op.usaBase() && r.base() == null || op.usaSecundario() && r.secundario() == null
                    || op.usaValor() && r.valor() == null) {
                throw new PerfilInvalidoException(p.id(), "regla " + op + " incompleta para " + r.resultado());
            }
            calculados.add(r.resultado());
        }
        for (CampoDestino c : formato.campos()) {
            if (c.obligatorio() && p.columnaDe(c.clave()).isEmpty() && !calculados.contains(c.clave())) {
                throw new PerfilInvalidoException(p.id(), "el campo obligatorio '" + c.clave() + "' no se mapea ni se calcula");
            }
        }
    }

    private static <T> List<T> leer(JsonMapper json, String patron, Class<T> tipo) {
        try {
            Resource[] recursos = new PathMatchingResourcePatternResolver().getResources(patron);
            List<T> resultado = new ArrayList<>();
            for (Resource r : ordenadosPorNombre(recursos)) {
                try (InputStream in = r.getInputStream()) {
                    resultado.add(json.readValue(in, tipo));
                }
            }
            return resultado;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static List<Resource> ordenadosPorNombre(Resource[] recursos) {
        List<Resource> lista = new ArrayList<>(List.of(recursos));
        lista.sort(Comparator.comparing(Resource::getFilename, Comparator.nullsFirst(Comparator.naturalOrder())));
        return lista;
    }
}
