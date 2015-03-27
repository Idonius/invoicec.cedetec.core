import java.util.Formatter;


public class Prueba {
	
	public static void main(String args[]){
		int numero = 425;
		 
		Formatter fmt = new Formatter();
		fmt.format("%08d",numero);
		 
		System.out.println("El numero formateado " + fmt.toString());
	}
}
