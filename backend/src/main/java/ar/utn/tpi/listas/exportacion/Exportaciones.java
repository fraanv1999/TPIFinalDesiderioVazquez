package ar.utn.tpi.listas.exportacion;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import ar.utn.tpi.listas.importacion.EstadoInvalidoException;
import ar.utn.tpi.listas.importacion.ImportacionResumen;
import ar.utn.tpi.listas.importacion.Importaciones;
import ar.utn.tpi.listas.motor.CodigoRepetido;
import ar.utn.tpi.listas.motor.Diferencia;
import ar.utn.tpi.listas.motor.ErrorCampo;
import ar.utn.tpi.listas.motor.EstadoFila;
import ar.utn.tpi.listas.motor.EstadoImportacion;
import ar.utn.tpi.listas.motor.FilaResultado;
import ar.utn.tpi.listas.motor.ResultadoImportacion;
import ar.utn.tpi.listas.perfil.CampoDestino;
import ar.utn.tpi.listas.perfil.RepositorioPerfiles;
import ar.utn.tpi.listas.perfil.TipoDato;

/**
 * Genera el archivo para el sistema destino (solo las filas válidas de una importación confirmada y
 * solo los campos exportados del formato, en su orden; los decimales se redondean recién acá) y el
 * reporte de errores (filas rechazadas con su motivo, avisos de estructura y códigos repetidos).
 */
@Service
public class Exportaciones {

    /** Decimales de los precios en el archivo final. */
    static final int DECIMALES = 2;

    private final Importaciones importaciones;
    private final RepositorioPerfiles perfiles;

    public Exportaciones(Importaciones importaciones, RepositorioPerfiles perfiles) {
        this.importaciones = importaciones;
        this.perfiles = perfiles;
    }

