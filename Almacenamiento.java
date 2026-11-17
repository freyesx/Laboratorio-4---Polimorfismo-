package acopiogt;
/** Valor extensible: agregar CONGELADO no exige cambiar los algoritmos. */
public record Almacenamiento(String codigo) {
    public static final Almacenamiento SECO = new Almacenamiento("SECO");
    public static final Almacenamiento FRIO = new Almacenamiento("FRIO");
    public Almacenamiento { codigo = Validar.texto(codigo, "Almacenamiento"); }
}
