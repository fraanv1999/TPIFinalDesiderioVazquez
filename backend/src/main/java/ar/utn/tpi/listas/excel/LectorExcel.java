package ar.utn.tpi.listas.excel;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaError;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.ss.util.NumberToTextConverter;

/** Lee una hoja de un .xlsx/.xls con Apache POI. Las fórmulas se leen por su último valor calculado. */
public final class LectorExcel {

    private static final DataFormatter FORMATO = new DataFormatter();

    private LectorExcel() {
    }

    public static LibroExcel leer(InputStream archivo, String nombreHoja) {
        try (Workbook libro = WorkbookFactory.create(archivo)) {
            List<String> hojas = new ArrayList<>();
            libro.forEach(h -> hojas.add(h.getSheetName()));
            Sheet hoja = libro.getSheet(nombreHoja);
            return new LibroExcel(hojas, Optional.ofNullable(hoja).map(LectorExcel::leerHoja));
        } catch (IOException | RuntimeException e) {
            throw new ArchivoInvalidoException("No se pudo leer el archivo como Excel (.xlsx o .xls)", e);
        }
    }

    private static HojaExcel leerHoja(Sheet hoja) {
        SortedMap<Integer, Map<String, Object>> filas = new TreeMap<>();
        for (Row fila : hoja) {
            Map<String, Object> celdas = new LinkedHashMap<>();
            for (Cell celda : fila) {
                Object valor = valor(celda, celda.getCellType());
                if (valor != null) {
                    celdas.put(CellReference.convertNumToColString(celda.getColumnIndex()), valor);
                }
            }
            if (!celdas.isEmpty()) {
                filas.put(fila.getRowNum() + 1, celdas);
            }
        }
        return new HojaExcel(hoja.getSheetName(), filas);
    }

    private static Object valor(Cell celda, CellType tipo) {
        return switch (tipo) {
            case STRING -> celda.getStringCellValue().isBlank() ? null : celda.getStringCellValue();
            // NumberToTextConverter da el número como lo muestra Excel: 1474.41 y no 1474.4100000000001
            case NUMERIC -> DateUtil.isCellDateFormatted(celda)
                    ? FORMATO.formatCellValue(celda)
                    : new BigDecimal(NumberToTextConverter.toText(celda.getNumericCellValue()));
            case BOOLEAN -> String.valueOf(celda.getBooleanCellValue());
            case FORMULA -> valor(celda, celda.getCachedFormulaResultType());
            // Un #DIV/0! llega como texto para que el motor lo informe y no lo trate como vacío.
            case ERROR -> FormulaError.forInt(celda.getErrorCellValue()).getString();
            default -> null;
        };
    }
}
