package acopiogt;
import java.math.BigDecimal;
public final class CamaraFria extends Bodega {
    public CamaraFria(String codigo, BigDecimal capacidad) { super(codigo, capacidad); }
    @Override public Almacenamiento getAlmacenamiento() { return Almacenamiento.FRIO; }
}
