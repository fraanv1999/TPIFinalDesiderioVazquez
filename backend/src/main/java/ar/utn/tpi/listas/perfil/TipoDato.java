package ar.utn.tpi.listas.perfil;

public enum TipoDato {
    TEXTO, DECIMAL, ENTERO;

    public boolean esNumerico() {
        return this != TEXTO;
    }
}
