import entities.Producto;
import repositories.ProductoRepository;

import java.util.ArrayList;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        //TIP Press <shortcut actionId="ShowIntentionActions"/> with your caret at the highlighted text
        // to see how IntelliJ IDEA suggests fixing it.
        ProductoRepository productoRepository = new ProductoRepository();

        productoRepository.insertDataEntity(
                new Producto("P-007", "TV", 1300, 16)
        );

        ArrayList<Producto> productos = productoRepository.getDataList();
        System.out.println("====== Listado de Productos ======");
        for (Producto p : productos){
            System.out.println("-------------------------------");
            System.out.println("Código: " + p.getCodigo());
            System.out.println("Nombre: " + p.getNombre());
            System.out.println("Precio: " + p.getPrecio());
            System.out.println("Stock: " + p.getStock());
        }
    }
}