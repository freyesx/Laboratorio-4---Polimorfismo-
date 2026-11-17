package acopiogt;
import java.time.LocalDate;
import java.util.Optional;
public interface CalendarioLote {
    LocalDate getFechaIngreso();
    default Optional<LocalDate> getVencimiento() { return Optional.empty(); }
    default boolean estaVencido(LocalDate fecha) {
        return getVencimiento().map(fecha::isAfter).orElse(false);
    }
}
