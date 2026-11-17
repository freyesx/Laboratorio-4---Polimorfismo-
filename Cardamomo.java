package acopiogt;
import java.math.BigDecimal;
public final class Cardamomo extends Lote implements Perecedero {
    private final BigDecimal granoVerde;
    public Cardamomo(DatosLote d) {
        super(d); granoVerde = Validar.rango(d.decimal("verde"), "Grano verde", "0", "100");
    }
    public BigDecimal getGranoVerde() { return granoVerde; }
    @Override public String getCategoria() { return "CARDAMOMO"; }
    @Override public int getVidaUtil() { return 180; }
    @Override public BigDecimal calcularPago() {
        BigDecimal tarifa = switch (getCalidad()) {
            case A -> new BigDecimal("60"); case B -> new BigDecimal("48"); case C -> new BigDecimal("35");
        };
        return getPesoRecibido().multiply(tarifa).multiply(granoVerde.compareTo(new BigDecimal("85")) >= 0
            ? new BigDecimal("1.10") : BigDecimal.ONE);
    }
}
