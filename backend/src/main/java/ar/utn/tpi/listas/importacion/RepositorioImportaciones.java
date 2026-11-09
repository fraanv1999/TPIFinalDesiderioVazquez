package ar.utn.tpi.listas.importacion;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import ar.utn.tpi.listas.motor.Diferencia;
import ar.utn.tpi.listas.motor.ErrorCampo;
import ar.utn.tpi.listas.motor.EstadoFila;
import ar.utn.tpi.listas.motor.EstadoImportacion;
import ar.utn.tpi.listas.motor.FilaResultado;
import ar.utn.tpi.listas.motor.MotivoIgnorada;
import ar.utn.tpi.listas.motor.ResultadoImportacion;
import ar.utn.tpi.listas.motor.TipoDiferencia;
import tools.jackson.core.StreamWriteFeature;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper;

/**
 * Historial de importaciones: cabecera, diferencias de estructura, filas (JSONB) y errores por fila.
 * JSONB no conserva el orden de las claves: al leer, las columnas del Excel se ordenan por letra
 * y los campos del resultado según el formato destino.
 */
@Repository
public class RepositorioImportaciones {

    private static final TypeReference<Map<String, Object>> MAPA = new TypeReference<>() {
    };

    private final JdbcClient jdbc;
    private final JdbcTemplate jdbcTemplate;
    private final JsonMapper json = JsonMapper.builder()
            .enable(StreamWriteFeature.WRITE_BIGDECIMAL_AS_PLAIN)
            .enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
            .build();

    public RepositorioImportaciones(JdbcClient jdbc, JdbcTemplate jdbcTemplate) {
        this.jdbc = jdbc;
        this.jdbcTemplate = jdbcTemplate;
    }

    /** Guarda la importación completa y devuelve su id. */
    public long guardar(ResultadoImportacion r, long usuarioId) {
        GeneratedKeyHolder clave = new GeneratedKeyHolder();
        jdbc.sql("""
                        INSERT INTO importacion (perfil_id, usuario_id, nombre_archivo, estado,
                                                 filas_validas, filas_ignoradas, filas_con_error)
                        VALUES (?, ?, ?, ?::estado_importacion, ?, ?, ?)""")
                .params(r.perfil(), usuarioId, recortar(r.archivo(), 255), r.estado().name(),
                        r.resumen().validas(), r.resumen().ignoradas(), r.resumen().errores())
                .update(clave, "id");
        long id = clave.getKeyAs(Long.class);

        jdbcTemplate.batchUpdate("""
                        INSERT INTO diferencia_estructura (importacion_id, tipo, bloqueante, columna_excel,
                                                           esperado, encontrado, mensaje)
                        VALUES (?, ?::tipo_diferencia, ?, ?, ?, ?, ?)""",
                r.diferencias().stream().map(d -> new Object[] {id, d.tipo().name(), d.bloqueante(), d.columna(),
                        recortar(d.esperado(), 150), recortar(d.encontrado(), 150), recortar(d.mensaje(), 500)}).toList());

        jdbcTemplate.batchUpdate("""
                        INSERT INTO fila_importada (importacion_id, numero_fila, estado, motivo_ignorada,
                                                    datos_crudos, datos_resultado)
                        VALUES (?, ?, ?::estado_fila, ?::motivo_ignorada, ?::jsonb, ?::jsonb)""",
                r.filas().stream().map(f -> new Object[] {id, f.numeroFila(), f.estado().name(),
                        f.motivoIgnorada() == null ? null : f.motivoIgnorada().name(),
                        json.writeValueAsString(f.datosCrudos()),
                        f.datosResultado() == null ? null : json.writeValueAsString(f.datosResultado())}).toList());

        List<Object[]> errores = new ArrayList<>();
        for (FilaResultado f : r.filas()) {
            for (ErrorCampo e : f.errores()) {
                errores.add(new Object[] {e.campo(), id, recortar(e.mensaje(), 255), id, f.numeroFila()});
            }
        }
        jdbcTemplate.batchUpdate("""
                        INSERT INTO error_fila (fila_id, campo_destino_id, mensaje)
                        SELECT f.id,
                               (SELECT c.id FROM campo_destino c
                                JOIN perfil_importacion p ON p.formato_destino_id = c.formato_destino_id
                                JOIN importacion i ON i.perfil_id = p.id
                                WHERE c.clave = ? AND i.id = ?),
                               ?
                        FROM fila_importada f WHERE f.importacion_id = ? AND f.numero_fila = ?""", errores);
        return id;
    }

    public Optional<ImportacionResumen> resumen(long id) {
        return jdbc.sql(SELECT_RESUMEN + " WHERE i.id = ?").param(id).query(this::mapearResumen).optional();
    }

    /** Las más nuevas primero. */
    public List<ImportacionResumen> listar() {
        return jdbc.sql(SELECT_RESUMEN + " ORDER BY i.creado_en DESC, i.id DESC").query(this::mapearResumen).list();
    }

