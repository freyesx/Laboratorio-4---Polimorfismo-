package acopiogt;
import java.math.BigDecimal;
import java.math.RoundingMode;
public class CafePergamino extends Lote {
    private final BigDecimal humedad;
    public CafePergamino(DatosLote d) {
        super(d); humedad = Validar.rango(d.decimal("humedad"), "Humedad", "8", "14");
    }
    public final BigDecimal getHumedad() { return humedad; }
    @Override public String getCategoria() { return "CAFE"; }
    @Override public BigDecimal calcularPago() {
        BigDecimal tarifa = switch (getCalidad()) {
            case A -> new BigDecimal("12"); case B -> new BigDecimal("9"); case C -> new BigDecimal("6");
        };
        BigDecimal puntos = humedad.subtract(new BigDecimal("12")).max(BigDecimal.ZERO)
            .setScale(0, RoundingMode.CEILING);
        BigDecimal factor = BigDecimal.ONE.subtract(puntos.multiply(new BigDecimal("0.03")));
        return getPesoRecibido().multiply(tarifa).multiply(factor);
    }
}
