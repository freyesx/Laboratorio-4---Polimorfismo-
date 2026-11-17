package acopiogt;
import java.math.BigDecimal;
public final class BodegaSeca extends Bodega {
    public BodegaSeca(String codigo, BigDecimal capacidad) { super(codigo, capacidad); }
    @Override public Almacenamiento getAlmacenamiento() { return Almacenamiento.SECO; }
}
