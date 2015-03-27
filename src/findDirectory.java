import java.io.File;
import java.io.IOException;
import java.util.Properties;


import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.ConfigurationConverter;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
//import org.apache.commons.configuration.Configuration;
//import org.apache.axis.components.logger.LogFactory;
//import org.apache.axis.utils.LockableHashtable;
import org.apache.commons.logging.Log;

public class findDirectory {

	
    protected static String configurationFileName = null;
	protected static Properties props = null;
	private PropertiesConfiguration configuration;
	private Logger log;
	
	public void configure() throws ConfigurationException, IOException{
		PropertyConfigurator.configure(configurationFileName);
		configuration = new PropertiesConfiguration(configurationFileName);
		props = ConfigurationConverter.getProperties(configuration);
        
		log.info("findDirectory Connector configurated");
		
    }
	
	public static void main(String arg[]){
		File dir = new File("D://FacturacionElectronica//");
		String[] ficheros = dir.list();
		if (ficheros == null)
			  System.out.println("No hay ficheros en el directorio especificado");
		else { 
		for (int x=0;x<ficheros.length;x++)
			System.out.println(ficheros[x]);
		}
		System.out.println("--------------------------------------------------");
		File[] ficheros2 = dir.listFiles();
		for (int x=0;x<ficheros2.length;x++){
			if (!ficheros2[x].isDirectory())
		    System.out.println(ficheros2[x].getPath());
		}
		
		       
		
	}
	
}
