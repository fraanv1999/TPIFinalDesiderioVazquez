package ar.utn.tpi.listas.motor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import ar.utn.tpi.listas.perfil.CampoDestino;
import ar.utn.tpi.listas.perfil.Transformacion;

/** Aplica las transformaciones de una columna y convierte la celda al tipo del campo destino. */
final class Conversor {

    // 1.474,41 / 1474,41 / 1474.41 / 1474
    private static final Pattern MILES_CON_PUNTO = Pattern.compile("-?\\d{1,3}(\\.\\d{3})+(,\\d+)?");
    private static final Pattern DECIMAL_CON_COMA = Pattern.compile("-?\\d+,\\d+");
    private static final Pattern DECIMAL_CON_PUNTO = Pattern.compile("-?\\d+(\\.\\d+)?");

    private Conversor() {
    }

    /** Devuelve {@code String}, {@code BigDecimal} o {@code null} si la celda queda vacía. */
    static Object convertir(Object celda, List<Transformacion> transformaciones, CampoDestino campo)
            throws ValorInvalidoException {
        Object valor = celda;
        if (valor instanceof String s) {
            if (transformaciones.contains(Transformacion.TRIM)) s = s.strip();
            if (transformaciones.contains(Transformacion.MAYUSCULAS)) s = s.toUpperCase(Locale.ROOT);
            valor = s.isBlank() ? null : s;
        }
        if (valor == null) {
            return null;
        }
        return switch (campo.tipo()) {
            case TEXTO -> valor instanceof BigDecimal n ? n.toPlainString() : valor;
            case DECIMAL -> numero(valor, transformaciones.contains(Transformacion.A_DECIMAL), campo);
            case ENTERO -> entero(numero(valor, transformaciones.contains(Transformacion.A_ENTERO), campo), campo);
        };
    }

    private static BigDecimal numero(Object valor, boolean aceptaTexto, CampoDestino campo)
            throws ValorInvalidoException {
        if (valor instanceof BigDecimal n) {
            return n;
        }
        String s = ((String) valor).replace("$", "").replace(" ", "");
        if (aceptaTexto) {
            if (MILES_CON_PUNTO.matcher(s).matches()) return new BigDecimal(s.replace(".", "").replace(',', '.'));
            if (DECIMAL_CON_COMA.matcher(s).matches()) return new BigDecimal(s.replace(',', '.'));
            if (DECIMAL_CON_PUNTO.matcher(s).matches()) return new BigDecimal(s);
        }
        throw new ValorInvalidoException(campo.etiqueta() + ": '" + valor + "' no es un número");
    }

    private static BigDecimal entero(BigDecimal n, CampoDestino campo) throws ValorInvalidoException {
        if (n.stripTrailingZeros().scale() > 0) {
            throw new ValorInvalidoException(campo.etiqueta() + ": " + n.toPlainString() + " no es un número entero");
        }
        return n;
    }
}