    public ArchivoExportado archivoDestino(long id) {
        ImportacionResumen cabecera = importaciones.resumen(id);
        if (cabecera.estado() != EstadoImportacion.CONFIRMADA) {
            throw new EstadoInvalidoException("La importación " + id + " está " + cabecera.estado()
                    + ": solo se exporta una importación confirmada");
        }
        ResultadoImportacion r = importaciones.detalle(id);
        List<CampoDestino> columnas = perfiles.formatoDe(perfiles.perfil(r.perfil())).campos().stream()
                .filter(CampoDestino::exportado)
                .toList();

        try (Workbook libro = new XSSFWorkbook()) {
            Sheet hoja = libro.createSheet("Productos");
            Estilos estilos = new Estilos(libro);
            Row encabezado = hoja.createRow(0);
            for (int c = 0; c < columnas.size(); c++) {
                estilos.encabezado(encabezado.createCell(c), columnas.get(c).etiqueta());
            }
            int n = 1;
            for (FilaResultado f : r.filas()) {
                if (f.estado() != EstadoFila.VALIDA) continue;
                Row fila = hoja.createRow(n++);
                for (int c = 0; c < columnas.size(); c++) {
                    escribir(fila.createCell(c), f.datosResultado().get(columnas.get(c).clave()), columnas.get(c), estilos);
                }
            }
            estilos.terminar(hoja, columnas.size());
            return new ArchivoExportado(nombre(cabecera, "") + ".xlsx", bytes(libro));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Hoja "Errores": una fila por fila rechazada, con el motivo y los datos tal como vinieron.
     * Hoja "Avisos": diferencias de estructura y códigos repetidos. Sirve en cualquier estado.
     */
    public ArchivoExportado reporteErrores(long id) {
        ImportacionResumen cabecera = importaciones.resumen(id);
        ResultadoImportacion r = importaciones.detalle(id);
        List<FilaResultado> conError = r.filas().stream().filter(f -> f.estado() == EstadoFila.ERROR).toList();
        List<String> columnasExcel = conError.stream()
                .flatMap(f -> f.datosCrudos().keySet().stream())
                .collect(Collectors.toCollection(() -> new TreeSet<>(
                        Comparator.comparingInt(String::length).thenComparing(Comparator.naturalOrder()))))
                .stream().toList();

        try (Workbook libro = new XSSFWorkbook()) {
            Estilos estilos = new Estilos(libro);
            Sheet errores = libro.createSheet("Errores");
            Row encabezado = errores.createRow(0);
            estilos.encabezado(encabezado.createCell(0), "Fila");
            estilos.encabezado(encabezado.createCell(1), "Motivo");
            for (int c = 0; c < columnasExcel.size(); c++) {
                estilos.encabezado(encabezado.createCell(c + 2), columnasExcel.get(c));
            }
            int n = 1;
            for (FilaResultado f : conError) {
                Row fila = errores.createRow(n++);
                fila.createCell(0).setCellValue(f.numeroFila());
                fila.createCell(1).setCellValue(f.errores().stream().map(ErrorCampo::mensaje).collect(Collectors.joining("; ")));
                for (int c = 0; c < columnasExcel.size(); c++) {
                    Object valor = f.datosCrudos().get(columnasExcel.get(c));
                    if (valor instanceof BigDecimal num) {
                        fila.createCell(c + 2).setCellValue(num.doubleValue());
                    } else if (valor != null) {
                        fila.createCell(c + 2).setCellValue(valor.toString());
                    }
                }
            }
            estilos.terminar(errores, columnasExcel.size() + 2);
            errores.setColumnWidth(1, 60 * 256);

            Sheet avisos = libro.createSheet("Avisos");
            Row encabezadoAvisos = avisos.createRow(0);
            estilos.encabezado(encabezadoAvisos.createCell(0), "Tipo");
            estilos.encabezado(encabezadoAvisos.createCell(1), "Bloquea");
            estilos.encabezado(encabezadoAvisos.createCell(2), "Detalle");
            n = 1;
            for (Diferencia d : r.diferencias()) {
                Row fila = avisos.createRow(n++);
                fila.createCell(0).setCellValue(d.tipo().name());
                fila.createCell(1).setCellValue(d.bloqueante() ? "Sí" : "No");
                fila.createCell(2).setCellValue(d.mensaje());
            }
            for (CodigoRepetido c : r.repetidos()) {
                Row fila = avisos.createRow(n++);
                fila.createCell(0).setCellValue("CODIGO_REPETIDO");
                fila.createCell(1).setCellValue("No");
                fila.createCell(2).setCellValue(c.codigo() + " aparece en las filas "
                        + c.filas().stream().map(String::valueOf).collect(Collectors.joining(", ")));
            }
            estilos.terminar(avisos, 2);
            avisos.setColumnWidth(2, 90 * 256);
            return new ArchivoExportado(nombre(cabecera, "-errores") + ".xlsx", bytes(libro));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static void escribir(Cell celda, Object valor, CampoDestino campo, Estilos estilos) {
        if (valor == null) {
            return;
        }
        if (valor instanceof BigDecimal n && campo.tipo() == TipoDato.ENTERO) {
            celda.setCellValue(n.longValueExact());
        } else if (valor instanceof BigDecimal n) {
            celda.setCellValue(redondear(n).doubleValue());
            celda.setCellStyle(estilos.decimal);
        } else {
            celda.setCellValue(valor.toString());
        }
    }

    static BigDecimal redondear(BigDecimal n) {
        return n.setScale(DECIMALES, RoundingMode.HALF_UP);
    }

    /** "ferreteria-lista-general-12": sin tildes ni espacios, para que sirva como nombre de archivo. */
    static String nombre(ImportacionResumen r, String sufijo) {
        String base = r.proveedor() + "-" + r.perfil() + "-" + r.id() + sufijo;
        return Normalizer.normalize(base, Normalizer.Form.NFD).replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
    }

    static byte[] bytes(Workbook libro) throws IOException {
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        libro.write(salida);
        return salida.toByteArray();
    }

    /** Estilos compartidos por los archivos que genera el sistema. */
    static final class Estilos {

        final CellStyle negrita;
        final CellStyle decimal;

        Estilos(Workbook libro) {
            Font fuente = libro.createFont();
            fuente.setBold(true);
            negrita = libro.createCellStyle();
            negrita.setFont(fuente);
            decimal = libro.createCellStyle();
            decimal.setDataFormat(libro.createDataFormat().getFormat("0.00"));
        }

        void encabezado(Cell celda, String texto) {
            celda.setCellValue(texto);
            celda.setCellStyle(negrita);
        }

        void terminar(Sheet hoja, int columnas) {
            hoja.createFreezePane(0, 1);
            for (int c = 0; c < columnas; c++) {
                hoja.setColumnWidth(c, 18 * 256);
            }
        }
    }
}
