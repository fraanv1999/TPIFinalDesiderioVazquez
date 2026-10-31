package ar.utn.tpi.listas.motor;

import java.util.List;

public record Resumen(int validas, int ignoradas, int vacias, int titulos, int errores) {

    static Resumen de(List<FilaResultado> filas) {
        int validas = 0, vacias = 0, titulos = 0, errores = 0;
        for (FilaResultado f : filas) {
            switch (f.estado()) {
                case VALIDA -> validas++;
                case ERROR -> errores++;
                case IGNORADA -> {
                    if (f.motivoIgnorada() == MotivoIgnorada.VACIA) vacias++;
                    else titulos++;
                }
            }
        }
        return new Resumen(validas, vacias + titulos, vacias, titulos, errores);
    }
}