    public List<Diferencia> diferencias(long id) {
        return jdbc.sql("""
                        SELECT tipo::text AS tipo, bloqueante, columna_excel, esperado, encontrado, mensaje
                        FROM diferencia_estructura WHERE importacion_id = ? ORDER BY id""")
                .param(id)
                .query((rs, n) -> new Diferencia(TipoDiferencia.valueOf(rs.getString("tipo")), rs.getBoolean("bloqueante"),
                        rs.getString("columna_excel"), rs.getString("esperado"), rs.getString("encontrado"),
                        rs.getString("mensaje")))
                .list();
    }

    /** @param ordenCampos claves del formato destino, en el orden de sus columnas. */
    public List<FilaResultado> filas(long id, List<String> ordenCampos) {
        Map<Long, List<ErrorCampo>> errores = new TreeMap<>();
        jdbc.sql("""
                        SELECT e.fila_id, c.clave, e.mensaje FROM error_fila e
                        JOIN fila_importada f ON f.id = e.fila_id
                        LEFT JOIN campo_destino c ON c.id = e.campo_destino_id
                        WHERE f.importacion_id = ? ORDER BY e.id""")
                .param(id)
                .query(rs -> {
                    errores.computeIfAbsent(rs.getLong("fila_id"), k -> new ArrayList<>())
                            .add(new ErrorCampo(rs.getString("clave"), rs.getString("mensaje")));
                });
        return jdbc.sql("""
                        SELECT id, numero_fila, estado::text AS estado, motivo_ignorada::text AS motivo,
                               datos_crudos::text AS crudos, datos_resultado::text AS resultado
                        FROM fila_importada WHERE importacion_id = ? ORDER BY numero_fila""")
                .param(id)
                .query((rs, n) -> {
                    String motivo = rs.getString("motivo");
                    String resultado = rs.getString("resultado");
                    return new FilaResultado(rs.getInt("numero_fila"), EstadoFila.valueOf(rs.getString("estado")),
                            motivo == null ? null : MotivoIgnorada.valueOf(motivo),
                            porColumna(leer(rs.getString("crudos"))),
                            resultado == null ? null : enOrden(leer(resultado), ordenCampos),
                            errores.getOrDefault(rs.getLong("id"), List.of()));
                })
                .list();
    }

    /** Cambia el estado solo si la importación está en {@code desde}. @return true si cambió. */
    public boolean cambiarEstado(long id, EstadoImportacion desde, EstadoImportacion hacia) {
        return jdbc.sql("""
                        UPDATE importacion SET estado = ?::estado_importacion
                        WHERE id = ? AND estado = ?::estado_importacion""")
                .params(hacia.name(), id, desde.name())
                .update() == 1;
    }

    private static final String SELECT_RESUMEN = """
            SELECT i.id, i.perfil_id, p.nombre AS perfil, pr.nombre AS proveedor, i.nombre_archivo,
                   i.estado::text AS estado, i.filas_validas, i.filas_ignoradas, i.filas_con_error,
                   u.nombre AS usuario, i.creado_en
            FROM importacion i
            JOIN perfil_importacion p ON p.id = i.perfil_id
            JOIN proveedor pr ON pr.id = p.proveedor_id
            JOIN usuario u ON u.id = i.usuario_id""";

    private ImportacionResumen mapearResumen(java.sql.ResultSet rs, int n) throws java.sql.SQLException {
        return new ImportacionResumen(rs.getLong("id"), rs.getLong("perfil_id"), rs.getString("perfil"),
                rs.getString("proveedor"), rs.getString("nombre_archivo"), EstadoImportacion.valueOf(rs.getString("estado")),
                rs.getInt("filas_validas"), rs.getInt("filas_ignoradas"), rs.getInt("filas_con_error"),
                rs.getString("usuario"), rs.getObject("creado_en", OffsetDateTime.class));
    }

    /** Los números vuelven como BigDecimal, igual que los produce el motor. */
    private Map<String, Object> leer(String texto) {
        Map<String, Object> mapa = json.readValue(texto, MAPA);
        mapa.replaceAll((k, v) -> v instanceof Integer || v instanceof Long || v instanceof BigInteger
                ? new BigDecimal(v.toString()) : v);
        return mapa;
    }

    /** A, B, ..., Z, AA, AB... */
    private static Map<String, Object> porColumna(Map<String, Object> celdas) {
        Map<String, Object> ordenadas = new LinkedHashMap<>();
        celdas.keySet().stream()
                .sorted(Comparator.comparingInt(String::length).thenComparing(Comparator.naturalOrder()))
                .forEach(k -> ordenadas.put(k, celdas.get(k)));
        return ordenadas;
    }

    private static Map<String, Object> enOrden(Map<String, Object> valores, List<String> ordenCampos) {
        Map<String, Object> ordenados = new LinkedHashMap<>();
        ordenCampos.forEach(c -> ordenados.put(c, valores.get(c)));
        return ordenados;
    }

    private static String recortar(String texto, int largo) {
        return texto == null || texto.length() <= largo ? texto : texto.substring(0, largo - 1) + "…";
    }
}
