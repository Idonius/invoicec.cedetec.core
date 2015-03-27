package com.sun.database;

import java.io.IOException;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;
import javax.naming.InitialContext;
import javax.naming.Context;
import javax.naming.NamingException;

import com.cimait.invoicec.core.ServiceData;
import com.util.util.key.Util;

import java.sql.Connection;

//import oracle.jdbc.OracleConnection;

public class ConexionBase {
	private static String Schema="";
	
	private static 	DataSource dataSource=null;

	public static String getSchema(){
		String ls_Schema = "";
		if ((ls_Schema!=null)&&(ls_Schema.length()>0)){
			ls_Schema = "."+ ConexionBase.Schema;
		}
		return ls_Schema;
	}
	private static void setupDataSource(String jndiName) throws IOException, NamingException {
    	//Utilerias propiedades = new Utilerias();
    	Context  initialContext = new InitialContext();
    	//log.info("setupDataSource request: "+ propiedades.getPropiedad("datasource.JNDI"));
    	dataSource = (DataSource)initialContext.lookup(jndiName);
    }
	/*
    public static OracleConnection getConexion(String nombreDataSource) throws SQLException, IOException, NamingException{
		if (dataSource== null)
    		setupDataSource(nombreDataSource);
		return (OracleConnection) dataSource.getConnection();
		//return  dataSource.getConnection();
    }*/
    
    public static Connection getConexion(String nombreDataSource)throws SQLException, IOException, NamingException{
    	if (dataSource== null)
    		setupDataSource(nombreDataSource);
		return (Connection) dataSource.getConnection();
		//return  dataSource.getConnection();
    	
    }
    
    public static Connection getConexionPostgres(String Driver, String Url, String User, String Pass)throws SQLException, IOException, NamingException,ClassNotFoundException{

    	Connection connection = null;    	
    	Class.forName(Driver);//"org.postgresql.Driver"
    	
    	connection = DriverManager.getConnection(Url, User, Pass);	
    			//Url -->	"jdbc:postgresql://127.0.0.1:5432/fac_electronica"
    			//User -->  "fac_electronica"
    	connection.setAutoCommit(false);
		if (connection != null) {
			System.out.println("Conexion Exitosa");
		} else {
			System.out.println("Fallo de Conexion");
		}
				
		return connection;		
	}
    
    public static Connection getConexionPostgres( )throws SQLException, IOException, NamingException,ClassNotFoundException{
    	//"org.postgresql.Driver";
    	String driver = Util.driverConection;    	
    	//"jdbc:postgresql://127.0.0.1:5432/fac_electronica";
    	String url = Util.urlConection;
    	//"fac_electronica";
    	String user = Util.userConection;
    	//"fac_electronica777";
    	String pass = Util.passwordConection;
    	Connection connection = null;    	
    	Class.forName(driver);//"org.postgresql.Driver"
    	if (url.indexOf("postgresql")>0){
    		ServiceData.databaseMotor = "PostgreSQL";
    	}
    	if (url.indexOf("sqlserver")>0){
    		ServiceData.databaseMotor = "SQLServer";
    	}
    	connection = DriverManager.getConnection(url, user, pass);
    	connection.setAutoCommit(true);
    	//Url -->	"jdbc:postgresql://127.0.0.1:5432/fac_electronica"
    	//User -->  "fac_electronica"

		if (connection != null) {
			System.out.println("Conexion Exitosa");
		} else {
			System.out.println("Fallo de Conexion");
		}
				
		return connection;		
	}
    
    public static Connection getConexionPostgresEstatica( )throws SQLException, IOException, NamingException,ClassNotFoundException{
    	//"org.postgresql.Driver";
    	String driver = "org.postgresql.Driver";    	
    	//"jdbc:postgresql://127.0.0.1:5432/fac_electronica";
    	String url = "jdbc:postgresql://192.168.32.117:5432/fac_electronica";
    	//"fac_electronica";
    	String user = "fac_electronica";
    	//"fac_electronica777";
    	String pass = "fac_electronica777";
    	Connection connection = null;    	
    	Class.forName(driver);//"org.postgresql.Driver"
    	
    	connection = DriverManager.getConnection(url, user, pass);
    	connection.setAutoCommit(false);
    	//Url -->	"jdbc:postgresql://127.0.0.1:5432/fac_electronica"
    	//User -->  "fac_electronica"

		if (connection != null) {
			System.out.println("Conexion Exitosa");
		} else {
			System.out.println("Fallo de Conexion");
		}
				
		return connection;		
	}
    
    public static void main (String arg[]) throws SQLException, IOException, NamingException, ClassNotFoundException{
    	Connection Con = getConexionPostgres("org.postgresql.Driver","jdbc:postgresql://127.0.0.1:5432/fac_electronica","fac_electronica","fac_electronica777");
    	ResultSet Rs;
    	Statement st;
    	st = Con.createStatement();
    	String sql = "SELECT version() ";    	        
    	Rs= st.executeQuery(sql);
    	while (Rs.next()){ 
    	System.out.println("Version ->"+Rs.getString(1));
    	}
    	Rs.close();
    	st.close();
    	Con.close();
    }
    public void limpiaDataSource(){
        if(dataSource!=null){
            dataSource= null;   
        }
    }
    
    /*
    public void ins (String arg[]) throws SQLException, IOException, NamingException, ClassNotFoundException{
    	Connection Con = getConexionPostgres("org.postgresql.Driver","jdbc:postgresql://127.0.0.1:5432/fac_electronica","fac_electronica","fac_electronica777");
    	ResultSet Rs;
    	Statement st;
    	st = Con.createStatement();
    	String sql = "SELECT version() ";    	        
    	Rs= st.executeQuery(sql);
    	while (Rs.next()){ 
    	System.out.println("Version ->"+Rs.getString(1));
    	}
    	Rs.close();
    	st.close();
    	Con.close();
    }
    
    insert into fac_cab_documentos(ambiente, "Ruc", "TipoIdentificacion", "CodEstablecimiento", 
		       "CodPuntEmision", secuencial, "fechaEmision", "guiaRemision", 
		       "razonSocialComprador", "identificacionComprador","totalSinImpuesto", 
		       "totalDescuento", email, propina, moneda, "infoAdicional", "periodoFiscal", 			       
		       rise, "fechaInicioTransporte", "fechaFinTransporte", placa, 
		       "fechaEmisionDocSustento","motivoRazon", "identificacionDestinatario", "razonSocialDestinatario", 
		       "direccionDestinatario", "motivoTraslado", "docAduaneroUnico", "codEstablecimientoDest", 
		       ruta, "codDocSustento", "numDocSustento", "numAutDocSustento", "fecEmisionDocSustento", 
		       autorizacion, fechaautorizacion, "claveAcceso", "importeTotal", "CodigoDocumento", 
		       "codDocModificado", "numDocModificado","motivoValor", "tipIdentificacionComprador", 
		       "tipoEmision", partida, subtotal12, subtotal0, "subtotalNoIva", "totalvalorICE", iva12, 
		       "isActive", "ESTADO_TRANSACCION", "MSJ_ERROR", "Tipo") 
		       values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,
		       ?,?,?,?,?,?,?,?,?,?,?,?,?,?);
        
	*/
}
