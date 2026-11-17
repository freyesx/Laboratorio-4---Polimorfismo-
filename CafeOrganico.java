package acopiogt;
import java.math.BigDecimal;
import java.time.LocalDate;
public final class CafeOrganico extends CafePergamino {
    private final String certificadora;
    private final LocalDate selloValidoHasta;
    public CafeOrganico(DatosLote d) {
        super(d); certificadora = d.texto("certificadora"); selloValidoHasta = d.fecha("sello");
        if (selloValidoHasta.isBefore(getFechaIngreso()))
            throw new IllegalArgumentException("El sello está vencido en la fecha de ingreso.");
    }
    public String getCertificadora() { return certificadora; }
    public LocalDate getSelloValidoHasta() { return selloValidoHasta; }
    @Override public String getCategoria() { return "ORGANICO"; }
    @Override public BigDecimal calcularPago() {
        return super.calcularPago().add(getPesoRecibido().multiply(new BigDecimal("0.75")));
    }
}
