package ar.utn.tpi.listas.perfil;

/** Conjunto cerrado de cálculos. Los porcentajes son fracciones: 0.04 = 4 %. */
public enum Operacion {
    COPIAR, VALOR_FIJO, SUMAR_PORCENTAJE, RESTAR_PORCENTAJE, MULTIPLICAR, DIVIDIR, SUMAR_CAMPO, CONCATENAR;

    public boolean usaBase() {
        return this != VALOR_FIJO;
    }

    public boolean usaSecundario() {
        return this == SUMAR_CAMPO || this == CONCATENAR;
    }

    public boolean usaValor() {
        return !usaSecundario() && this != COPIAR;
    }
}
