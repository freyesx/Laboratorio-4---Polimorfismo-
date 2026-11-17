package acopiogt;
import java.math.BigDecimal;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.TreeSet;
/** Encapsula la colección ordenada y controla remove durante el recorrido. */
public abstract class Bodega implements Iterable<Lote> {
    private final String codigo;
    private final BigDecimal capacidad;
    private final TreeSet<Lote> lotes = new TreeSet<>();
    protected Bodega(String codigo, BigDecimal capacidad) {
        this.codigo = Validar.texto(codigo, "Código de bodega");
        this.capacidad = Validar.positivo(capacidad, "Capacidad");
    }
    public abstract Almacenamiento getAlmacenamiento();
    public final String getCodigo() { return codigo; }
    public final BigDecimal getCapacidad() { return capacidad; }
    public final BigDecimal getOcupacion() {
        BigDecimal total = BigDecimal.ZERO;
        for (Lote lote : this) total = total.add(lote.getDisponible());
        return total;
    }
    public final boolean esCamaraFria() { return getAlmacenamiento().equals(Almacenamiento.FRIO); }
    final void validarIngreso(Lote lote) {
        if (!getAlmacenamiento().equals(lote.getAlmacenamiento()))
            throw new IllegalArgumentException("Lote incompatible con bodega " + codigo + ".");
        if (getOcupacion().add(lote.getPesoRecibido()).compareTo(capacidad) > 0)
            throw new IllegalArgumentException("Capacidad insuficiente en bodega " + codigo + ".");
        if (lote.getCodigoBodega().isPresent() || lote.getEstado() != EstadoLote.DISPONIBLE
                || lote.getDisponible().compareTo(lote.getPesoRecibido()) != 0)
            throw new IllegalArgumentException("Solo se recibe un lote nuevo.");
        if (lotes.contains(lote)) throw new IllegalArgumentException("Lote repetido en bodega.");
    }
    final void almacenar(Lote lote) {
        validarIngreso(lote); lote.asignarBodega(codigo); lotes.add(lote);
    }
    final void retirarAgotado(Lote lote) {
        if (lote.getEstado() != EstadoLote.AGOTADO) throw new IllegalArgumentException("El lote aún tiene existencias.");
        lotes.remove(lote);
    }
    @Override public final Iterator<Lote> iterator() {
        Iterator<Lote> interno = lotes.iterator();
        return new Iterator<Lote>() {
            private Lote actual;
            @Override public boolean hasNext() { return interno.hasNext(); }
            @Override public Lote next() {
                if (!hasNext()) throw new NoSuchElementException();
                actual = interno.next(); return actual;
            }
            @Override public void remove() {
                if (actual == null || actual.getEstado() == EstadoLote.DISPONIBLE)
                    throw new IllegalStateException("Solo se retiran lotes terminales durante el recorrido.");
                interno.remove(); actual = null;
            }
        };
    }
}
