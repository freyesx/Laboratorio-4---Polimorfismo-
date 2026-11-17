package acopiogt;
/** Capacidad compartida por categorías sin parentesco directo. */
public interface RequiereFrio extends Almacenable {
    @Override default Almacenamiento getAlmacenamiento() { return Almacenamiento.FRIO; }
}
