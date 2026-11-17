package acopiogt;
import java.math.BigDecimal;
public final class QuesoFresco extends Lote implements Perecedero, RequiereFrio {
    private final BigDecimal grasa;
    public QuesoFresco(DatosLote d) {
        super(d); grasa = Validar.rango(d.decimal("grasa"), "Grasa", "10", "40");
        if (getCalidad() == Calidad.C) throw new IllegalArgumentException("No se acepta queso de calidad C.");
    }
    public BigDecimal getGrasa() { return grasa; }
    @Override public String getCategoria() { return "QUESO"; }
    @Override public int getVidaUtil() { return 21; }
    @Override public BigDecimal calcularPago() {
        BigDecimal tarifa = getCalidad() == Calidad.A ? new BigDecimal("38") : new BigDecimal("30");
        return getPesoRecibido().multiply(tarifa.subtract(new BigDecimal("1.50")));
    }
}
