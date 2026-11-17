package acopiogt;
import java.math.BigDecimal;
import java.util.Objects;
final class Validar {
    private Validar() { }
    static String texto(String valor, String campo) {
        if (valor == null || valor.isBlank()) throw new IllegalArgumentException(campo + " no puede estar vacío.");
        return valor.trim();
    }
    static BigDecimal positivo(BigDecimal valor, String campo) {
        Objects.requireNonNull(valor, campo + " requerido.");
        if (valor.signum() <= 0) throw new IllegalArgumentException(campo + " debe ser positivo.");
        return valor;
    }
    static BigDecimal rango(BigDecimal valor, String campo, String minimo, String maximo) {
        Objects.requireNonNull(valor, campo + " requerido.");
        if (valor.compareTo(new BigDecimal(minimo)) < 0 || valor.compareTo(new BigDecimal(maximo)) > 0)
            throw new IllegalArgumentException(campo + " debe estar entre " + minimo + " y " + maximo + ".");
        return valor;
    }
}
