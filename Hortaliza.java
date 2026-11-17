package acopiogt;
import java.math.BigDecimal;
public final class Hortaliza extends Lote implements Perecedero, RequiereFrio {
    private final int vidaUtil;
    public Hortaliza(DatosLote d) {
        super(d); vidaUtil = d.entero("vida");
        if (vidaUtil < 3 || vidaUtil > 14) throw new IllegalArgumentException("Vida útil debe estar entre 3 y 14 días.");
    }
    @Override public String getCategoria() { return "HORTALIZA"; }
    @Override public int getVidaUtil() { return vidaUtil; }
    @Override public BigDecimal calcularPago() {
        BigDecimal tarifa = switch (getCalidad()) {
            case A -> new BigDecimal("4"); case B -> new BigDecimal("3"); case C -> new BigDecimal("2");
        };
        return getPesoRecibido().multiply(new BigDecimal("0.95")).multiply(tarifa);
    }
}
