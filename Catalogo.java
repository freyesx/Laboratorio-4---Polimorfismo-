package acopiogt;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
/** Punto de composición: metadatos y fábricas; no selecciona reglas mediante tipos. */
public final class Catalogo {
    public record Categoria(String codigo, String nombre, List<String> campos, Function<DatosLote, Lote> fabrica) {
        public Categoria {
            codigo = Validar.texto(codigo, "Categoría"); nombre = Validar.texto(nombre, "Nombre");
            campos = List.copyOf(campos); java.util.Objects.requireNonNull(fabrica);
        }
        public Lote crear(DatosLote datos) { return fabrica.apply(datos); }
    }
    private final Map<String, Categoria> categorias = new LinkedHashMap<>();
    public Catalogo() {
        registrar(new Categoria("CAFE", "Café en pergamino", List.of("humedad"), CafePergamino::new));
        registrar(new Categoria("ORGANICO", "Café orgánico", List.of("humedad", "certificadora", "sello"), CafeOrganico::new));
        registrar(new Categoria("CARDAMOMO", "Cardamomo", List.of("verde"), Cardamomo::new));
        registrar(new Categoria("HORTALIZA", "Hortaliza", List.of("vida"), Hortaliza::new));
        registrar(new Categoria("QUESO", "Queso fresco", List.of("grasa"), QuesoFresco::new));
    }
    public void registrar(Categoria categoria) {
        if (categorias.containsKey(categoria.codigo())) throw new IllegalArgumentException("Categoría repetida.");
        categorias.put(categoria.codigo(), categoria);
    }
    public Categoria buscar(String codigo) {
        Categoria c = categorias.get(Validar.texto(codigo, "Categoría"));
        if (c == null) throw new IllegalArgumentException("Categoría inexistente.");
        return c;
    }
    public List<Categoria> listar() { return List.copyOf(categorias.values()); }
}
