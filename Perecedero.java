package acopiogt;
import java.time.LocalDate;
import java.util.Optional;
/** Comportamiento reutilizable por composición de interfaces. */
public interface Perecedero extends CalendarioLote {
    int getVidaUtil();
    @Override default Optional<LocalDate> getVencimiento() {
        return Optional.of(getFechaIngreso().plusDays(getVidaUtil()));
    }
}
