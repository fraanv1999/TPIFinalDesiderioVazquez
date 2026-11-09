package ar.utn.tpi.listas.perfil;

import java.math.BigDecimal;
import java.sql.Array;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/**
 * Perfiles de importación y formatos destino guardados en PostgreSQL (database/schema.sql).
 * Un perfil se valida cada vez que se lee: si está mal configurado no llega al motor.
 */
@Repository
public class RepositorioPerfiles {

    private final JdbcClient jdbc;

    public RepositorioPerfiles(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    /** Perfiles activos, ordenados por proveedor y nombre. */
    public List<Perfil> perfiles() {
        return jdbc.sql("""
                        SELECT p.id FROM perfil_importacion p JOIN proveedor pr ON pr.id = p.proveedor_id
                        WHERE p.activo ORDER BY pr.nombre, p.nombre, p.id""")
                .query(Long.class)
                .list()
                .stream()
                .map(this::perfil)
                .toList();
    }

    public Perfil perfil(long id) {
        Perfil sinDetalle = jdbc.sql("""
                        SELECT p.id, pr.nombre AS proveedor, p.nombre, f.nombre AS formato, p.hoja,
                               p.fila_encabezado, p.margen_encabezado, p.fila_inicio_datos,
                               ck.clave AS campo_clave, cc.clave AS campo_categoria
                        FROM perfil_importacion p
                        JOIN proveedor pr ON pr.id = p.proveedor_id
                        JOIN formato_destino f ON f.id = p.formato_destino_id
                        JOIN campo_destino ck ON ck.id = p.campo_clave_id
                        LEFT JOIN campo_destino cc ON cc.id = p.campo_categoria_id
                        WHERE p.id = ?""")
                .param(id)
                .query((rs, n) -> new Perfil(rs.getLong("id"), rs.getString("proveedor"), rs.getString("nombre"),
                        rs.getString("formato"), rs.getString("hoja"), rs.getInt("fila_encabezado"),
                        rs.getInt("margen_encabezado"), rs.getInt("fila_inicio_datos"), rs.getString("campo_clave"),
                        rs.getString("campo_categoria"), List.of(), List.of()))
                .optional()
                .orElseThrow(() -> new PerfilNoEncontradoException(id));

        Perfil perfil = new Perfil(sinDetalle.id(), sinDetalle.proveedor(), sinDetalle.nombre(), sinDetalle.formato(),
                sinDetalle.hoja(), sinDetalle.filaEncabezado(), sinDetalle.margenEncabezado(),
                sinDetalle.filaInicioDatos(), sinDetalle.campoClave(), sinDetalle.campoCategoria(),
                columnas(id), reglas(id));
        validar(perfil, formatoDe(perfil));
        return perfil;
    }

    public FormatoDestino formatoDe(Perfil perfil) {
        List<CampoDestino> campos = jdbc.sql("""
                        SELECT c.clave, c.etiqueta, c.tipo_dato::text AS tipo, c.obligatorio, c.exportado
                        FROM campo_destino c
                        JOIN formato_destino f ON f.id = c.formato_destino_id
                        JOIN perfil_importacion p ON p.formato_destino_id = f.id
                        WHERE p.id = ?
                        ORDER BY c.orden""")
                .param(perfil.id())
                .query((rs, n) -> new CampoDestino(rs.getString("clave"), rs.getString("etiqueta"),
                        TipoDato.valueOf(rs.getString("tipo")), rs.getBoolean("obligatorio"), rs.getBoolean("exportado")))
                .list();
        return new FormatoDestino(perfil.formato(), campos);
    }

    private List<MapeoColumna> columnas(long perfilId) {
        return jdbc.sql("""
                        SELECT m.columna_excel, m.encabezado_esperado, c.clave, m.transformaciones::text[] AS t
                        FROM mapeo_columna m LEFT JOIN campo_destino c ON c.id = m.campo_destino_id
                        WHERE m.perfil_id = ?
                        ORDER BY length(m.columna_excel), m.columna_excel""")
                .param(perfilId)
                .query((rs, n) -> new MapeoColumna(rs.getString("columna_excel"), rs.getString("encabezado_esperado"),
                        rs.getString("clave"), transformaciones(rs.getArray("t"))))
                .list();
    }

    private List<ReglaCalculo> reglas(long perfilId) {
        return jdbc.sql("""
                        SELECT r.operacion::text AS operacion, cr.clave AS resultado, cb.clave AS base,
                               cs.clave AS secundario, r.valor
                        FROM regla_calculo r
                        JOIN campo_destino cr ON cr.id = r.campo_resultado_id
                        LEFT JOIN campo_destino cb ON cb.id = r.campo_base_id
                        LEFT JOIN campo_destino cs ON cs.id = r.campo_secundario_id
                        WHERE r.perfil_id = ?
                        ORDER BY r.orden""")
                .param(perfilId)
                .query((rs, n) -> new ReglaCalculo(rs.getString("resultado"), Operacion.valueOf(rs.getString("operacion")),
                        rs.getString("base"), rs.getString("secundario"), sinCeros(rs.getBigDecimal("valor"))))
                .list();
    }

    private static List<Transformacion> transformaciones(Array array) throws SQLException {
        return Arrays.stream((String[]) array.getArray()).map(Transformacion::valueOf).toList();
    }

    /** NUMERIC(12,6) devuelve 0.040000: se guarda como 0.04. */
    private static BigDecimal sinCeros(BigDecimal valor) {
        return valor == null ? null : valor.stripTrailingZeros();
    }

    private static void validar(Perfil p, FormatoDestino formato) {
        if (p.filaInicioDatos() <= p.filaEncabezado()) {
            throw new PerfilInvalidoException(p.nombre(), "la fila de inicio de datos debe ser posterior al encabezado");
        }
        for (MapeoColumna m : p.columnas()) {
            if (!m.ignorada() && formato.campo(m.campo()).isEmpty()) {
                throw new PerfilInvalidoException(p.nombre(), "la columna " + m.columna() + " apunta a un campo inexistente: " + m.campo());
            }
        }
        if (p.margenEncabezado() < 0 || p.margenEncabezado() > 5) {
            throw new PerfilInvalidoException(p.nombre(), "el margen de encabezado debe estar entre 0 y 5");
        }
        if (p.columnaDe(p.campoClave()).isEmpty()) {
            throw new PerfilInvalidoException(p.nombre(), "el campo clave '" + p.campoClave() + "' no está mapeado");
        }
        if (p.campoCategoria() != null
                && formato.campo(p.campoCategoria()).filter(c -> c.tipo() == TipoDato.TEXTO).isEmpty()) {
            throw new PerfilInvalidoException(p.nombre(), "el campo de categoría '" + p.campoCategoria() + "' no es un texto del formato");
        }
        List<String> calculados = new ArrayList<>();
        for (ReglaCalculo r : p.reglas()) {
            for (String campo : new String[] {r.resultado(), r.base(), r.secundario()}) {
                if (campo != null && formato.campo(campo).isEmpty()) {
                    throw new PerfilInvalidoException(p.nombre(), "regla con campo inexistente: " + campo);
                }
            }
            Operacion op = r.operacion();
            if (op.usaBase() && r.base() == null || op.usaSecundario() && r.secundario() == null
                    || op.usaValor() && r.valor() == null) {
                throw new PerfilInvalidoException(p.nombre(), "regla " + op + " incompleta para " + r.resultado());
            }
            calculados.add(r.resultado());
        }
        for (CampoDestino c : formato.campos()) {
            if (c.obligatorio() && p.columnaDe(c.clave()).isEmpty() && !calculados.contains(c.clave())) {
                throw new PerfilInvalidoException(p.nombre(), "el campo obligatorio '" + c.clave() + "' no se mapea ni se calcula");
            }
        }
    }
}
