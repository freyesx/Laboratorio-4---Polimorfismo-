package acopiogt;
import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.util.Optional;
/** Identidad y claves de orden inmutables. Solo el dominio cambia peso/estado. */
public abstract class Lote implements Comparable<Lote>, Almacenable, CalendarioLote {
    private final String codigo;
    private final String productor;
    private final LocalDate fechaIngreso;
    private final BigDecimal pesoRecibido;
    private final Calidad calidad;
    private BigDecimal disponible;
    private EstadoLote estado = EstadoLote.DISPONIBLE;
    private String codigoBodega;
    protected Lote(DatosLote d) {
        codigo = d.codigo(); productor = d.productor(); fechaIngreso = d.fechaIngreso();
        pesoRecibido = d.peso(); calidad = d.calidad(); disponible = pesoRecibido;
    }
    public abstract String getCategoria();
    public abstract BigDecimal calcularPago();
    public final String getCodigo() { return codigo; }
    public final String getProductor() { return productor; }
    @Override public final LocalDate getFechaIngreso() { return fechaIngreso; }
    public final BigDecimal getPesoRecibido() { return pesoRecibido; }
    public final Calidad getCalidad() { return calidad; }
    public final BigDecimal getDisponible() { return disponible; }
    public final EstadoLote getEstado() { return estado; }
    public final Optional<String> getCodigoBodega() { return Optional.ofNullable(codigoBodega); }
    public final BigDecimal getCostoKg() {
        return calcularPago().divide(pesoRecibido, MathContext.DECIMAL128);
    }
    final void asignarBodega(String codigo) {
        if (codigoBodega != null) throw new IllegalArgumentException("Un lote nunca cambia de bodega.");
        codigoBodega = Validar.texto(codigo, "Bodega");
    }
    final void descontar(BigDecimal kg) {
        Validar.positivo(kg, "Kilogramos");
        if (estado != EstadoLote.DISPONIBLE || kg.compareTo(disponible) > 0)
            throw new IllegalArgumentException("Peso no disponible.");
        disponible = disponible.subtract(kg);
        if (disponible.signum() == 0) estado = EstadoLote.AGOTADO;
    }
    final void descartar(LocalDate fecha) {
        if (estado != EstadoLote.DISPONIBLE || !estaVencido(fecha))
            throw new IllegalArgumentException("Solo se descarta un lote disponible y vencido.");
        disponible = BigDecimal.ZERO; estado = EstadoLote.DESCARTADO;
    }
    /** FEFO; luego FIFO; calidad y código. No depende de subtipos concretos. */
    @Override public final int compareTo(Lote otro) {
        Optional<LocalDate> a = getVencimiento(), b = otro.getVencimiento();
        int c = Boolean.compare(b.isPresent(), a.isPresent());
        if (c == 0) c = a.orElse(fechaIngreso).compareTo(b.orElse(otro.fechaIngreso));
        if (c == 0) c = calidad.compareTo(otro.calidad);
        return c != 0 ? c : codigo.compareTo(otro.codigo);
    }
}
