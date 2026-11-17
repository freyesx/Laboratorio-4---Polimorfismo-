package acopiogt;
import java.math.BigDecimal;
import java.time.LocalDate;
/** Entrada contable inmutable; los datos iniciales no crean movimientos. */
public record Movimiento(Tipo tipo, String categoria, String codigoLote, LocalDate fecha, BigDecimal monto) {
    public enum Tipo { PAGO, COBRO, PERDIDA }
}
