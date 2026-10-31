package ar.utn.tpi.listas.excel;

import java.util.List;
import java.util.Optional;

/** Nombres de todas las hojas del archivo y la hoja pedida por el perfil, si existe. */
public record LibroExcel(List<String> hojas, Optional<HojaExcel> hoja) {
}
