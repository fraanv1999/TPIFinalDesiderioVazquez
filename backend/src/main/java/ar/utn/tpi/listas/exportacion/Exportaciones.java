package ar.utn.tpi.listas.exportacion;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.util.List;
import java.util.Locale;

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
import ar.utn.tpi.listas.motor.EstadoFila;
import ar.utn.tpi.listas.motor.EstadoImportacion;
import ar.utn.tpi.listas.motor.FilaResultado;
import ar.utn.tpi.listas.motor.ResultadoImportacion;
import ar.utn.tpi.listas.perfil.CampoDestino;
import ar.utn.tpi.listas.perfil.RepositorioPerfiles;
import ar.utn.tpi.listas.perfil.TipoDato;

/**
 * Genera el archivo para el sistema destino: solo las filas válidas de una importación confirmada y
 * solo los campos exportados del formato, en su orden. Los decimales se redondean recién acá.
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
