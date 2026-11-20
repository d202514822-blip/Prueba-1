package repositories;

import entities.Producto;
import persistence.FileManage;

import java.util.ArrayList;
import java.util.StringTokenizer;

public class ProductoRepository {

    private final FileManage fileManage;
    private String delimiterToken;

    public ProductoRepository(){
        this.fileManage = new FileManage("productos.txt");
        this.delimiterToken = "|";
    }

    public ArrayList<Producto> getDataList(){
        ArrayList<Producto> productos = null;
        ArrayList<String> lines = this.fileManage.getDataOfFile();
        if(lines != null){
            productos = new ArrayList<>();
            for(String line : lines){
                StringTokenizer tokens = new StringTokenizer(line, this.delimiterToken);
                Long id = Long.parseLong(tokens.nextToken());
                String codigo = tokens.nextToken();
                String nombre = tokens.nextToken();
                Double precio = Double.parseDouble(tokens.nextToken());
                int stock = Integer.parseInt(tokens.nextToken());
                Producto producto = new Producto(codigo, nombre, precio, stock);
                productos.add(producto);
            }
        }

        return productos;
    }

    public void insertDataEntity(Producto producto){
        this.fileManage.writeFile(this.getLineEntity(producto));
    }

    public String getLineEntity(Producto producto){
        Long id = producto.getId();
        return id + this.delimiterToken + producto.getCodigo() + this.delimiterToken +
                producto.getNombre() + this.delimiterToken + producto.getPrecio() +
                this.delimiterToken + producto.getStock();
    }
}
