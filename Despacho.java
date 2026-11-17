package acopiogt;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
public record Despacho(String comprador, String categoria, BigDecimal kg,
                       LocalDate fecha, List<Linea> lineas, BigDecimal total) {
    public record Linea(String codigoLote, String codigoBodega, BigDecimal kg, BigDecimal cobro) { }
    public Despacho { lineas = List.copyOf(lineas); }
}
