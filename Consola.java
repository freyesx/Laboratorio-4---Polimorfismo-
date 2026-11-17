package acopiogt;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.function.BiFunction;
/** Vista/controlador: solo captura datos y presenta resultados del dominio. */
public final class Consola {
    private final Cooperativa cooperativa;
    private final Scanner entrada;
    private final Map<String, BiFunction<String, BigDecimal, Bodega>> tiposBodega = new LinkedHashMap<>();
    public Consola(Cooperativa cooperativa, Scanner entrada) {
        this.cooperativa = cooperativa; this.entrada = entrada;
        tiposBodega.put("SECA", BodegaSeca::new); tiposBodega.put("FRIA", CamaraFria::new);
    }
    public void ejecutar() {
        boolean seguir = true;
        System.out.println("AcopioGT | Inventario inicial: 10 lotes, 3 bodegas, contabilidad en cero.");
        while (seguir) {
            System.out.println("\nFecha de operación: " + cooperativa.getFechaOperacion());
            System.out.println("1 Pre-liquidar | 2 Recibir lote | 3 Registrar bodega | 4 Pedido y despacho");
            System.out.println("5 Avanzar fecha | 6 Existencias por categoría | 7 Bodegas | 8 Próximos a vencer");
            System.out.println("9 Resumen financiero | 10 Historial de lotes | 0 Salir");
            try {
                switch (leer("Opción")) {
                    case "1" -> cotizar(false);
                    case "2" -> cotizar(true);
                    case "3" -> registrarBodega();
                    case "4" -> pedido();
                    case "5" -> {
                        cooperativa.avanzarFecha(LocalDate.parse(leer("Nueva fecha (AAAA-MM-DD)")));
                        System.out.println("Fecha avanzada; vencidos descartados y pérdidas registradas.");
                    }
                    case "6" -> existencias();
                    case "7" -> bodegas();
                    case "8" -> {
                        System.out.println("Vencen desde hoy hasta +3 días, ambos incluidos:");
                        var lotes = cooperativa.proximosVencer();
                        if (lotes.isEmpty()) System.out.println("Sin lotes próximos a vencer.");
                        lotes.forEach(this::mostrarLote);
                    }
                    case "9" -> finanzas();
                    case "10" -> cooperativa.getHistorial().forEach(this::mostrarLote);
                    case "0" -> seguir = false;
                    default -> System.out.println("Opción inválida.");
                }
            } catch (java.util.NoSuchElementException ex) {
                System.out.println("\nFin de entrada. Sesión cerrada."); seguir = false;
            } catch (IllegalArgumentException | java.time.DateTimeException | NullPointerException ex) {
                System.out.println("Operación rechazada: " + ex.getMessage());
            }
        }
    }
    private String leer(String mensaje) {
        System.out.print(mensaje + ": ");
        return entrada.nextLine().trim();
    }
    private BigDecimal numero(String mensaje) {
        return new BigDecimal(leer(mensaje + " (decimal con punto)"));
    }
    private String categoria() {
        cooperativa.getCatalogo().listar().forEach(c -> System.out.println(c.codigo() + " = " + c.nombre()));
        String codigo = leer("Categoría").toUpperCase(java.util.Locale.ROOT);
        cooperativa.getCatalogo().buscar(codigo); return codigo;
    }
    private void cotizar(boolean recibir) {
        String cat = categoria();
        String codigo = leer("Código de lote"), productor = leer("Productor");
        BigDecimal peso = numero("Peso recibido kg");
        Calidad calidad = Calidad.valueOf(leer("Calidad A/B/C").toUpperCase(java.util.Locale.ROOT));
        Map<String, String> extras = new LinkedHashMap<>();
        System.out.println("Rangos: humedad 8..14; verde 0..100; vida 3..14 entero; grasa 10..40; sello AAAA-MM-DD.");
        for (String campo : cooperativa.getCatalogo().buscar(cat).campos()) extras.put(campo, leer(campo));
        DatosLote datos = new DatosLote(codigo, productor, cooperativa.getFechaOperacion(), peso, calidad, extras);
        Lote candidato = cooperativa.preliquidar(cat, datos);
        System.out.println("Pago: " + dinero(candidato.calcularPago()) + " | Vence: "
            + candidato.getVencimiento().map(Object::toString).orElse("No vence"));
        if (recibir || leer("¿Recibir este lote? (S/N)").equalsIgnoreCase("S")) {
            String bodega = leer("Código de bodega");
            cooperativa.recibir(cat, datos, bodega);
            System.out.println("Lote recibido y pago registrado: " + dinero(candidato.calcularPago()));
        } else System.out.println("Pre-liquidación finalizada sin registros ni pagos.");
    }
    private void registrarBodega() {
        String codigo = leer("Código de bodega"); BigDecimal capacidad = numero("Capacidad kg");
        System.out.println("Tipos: " + tiposBodega.keySet());
        var fabrica = tiposBodega.get(leer("Tipo").toUpperCase(java.util.Locale.ROOT));
        if (fabrica == null) throw new IllegalArgumentException("Tipo de bodega inexistente.");
        cooperativa.registrarBodega(fabrica.apply(codigo, capacidad));
        System.out.println("Bodega registrada.");
    }
    private void pedido() {
        String comprador = leer("Comprador"), cat = categoria(); BigDecimal kg = numero("Kilogramos del pedido");
        Despacho d = cooperativa.despachar(comprador, cat, kg);
        System.out.println("Pedido despachado a " + d.comprador());
        d.lineas().forEach(l -> System.out.println(l.codigoLote() + " | " + l.codigoBodega()
            + " | " + l.kg().toPlainString() + " kg | " + dinero(l.cobro())));
        System.out.println("Total cobrado: " + dinero(d.total()));
    }
    private void existencias() {
        for (Catalogo.Categoria c : cooperativa.getCatalogo().listar()) {
            var lotes = cooperativa.porCategoria(c.codigo());
            BigDecimal kg = lotes.stream().map(Lote::getDisponible).reduce(BigDecimal.ZERO, BigDecimal::add);
            System.out.println("\n" + c.nombre() + " | Total " + kg.toPlainString() + " kg");
            if (lotes.isEmpty()) System.out.println("Sin existencias.");
            lotes.forEach(this::mostrarLote);
        }
    }
    private void bodegas() {
        for (Bodega b : cooperativa.getBodegas()) {
            System.out.println("\n" + b.getCodigo() + " | " + b.getAlmacenamiento().codigo()
                + " | Cámara fría: " + b.esCamaraFria()
                + " | Ocupación " + b.getOcupacion().toPlainString() + "/" + b.getCapacidad().toPlainString() + " kg");
            for (Lote l : b) mostrarLote(l);
        }
    }
    private void finanzas() {
        BigDecimal pagos = BigDecimal.ZERO, cobros = BigDecimal.ZERO, perdidas = BigDecimal.ZERO;
        for (Catalogo.Categoria c : cooperativa.getCatalogo().listar()) {
            BigDecimal p = cooperativa.total(c.codigo(), Movimiento.Tipo.PAGO);
            BigDecimal co = cooperativa.total(c.codigo(), Movimiento.Tipo.COBRO);
            BigDecimal pe = cooperativa.total(c.codigo(), Movimiento.Tipo.PERDIDA);
            System.out.println(c.nombre() + " | Pagado " + dinero(p) + " | Cobrado " + dinero(co) + " | Pérdida " + dinero(pe));
            pagos = pagos.add(p); cobros = cobros.add(co); perdidas = perdidas.add(pe);
        }
        System.out.println("TOTAL | Pagado " + dinero(pagos) + " | Cobrado " + dinero(cobros) + " | Pérdida " + dinero(perdidas));
    }
    private void mostrarLote(Lote l) {
        System.out.println(l.getCodigo() + " | " + l.getCategoria() + " | Productor " + l.getProductor()
            + " | Calidad " + l.getCalidad() + " | Ingreso " + l.getFechaIngreso()
            + " | Recibido " + l.getPesoRecibido().toPlainString() + " kg | Disponible " + l.getDisponible().toPlainString()
            + " kg | " + l.getEstado() + " | Bodega " + l.getCodigoBodega().orElse("Sin asignar")
            + " | Vence " + l.getVencimiento().map(Object::toString).orElse("No vence")
            + " | Costo/kg " + dinero(l.getCostoKg()));
    }
    public static String dinero(BigDecimal valor) {
        return "Q" + valor.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }
}
