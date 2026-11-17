package acopiogt;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.Objects;
/** Entrada inmutable, reutilizada por todas las fábricas y constructores. */
public record DatosLote(String codigo, String productor, LocalDate fechaIngreso,
                        BigDecimal peso, Calidad calidad, Map<String, String> extras) {
    public DatosLote {
        codigo = Validar.texto(codigo, "Código");
        productor = Validar.texto(productor, "Productor");
        Objects.requireNonNull(fechaIngreso, "Fecha requerida.");
        Validar.positivo(peso, "Peso");
        Objects.requireNonNull(calidad, "Calidad requerida.");
        extras = Map.copyOf(extras);
    }
    public String texto(String clave) { return Validar.texto(extras.get(clave), clave); }
    public BigDecimal decimal(String clave) { return new BigDecimal(texto(clave)); }
    public int entero(String clave) { return Integer.parseInt(texto(clave)); }
    public LocalDate fecha(String clave) { return LocalDate.parse(texto(clave)); }
}
