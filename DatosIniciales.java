package acopiogt;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
public final class DatosIniciales {
    private DatosIniciales() { }
    public static Cooperativa crear() {
        LocalDate f = LocalDate.of(2026, 3, 12);
        Cooperativa c = new Cooperativa(f, new Catalogo());
        c.registrarBodega(new BodegaSeca("S1", new BigDecimal("3000")));
        c.registrarBodega(new BodegaSeca("S2", new BigDecimal("3000")));
        c.registrarBodega(new CamaraFria("F1", new BigDecimal("2000")));
        cargar(c, "CAFE", "CAF-01", "Ana", f.minusDays(10), "500", Calidad.A, Map.of("humedad", "13.4"), "S1");
        cargar(c, "CAFE", "CAF-02", "Luis", f.minusDays(10), "100", Calidad.B, Map.of("humedad", "12"), "S2");
        cargar(c, "ORGANICO", "ORG-01", "Marta", f.minusDays(8), "500", Calidad.A,
            Map.of("humedad", "13.4", "certificadora", "EcoGT", "sello", "2026-12-31"), "S2");
        cargar(c, "ORGANICO", "ORG-02", "Pedro", f.minusDays(3), "50", Calidad.C,
            Map.of("humedad", "8", "certificadora", "EcoGT", "sello", "2026-03-09"), "S1");
        cargar(c, "CARDAMOMO", "CAR-01", "Rosa", f.minusDays(179), "80", Calidad.B, Map.of("verde", "90"), "S1");
        cargar(c, "CARDAMOMO", "CAR-02", "Juan", f.minusDays(179), "40", Calidad.A, Map.of("verde", "84.9"), "S2");
        cargar(c, "HORTALIZA", "HOR-01", "Eva", f.minusDays(7), "200", Calidad.A, Map.of("vida", "7"), "F1");
        cargar(c, "HORTALIZA", "HOR-02", "Raúl", f, "90", Calidad.C, Map.of("vida", "3"), "F1");
        cargar(c, "QUESO", "QUE-01", "Nora", f.minusDays(18), "40", Calidad.A, Map.of("grasa", "20"), "F1");
        cargar(c, "QUESO", "QUE-02", "Mario", f.minusDays(2), "60", Calidad.B, Map.of("grasa", "40"), "F1");
        return c;
    }
    private static void cargar(Cooperativa c, String cat, String cod, String productor, LocalDate f,
                               String kg, Calidad calidad, Map<String, String> extras, String bodega) {
        c.cargarInicial(cat, new DatosLote(cod, productor, f, new BigDecimal(kg), calidad, extras), bodega);
    }
}
