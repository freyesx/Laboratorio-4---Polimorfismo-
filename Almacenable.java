package acopiogt;
/** Contrato de compatibilidad; por defecto un producto requiere bodega seca. */
public interface Almacenable {
    default Almacenamiento getAlmacenamiento() { return Almacenamiento.SECO; }
}
