package acopiogt;
import java.util.Scanner;
/** Punto de entrada. No contiene reglas de categorías ni de contabilidad. */
public final class Main {
    private Main() { }
    public static void main(String[] args) {
        new Consola(DatosIniciales.crear(), new Scanner(System.in)).ejecutar();
    }
}
