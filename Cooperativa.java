package acopiogt;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
/** Servicio transaccional en memoria, de un solo hilo, independiente de categorías concretas. */
public final class Cooperativa {
    private LocalDate fechaOperacion;
    private final Catalogo catalogo;
    private final Map<String, Bodega> bodegas = new LinkedHashMap<>();
    private final Map<String, Lote> historial = new LinkedHashMap<>();
    private final List<Movimiento> movimientos = new ArrayList<>();
    private final List<Despacho> despachos = new ArrayList<>();
    public Cooperativa(LocalDate fechaOperacion, Catalogo catalogo) {
        this.fechaOperacion = Objects.requireNonNull(fechaOperacion);
        this.catalogo = Objects.requireNonNull(catalogo);
    }
    public LocalDate getFechaOperacion() { return fechaOperacion; }
    public Catalogo getCatalogo() { return catalogo; }
    public List<Bodega> getBodegas() { return List.copyOf(bodegas.values()); }
    public List<Movimiento> getMovimientos() { return List.copyOf(movimientos); }
    public List<Despacho> getDespachos() { return List.copyOf(despachos); }
    public List<Lote> getHistorial() {
        List<Lote> copia = new ArrayList<>(historial.values()); Collections.sort(copia); return List.copyOf(copia);
    }
    public Bodega buscarBodega(String codigo) {
        Bodega b = bodegas.get(Validar.texto(codigo, "Bodega"));
        if (b == null) throw new IllegalArgumentException("Bodega inexistente.");
        return b;
    }
    public void registrarBodega(Bodega bodega) {
        Objects.requireNonNull(bodega);
        if (bodegas.containsKey(bodega.getCodigo())) throw new IllegalArgumentException("Código de bodega repetido.");
        if (bodega.iterator().hasNext()) throw new IllegalArgumentException("La nueva bodega debe estar vacía.");
        bodegas.put(bodega.getCodigo(), bodega);
    }
    /** Cotizar construye un candidato independiente; no cambia ningún registro. */
    public Lote preliquidar(String categoria, DatosLote datos) {
        if (!datos.fechaIngreso().equals(fechaOperacion))
            throw new IllegalArgumentException("Un lote nuevo debe ingresar en la fecha de operación.");
        if (historial.containsKey(datos.codigo())) throw new IllegalArgumentException("Código de lote repetido.");
        return catalogo.buscar(categoria).crear(datos);
    }
    public Lote recibir(String categoria, DatosLote datos, String codigoBodega) {
        Lote lote = preliquidar(categoria, datos);
        ingresar(lote, codigoBodega, false);
        return lote;
    }
    /** Solo la carga de arranque usa este acceso de paquete; no registra pagos. */
    void cargarInicial(String categoria, DatosLote datos, String codigoBodega) {
        Lote lote = catalogo.buscar(categoria).crear(datos);
        ingresar(lote, codigoBodega, true);
    }
    private void ingresar(Lote lote, String codigoBodega, boolean inicial) {
        if (historial.containsKey(lote.getCodigo())) throw new IllegalArgumentException("Código de lote repetido.");
        if (lote.getFechaIngreso().isAfter(fechaOperacion) || lote.estaVencido(fechaOperacion))
            throw new IllegalArgumentException("Fecha inicial futura o lote ya vencido.");
        if (!inicial && !lote.getFechaIngreso().equals(fechaOperacion))
            throw new IllegalArgumentException("Fecha de ingreso incorrecta.");
        Bodega b = buscarBodega(codigoBodega);
        b.validarIngreso(lote);
        BigDecimal pago = lote.calcularPago();
        b.almacenar(lote); historial.put(lote.getCodigo(), lote);
        if (!inicial) movimientos.add(new Movimiento(Movimiento.Tipo.PAGO,
            lote.getCategoria(), lote.getCodigo(), fechaOperacion, pago));
    }
    /** La combinación global delega la misma comparación de cada bodega. */
    public List<Lote> existencias() {
        List<Lote> lista = new ArrayList<>();
        for (Bodega b : bodegas.values()) for (Lote l : b) lista.add(l);
        Collections.sort(lista); return List.copyOf(lista);
    }
    public List<Lote> porCategoria(String categoria) {
        catalogo.buscar(categoria);
        return existencias().stream().filter(l -> l.getCategoria().equals(categoria)).toList();
    }
    /** Incluye hoy y el límite +3; un lote aún se puede despachar el día que vence. */
    public List<Lote> proximosVencer() {
        LocalDate limite = fechaOperacion.plusDays(3);
        return existencias().stream().filter(l -> l.getVencimiento()
            .map(f -> !f.isBefore(fechaOperacion) && !f.isAfter(limite)).orElse(false)).toList();
    }
    public BigDecimal total(String categoria, Movimiento.Tipo tipo) {
        catalogo.buscar(categoria); BigDecimal suma = BigDecimal.ZERO;
        for (Movimiento m : movimientos)
            if (m.categoria().equals(categoria) && m.tipo() == tipo) suma = suma.add(m.monto());
        return suma;
    }
    public Despacho despachar(String comprador, String categoria, BigDecimal kg) {
        comprador = Validar.texto(comprador, "Comprador"); Validar.positivo(kg, "Pedido");
        List<Lote> candidatos = porCategoria(categoria);
        BigDecimal disponible = candidatos.stream().map(Lote::getDisponible).reduce(BigDecimal.ZERO, BigDecimal::add);
        if (disponible.compareTo(kg) < 0) throw new IllegalArgumentException("Existencias insuficientes; pedido rechazado sin cambios.");
        List<Despacho.Linea> lineas = new ArrayList<>();
        BigDecimal restante = kg, cobroTotal = BigDecimal.ZERO;
        // Plan completo y cálculos antes de modificar pesos o contabilidad.
        for (Lote l : candidatos) {
            if (restante.signum() == 0) break;
            BigDecimal tomar = restante.min(l.getDisponible());
            BigDecimal cobro = tomar.multiply(l.getCostoKg()).multiply(new BigDecimal("1.35"));
            lineas.add(new Despacho.Linea(l.getCodigo(), l.getCodigoBodega().orElseThrow(), tomar, cobro));
            restante = restante.subtract(tomar); cobroTotal = cobroTotal.add(cobro);
        }
        Despacho resultado = new Despacho(comprador, categoria, kg, fechaOperacion, lineas, cobroTotal);
        for (Despacho.Linea linea : lineas) {
            Lote l = historial.get(linea.codigoLote()); l.descontar(linea.kg());
            if (l.getEstado() == EstadoLote.AGOTADO) buscarBodega(linea.codigoBodega()).retirarAgotado(l);
            movimientos.add(new Movimiento(Movimiento.Tipo.COBRO, categoria, l.getCodigo(), fechaOperacion, linea.cobro()));
        }
        despachos.add(resultado); return resultado;
    }
    public void avanzarFecha(LocalDate nueva) {
        Objects.requireNonNull(nueva, "Fecha requerida.");
        if (!nueva.isAfter(fechaOperacion)) throw new IllegalArgumentException("La nueva fecha debe ser posterior a la actual.");
        // Precalcular pérdidas antes del primer cambio; retirar usando Iterator.remove.
        Map<String, BigDecimal> perdidas = new LinkedHashMap<>();
        for (Lote l : existencias()) if (l.estaVencido(nueva))
            perdidas.put(l.getCodigo(), l.getDisponible().multiply(l.getCostoKg()));
        for (Bodega b : bodegas.values()) {
            Iterator<Lote> it = b.iterator();
            while (it.hasNext()) {
                Lote l = it.next();
                if (perdidas.containsKey(l.getCodigo())) {
                    l.descartar(nueva); it.remove();
                    movimientos.add(new Movimiento(Movimiento.Tipo.PERDIDA, l.getCategoria(),
                        l.getCodigo(), nueva, perdidas.get(l.getCodigo())));
                }
            }
        }
        fechaOperacion = nueva;
    }
}
