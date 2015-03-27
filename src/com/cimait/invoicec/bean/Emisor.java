package com.cimait.invoicec.bean;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.sun.DAO.InformacionTributaria;
import com.sun.database.ConexionBase;
import com.sun.directory.examples.InfoEmpresa;
import com.cimait.invoicec.core.ServiceData;
import com.cimait.invoicec.core.ServiceDataHilo;
import com.cimait.invoicec.db.*;
import com.cimait.invoicec.entity.FacBitacoraDocumentosEntity;
import com.cimait.invoicec.entity.FacClavescontingenciaEntity;
import com.cimait.invoicec.entity.FacEmpresaEntity;
import com.cimait.invoicec.entity.FacEstablecimientoEntity;
import com.cimait.invoicec.entity.FacEstablecimientoEntityPK;
public class Emisor {
	private InformacionTributaria infEmisor;
	private String filexml;
	private String fileXmlBackup;
	private String fileTxt;	
	private int resultado;

	private static Logger LOGGER = Logger.getLogger(ServiceDataHilo.class);
	
	public Emisor() {
		infEmisor = new InformacionTributaria();
	}

	public InformacionTributaria getInfEmisor() {
		return infEmisor;
	}

	public void setInfEmisor(InformacionTributaria infEmisor) {
		this.infEmisor = infEmisor;
	}	

	public String statusDocumento(int ambiente, String ruc, String codigoDoc, String establecimiento, 
								  String puntoEmision, String secuencial) throws SQLException, IOException, NamingException, ClassNotFoundException{
		String status = "";		
		Connection Con = DBDataSource.getInstance().getConnection();
		
    	ResultSet Rs= null;
    	PreparedStatement pst = null;
    	try{    	
	    	String sql = " Select \"ESTADO_TRANSACCION\" from " + ConexionBase.getSchema() +"fac_cab_documentos "
	    			+ "where ambiente = ? and \"Ruc\" = ? and \"CodigoDocumento\" = ? "
	    			+ " and \"CodEstablecimiento\" = ? and \"CodPuntEmision\" = ? and secuencial = ? " ;
	
	    	pst = Con.prepareStatement(sql);
	    	pst.setInt(1, ambiente);
	    	pst.setString(2, ruc);
	    	pst.setString(3, codigoDoc);
	    	pst.setString(4, establecimiento);
	    	pst.setString(5, puntoEmision);
	    	pst.setString(6, secuencial);
	    	Rs= pst.executeQuery();
	    	while (Rs.next()){ 	    		
	    		status=Rs.getString(1);	    		
	    	}
	    	Rs.close();
	    	pst.close();
	    	Con.close();
    	}catch(Exception e){
    		LOGGER.debug("Error verificando estado transaccion : " + e.getMessage() );
    		e.printStackTrace();
    	} finally {
    		if (Rs != null) Rs.close();
        	if (pst != null) pst.close();
        	if (Con != null) Con.close();
    	}
		return status;
	}
	
	
	public InformacionTributaria obtieneInfoTributaria(String ps_ruc) throws SQLException, IOException, NamingException, ClassNotFoundException{
		InformacionTributaria infTrib = new InformacionTributaria();		
		//Connection Con = ConexionBase.getConexionPostgres();
		Connection Con = DBDataSource.getInstance().getConnection();;
		
    	ResultSet Rs= null;
    	PreparedStatement pst = null;
    	try{    	
	    	String sql = " SELECT \"Ruc\", \"RazonSocial\", \"RazonComercial\", \"DireccionMatriz\", " +
	    			     " \"ContribEspecial\", \"ObligContabilidad\", \"PathCompGenerados\", \"PathCompFirmados\", \"PathInfoRecibida\",  " +
	    			     " \"PathCompAutorizados\" , \"PathCompNoAutorizados\", \"PathCompContingencia\", \"emailEnvio\",\"PassFirma\", " +
	    			     " \"TypeFirma\",\"PathXSD\",\"PathJasper\",\"PathFirma\", COALESCE(\"ruc_firmante\",\"Ruc\") as rucFirmante " +
	    				 " from " + ConexionBase.getSchema() + "fac_empresa where \"Ruc\" = ? and \"isActive\" in ('Y','1') ";
	
	    	pst = Con.prepareStatement(sql);
	    	pst.setString(1, ps_ruc);
	    	Rs= pst.executeQuery();
	    	while (Rs.next()){ 
	    		System.out.println("Version ->"+Rs.getString(1));
	    		infTrib.setRuc(Rs.getString(1));
	    		infTrib.setRazonSocial(Rs.getString(2));
	    		infTrib.setNombreComercial(Rs.getString(3));
	    		infTrib.setDireccionMatriz(Rs.getString(4));
	    		infTrib.setContribEspecial(Rs.getInt(5));
	    		infTrib.setObligContabilidad(Rs.getString(6));    		
	    		infTrib.set_pathGenerados(Rs.getString(7));
	    		infTrib.set_pathFirmados(Rs.getString(8));
	    		infTrib.set_pathInfoRecibida(Rs.getString(9));
	    		infTrib.set_pathAutorizados(Rs.getString(10));
	    		infTrib.set_pathNoAutorizados(Rs.getString(11));
	    		infTrib.set_PathCompContingencia(Rs.getString(12));
	    		infTrib.setMailEmpresa(Rs.getString(13));
	    		infTrib.set_ClaveFirma(Rs.getString(14));
	    		infTrib.set_TipoFirma(Rs.getString(15));
	    		infTrib.set_pathXsd(Rs.getString(16));
	    		infTrib.set_pathJasper(Rs.getString(17));
	    		infTrib.set_PathFirma(Rs.getString(18));
	    		infTrib.setRucFirmante(Rs.getString(19));
	    	}
	    	Rs.close();
	    	pst.close();
	    	Con.close();
    	}catch(Exception e){
        	e.printStackTrace();
        	
        	LOGGER.debug("Error en obtencion informacion tributaria : " + e.getMessage());
    	} finally {
    		if (Rs!=null) Rs.close();
        	if (pst != null) pst.close();
        	if (Con != null) Con.close();
        	
    	}
		return infTrib;
	}
	
	
	
	public InfoEmpresa obtieneInfoEmpresa(String ps_ruc) throws SQLException, IOException, NamingException, ClassNotFoundException{
		 Session session = DBDataSource.getInstance().getFactory().openSession();
	     Transaction tx = null ;
	     InfoEmpresa infEmp = null;
	     
	     try {
	            tx = session.beginTransaction();
	             Query qry = session.createQuery("from FacEmpresaEntity where ruc=:RUC");
	             qry.setString("RUC", ps_ruc);
	             List result = qry.list(); 
	            if (result != null) {
	            	FacEmpresaEntity empresa = (FacEmpresaEntity) result.get(0);
	            	infEmp = new InfoEmpresa();
		    		//no hace sentido, solo para seguir la misma logica
		    		infEmp.setRuc(empresa.getRuc());
		    		infEmp.setRazonSocial(empresa.getRazonSocial());
		    		infEmp.setRazonComercial(empresa.getRazonComercial());
		    		infEmp.setDirMatriz(empresa.getDireccionMatriz());
		    		infEmp.setContribEspecial(empresa.getContribEspecial().toString());
		    		infEmp.setObligContabilidad(empresa.getObligContabilidad());
		    		infEmp.setFecResolContribEspecial(empresa.getFechaResolucionContribEspecial());
		    		
		    		infEmp.setDirectorio(empresa.getPathCompGenerados());
		    		infEmp.setDirGenerado(empresa.getPathCompGenerados());
		    		//infEmp.setDirRecibidos(empresa.getPathCompRecepcion());
		    		infEmp.setDirRecibidos(empresa.getPathInfoRecibida());
		    		infEmp.setDirFirmados(empresa.getPathCompFirmados());
		    		infEmp.setDirAutorizados(empresa.getPathCompAutorizados());      
		    		infEmp.setDirNoAutorizados(empresa.getPathCompNoAutorizados());
		    		infEmp.setDirContingencias(empresa.getPathCompContingencia());
					
		    		infEmp.setRutaFirma(empresa.getPathFirma());
		    		infEmp.setRucFirmante(empresa.getRucFirmante());
		    		infEmp.setClaveFirma(empresa.getPassFirma());
		    		infEmp.setTipoFirma(empresa.getTypeFirma());
					
		    		infEmp.setMailEmpresa(empresa.getEmailEnvio());
		    		infEmp.setPathReports(empresa.getPathJasper());
		    		infEmp.setPathXsd(empresa.getPathXsd());
					
		    		infEmp.setDirLogo(empresa.getPathLogoEmpresa());
		    		infEmp.setServidorSmtp(empresa.getServidorSmtp());
		    		infEmp.setPortSmtp((empresa.getPuertoSmtp().toString()));
		    		infEmp.setUserSmtp(empresa.getUserSmtp());
		    		infEmp.setPassSmtp(empresa.getPassSmtp());
	            }
	            tx.commit();
	        } catch (HibernateException e) {
	            if (tx!=null) tx.rollback();
	            e.printStackTrace();
	        }finally {
	            session.close();
	        }
		return infEmp;
	}
	
	public InformacionTributaria obtieneMailEstablecimiento(InformacionTributaria infTrib) throws SQLException, IOException, NamingException, ClassNotFoundException{		
		
		Session session = DBDataSource.getInstance().getFactory().openSession();
        Transaction tx = null ;
        try {
            tx = session.beginTransaction();
            FacEstablecimientoEntityPK establecimientoSearch = new FacEstablecimientoEntityPK();
            establecimientoSearch.setRuc(infTrib.getRuc());
            establecimientoSearch.setCodEstablecimiento(infTrib.getCodEstablecimiento());
            
            FacEstablecimientoEntity establecimiento = (FacEstablecimientoEntity) session.get(FacEstablecimientoEntity.class, establecimientoSearch);
            
            if (establecimiento != null) {
            	infTrib.setMailEstablecimiento(establecimiento.getCorreo());
            	infTrib.setPathAnexoEstablecimiento(establecimiento.getPathAnexo());
            	infTrib.setMensajeEstablecimiento(establecimiento.getMensaje());
            }
        } catch (HibernateException e) {
            if (tx!=null) tx.rollback();
            e.printStackTrace();
        }finally {
            session.close();
        }
		return infTrib;
	}
	
	public boolean existeEmpresa(String ps_ruc) throws SQLException, IOException, NamingException, ClassNotFoundException{
		Session session = DBDataSource.getInstance().getFactory().openSession();
        Transaction tx = null ;
		
		boolean existEmpresa = false;
		
		try {
			 tx = session.beginTransaction();
			 Query qry = session.createQuery("Select 1 from FacEmpresaEntity where ruc=:RUC and isActive = 'Y'");
			 qry.setString("RUC", ps_ruc);
			 List<?> results = qry.list();
			 if (results != null) {
				 existEmpresa = true;
			 } 
			 tx.commit();
		} catch (HibernateException e) {
            if (tx!=null) tx.rollback();
            e.printStackTrace();
        }finally {
            session.close();
        } 
		return existEmpresa;
	}

	public boolean existeEstablecimiento(String ps_ruc, String ps_CodEstablecimiento) throws SQLException, IOException, NamingException, ClassNotFoundException{		
		//Connection Con = ConexionBase.getConexionPostgres();
		Connection Con = DBDataSource.getInstance().getConnection();;
		boolean existEstablecimiento = false;
		
    	ResultSet Rs= null;
    	PreparedStatement pst = null;
    	try{    	
	    	String sql = " SELECT 1 " +
	    				 " from " + ConexionBase.getSchema() + "fac_establecimiento where \"Ruc\" = ? " +
	    				 "and \"isActive\" in ('Y','1') and \"CodEstablecimiento\" =  ? ";
	    	pst = Con.prepareStatement(sql);
	    	pst.setString(1, ps_ruc);
	    	pst.setString(2, ps_CodEstablecimiento);
	    	Rs= pst.executeQuery();
	    	while (Rs.next()){ 
	    		existEstablecimiento = true;	    		
	    	}
    	}catch(Exception e){    		
    		LOGGER.debug("Error en existeEstablecimiento : " + e.getMessage());
        	e.printStackTrace();
    	} finally { 
    		Rs.close();
        	pst.close();
        	Con.close();
    	}
		return existEstablecimiento;
	}
	
	public boolean existePuntoEmision(String ps_ruc, String ps_CodEstablecimiento, String ps_CodPuntoEmision) throws SQLException, IOException, NamingException, ClassNotFoundException{				
		//Connection Con = ConexionBase.getConexionPostgres();
		Connection Con = DBDataSource.getInstance().getConnection();;
		boolean existPuntoEmision = false;
		
    	ResultSet Rs= null;
    	PreparedStatement pst = null;
    	try{    	
	    	String sql = " SELECT 1 " +
	    				 " from " + ConexionBase.getSchema() + "fac_punto_emision where \"Ruc\" = ? " +
	    				 "and \"isActive\" in ('Y','1') and \"CodEstablecimiento\" =  ?  and \"CodPuntEmision\" = ? ";
	    	pst = Con.prepareStatement(sql);
	    	pst.setString(1, ps_ruc);
	    	pst.setString(2, ps_CodEstablecimiento);
	    	pst.setString(3, ps_CodPuntoEmision);
	    	Rs= pst.executeQuery();
	    	while (Rs.next()){ 
	    		existPuntoEmision = true;	    		
	    	}
    	}catch(Exception e){    		
    		LOGGER.debug("Error en existePuntoEmision : " + e.getMessage());
        	e.printStackTrace();
    	} finally {
    		Rs.close();
        	pst.close();
        	Con.close();
    	}
		return existPuntoEmision;
	}
	
	public boolean existeDocumentoPuntoEmision(String ps_ruc, String ps_CodEstablecimiento, String ps_CodPuntoEmision, String tipoDocumento) throws SQLException, IOException, NamingException, ClassNotFoundException{			
		//Connection Con = ConexionBase.getConexionPostgres();
		Connection Con = DBDataSource.getInstance().getConnection();;
		boolean existPuntoEmision = false;
		
    	ResultSet Rs= null;
    	PreparedStatement pst = null;
    	try{    	
	    	String sql = " SELECT 1 " +
	    				 " from " + ConexionBase.getSchema() + "fac_punto_emision where \"Ruc\" = ? " +
	    				 "and \"isActive\" in ('Y','1') and \"CodEstablecimiento\" =  ?  and \"CodPuntEmision\" = ? ";
	    	pst = Con.prepareStatement(sql);
	    	pst.setString(1, ps_ruc);
	    	pst.setString(2, ps_CodEstablecimiento);
	    	pst.setString(3, ps_CodPuntoEmision);
	    	Rs= pst.executeQuery();
	    	while (Rs.next()){ 
	    		existPuntoEmision = true;	    		
	    	}
    	}catch(Exception e){    		
    		LOGGER.debug("Error en existeDocumentoPuntoEmision" + e.getMessage());
        	e.printStackTrace();
    	} finally {
    		Rs.close();
        	pst.close();
        	Con.close();
    	}    	    	    	
		return existPuntoEmision;
	}
	
	public String ambienteDocumentoPuntoEmision(String ps_ruc, String ps_CodEstablecimiento, String ps_CodPuntoEmision, String tipoDocumento) throws SQLException, IOException, NamingException, ClassNotFoundException{		
		//Connection Con = ConexionBase.getConexionPostgres();
		Connection Con = DBDataSource.getInstance().getConnection();;
		String ambiente = null;
		
    	ResultSet Rs= null;
    	PreparedStatement pst = null;
    	try{    	
	    	String sql = " SELECT \"TipoAmbiente\" " +
	    				 " from " + ConexionBase.getSchema() + "fac_punto_emision where \"Ruc\" = ? " +
	    				 " and \"isActive\" in ('Y','1') and \"CodEstablecimiento\" =  ?  and \"CodPuntEmision\" = ? and \"TipoDocumento\" = ? ";
	    	pst = Con.prepareStatement(sql);
	    	pst.setString(1, ps_ruc);
	    	pst.setString(2, ps_CodEstablecimiento);
	    	pst.setString(3, ps_CodPuntoEmision);
	    	pst.setString(4, tipoDocumento);
	    	Rs= pst.executeQuery();
	    	while (Rs.next()){ 
	    		ambiente = (Rs.getString(1).equals("D")?"1":(Rs.getString(1).equals("P")?"2":"-1"));  		
	    	}
    	}catch(Exception e){    		
    		LOGGER.debug("Error en ambienteDocumentoPuntoEmision : " + e.getMessage());
        	ambiente = null;
        	e.printStackTrace();
    	}	finally {
    		Rs.close();
        	pst.close();
        	Con.close();
    	}
			return ambiente;		
	}
		
	public String obtieneClaveContingencia(String ps_ruc, String ps_tipo, String ps_estado) throws SQLException, IOException, NamingException, ClassNotFoundException, ParseException{
		String claveContingencia = "";		

		Session session = DBDataSource.getInstance().getFactory().openSession();
        Transaction tx = null ;
        
        try {
			 tx = session.beginTransaction();
			 
			 Query qry = session.createQuery("Select c from FacClavescontingenciaEntity c where c.ruc=:RUC and c.tipo=:TIPO and c.estado =:ESTADO");
			 qry.setString("RUC", ps_ruc);
			 qry.setString("TIPO", ps_tipo);
			 qry.setString("ESTADO", ps_estado);
			 List<?> results = qry.list();
			 
			 if (!results.isEmpty()) {
				 //se toma la primera clave de contingencia
				 FacClavescontingenciaEntity clave = (FacClavescontingenciaEntity) results.get(0);
				 claveContingencia = clave.getClave();
				 //se actualiza su estado a usada.
				 clave.setFechauso(new java.sql.Date(new java.util.Date().getTime()));
				 SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
				 //todo grabar fecha de uso.
				 //clave.setFechauso(new java.sql.Date(sdf.parse(new java.util.Date()).getTime()));
				 clave.setEstado("1");
				 session.save(clave);
			 }
			 tx.commit();
		} catch (HibernateException e) {
            if (tx!=null) tx.rollback();
            e.printStackTrace();
        }finally {
            session.close();
        } 
		return claveContingencia;
	}
	
	public int insertaBitacoraDocumento(String ps_ambiente,
										String ps_ruc,  
									    String ps_CodEstablecimiento,
									    String ps_CodPuntEmision,
									    String ps_secuencial,
									    String ps_CodigoDocumento,
									    String ps_fechaEmision,									    
									    String ps_estadoTransaccion,
									    String ps_msgProceso,
									    String ps_msgError,
									    String ps_xmlGenerado,
									    String ps_xmlFirmado,
									    String ps_xmlRespuesta,
									    String ps_xmlAutorizacion, String invoiceNumber) throws Exception{
		int resultado = 0;
		if (ServiceData.databaseMotor.equals("PostgreSQL")){
			resultado = insertaBitacoraDocumentoPostgreSQL(ps_ambiente,ps_ruc,ps_CodEstablecimiento,ps_CodPuntEmision,ps_secuencial,
														   ps_CodigoDocumento,ps_fechaEmision,ps_estadoTransaccion,ps_msgProceso,
														   ps_msgError,ps_xmlGenerado,ps_xmlFirmado,ps_xmlRespuesta,ps_xmlAutorizacion, invoiceNumber);
		}
		if (ServiceData.databaseMotor.equals("SQLServer")){
			resultado = insertaBitacoraDocumentoSQLServer(ps_ambiente,ps_ruc,ps_CodEstablecimiento,ps_CodPuntEmision,ps_secuencial,
														  ps_CodigoDocumento,ps_fechaEmision,ps_estadoTransaccion,ps_msgProceso,
														  ps_msgError,ps_xmlGenerado,ps_xmlFirmado,ps_xmlRespuesta,ps_xmlAutorizacion);
		}
		return resultado;
	}
	//fin ea
	
	//ini ea
	public int insertaBitacoraDocumentoPostgreSQL(String ps_ambiente,
												  String ps_ruc,  
												  String ps_CodEstablecimiento,
												  String ps_CodPuntEmision,
												  String ps_secuencial,
												  String ps_CodigoDocumento,
												  String ps_fechaEmision,									    
												  String ps_estadoTransaccion,
												  String ps_msgProceso,
												  String ps_msgError,
												  String ps_xmlGenerado,
												  String ps_xmlFirmado,
												  String ps_xmlRespuesta,
												  String ps_xmlAutorizacion,
												  String invoiceNumber) throws Exception{
				
				Session session = DBDataSource.getInstance().getFactory().openSession();
			    Transaction tx = null ;
			    try {
			    	 tx = session.beginTransaction();
			    	 FacBitacoraDocumentosEntity bitacora = new FacBitacoraDocumentosEntity();
			    	 bitacora.setAmbiente(Integer.parseInt(ps_ambiente));
			    	 bitacora.setRuc(ps_ruc);
			    	 bitacora.setCodEstablecimiento(ps_CodEstablecimiento);
			    	 bitacora.setCodPuntEmision(ps_CodPuntEmision);
			    	 bitacora.setSecuencial(ps_secuencial);
			    	 bitacora.setCodigoDocumento(ps_CodigoDocumento);
			    	 
			    	 Date dt1 = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.SSSSSS").parse(ps_fechaEmision);
			    	 
			    	 bitacora.setFechaProceso(new Timestamp(dt1.getTime()));
					 bitacora.setFechaEmision(new Timestamp(dt1.getTime())); //fecha de emision deberia ser del documento
			    	 bitacora.setEstadoTransaccion(ps_estadoTransaccion);
			    	 bitacora.setMsjProceso(ps_msgProceso);
			    	 bitacora.setMsjError(ps_msgError);
			    	 bitacora.setXmlGenerado(ps_xmlGenerado);
			    	 bitacora.setXmlFirmado(ps_xmlFirmado);
			    	 bitacora.setXmlRespuesta(ps_xmlRespuesta);
			    	 bitacora.setXmlAutorizacion(ps_xmlAutorizacion);
			    	 
			    	 session.save(bitacora);
			    	   tx.commit();
		        } catch (HibernateException e) {
		            if (tx!=null) tx.rollback();
		            e.printStackTrace();
		        }finally {
		            session.close();
		        } 
				return 0;
		}
	
	public int insertaBitacoraDocumentoSQLServer(String ps_ambiente,
												  String ps_ruc,  
												  String ps_CodEstablecimiento,
												  String ps_CodPuntEmision,
												  String ps_secuencial,
												  String ps_CodigoDocumento,
												  String ps_fechaEmision,									    
												  String ps_estadoTransaccion,
												  String ps_msgProceso,
												  String ps_msgError,
												  String ps_xmlGenerado,
												  String ps_xmlFirmado,
												  String ps_xmlRespuesta,
												  String ps_xmlAutorizacion) throws Exception{
		//Connection Con = ConexionBase.getConexionPostgres();
		Connection Con = DBDataSource.getInstance().getConnection();;		
		PreparedStatement pst = null;
		try{    	
		String sql = " insert into fac_bitacora_documentos(ambiente,\"Ruc\"," +
					"\"CodEstablecimiento\",\"CodPuntEmision\"," +
					  "secuencial,\"CodigoDocumento\"," +
					"\"fechaEmision\",\"fechaProceso\"," +
					"\"ESTADO_TRANSACCION\",\"MSJ_PROCESO\"," +
					"\"MSJ_ERROR\",xml_generado,xml_firmado," +
					"xml_respuesta,xml_autorizacion) values(?,?,?,?,?,?,GETDATE(),GETDATE(),?,?,?,?,?,?,?);";
		
		//SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy");
		//String date = DATE_FORMAT.format(pd_fechaEmision);
		
		pst = Con.prepareStatement(sql);
		pst.setInt(1, Integer.parseInt(ps_ambiente));
		pst.setString(2, ps_ruc);
		pst.setString(3, ps_CodEstablecimiento);
		pst.setString(4, ps_CodPuntEmision);
		pst.setString(5, ps_secuencial);
		pst.setString(6, ps_CodigoDocumento);
		//pst.setString(7, ps_fechaEmision);
		pst.setString(7, ps_estadoTransaccion);
		pst.setString(8, ps_msgProceso);
		pst.setString(9, ps_msgError);
		pst.setString(10, ps_xmlGenerado);
		pst.setString(11, ps_xmlFirmado);
		pst.setString(12, ps_xmlRespuesta);
		pst.setString(13, ps_xmlAutorizacion);	    	
		
		int resultado = pst.executeUpdate();
		//Con.commit();	    	
		}catch(SQLException e){
		//Con.rollback();
		pst.close();
		Con.close();
		resultado = -1;
		e.printStackTrace();
		LOGGER.debug("Error en insertaBitacora : "+ e.getMessage());
		}
		return resultado;
		}
	
	
	
	/*
	public InformacionTributaria obtieneEmailAdministrador(String ps_ruc, String tipoDocumento, String) throws SQLException, IOException, NamingException, ClassNotFoundException{
		InformacionTributaria infTrib = new InformacionTributaria();
		ConexionBase Conex;
		
		Connection Con = ConexionBase.getConexionPostgres();
		
    	ResultSet Rs;
    	PreparedStatement pst;
    	String sql = " Select es.\"Correo\" " +
    			     " from fac_punto_emision pe, fac_establecimiento es " +
    			     " where pe.\"CodEstablecimiento" = es."CodEstablecimiento" and pe."Ruc" = es."Ruc" and
 pe."isActive" = '1'  and es."isActive" = '1'
 and pe."TipoDocumento" = '01' and pe."Ruc" = '0992531940001' " +
    			
    				 " from " + ConexionBase.getSchema() + "fac_empresa where \"Ruc\" = ? and \"isActive\" = 'Y' ";
    	pst = Con.prepareStatement(sql);
    	pst.setString(1, ps_ruc);
    	Rs= pst.executeQuery();
    	while (Rs.next()){ 
    		System.out.println("Version ->"+Rs.getString(1));
    		infTrib.setRuc(Rs.getString(1));
    		infTrib.setRazonSocial(Rs.getString(2));
    		infTrib.setNombreComercial(Rs.getString(3));
    		infTrib.setDireccionMatriz(Rs.getString(4));
    		infTrib.setContribEspecial(Rs.getInt(5));
    		infTrib.setObligContabilidad(Rs.getString(6));    		
    		infTrib.set_pathGenerados(Rs.getString(7));
    		infTrib.set_pathFirmados(Rs.getString(8));
    		infTrib.set_pathInfoRecibida(Rs.getString(9));
    		infTrib.set_pathAutorizados(Rs.getString(10));
    		infTrib.set_pathNoAutorizados(Rs.getString(11));
    	}
    	Rs.close();
    	pst.close();
    	Con.close();
		return infTrib;
	}*/
	
	public int obtieneSecuencia(String psRuc,String psCodEstablecimiento,String psCodPuntEmision, String psTiposDocumentos) throws Exception{
		
		ConexionBase Conex;
		
		//Connection Con = ConexionBase.getConexionPostgres();
		Connection Con = DBDataSource.getInstance().getConnection();;
		int liSecuencia = 0, liFlag = 0;
    	ResultSet Rs;
    	PreparedStatement pst;
    	String sql = " SELECT Max(secuencial) " +    			     
    				 " from " + ConexionBase.getSchema() + "fac_cab_documentos where \"Ruc\" = ? " +
    				 " and \"CodEstablecimiento\" =  ? " +
    				 " and \"CodPuntEmision\" =  ? " +
    				 " and \"CodigoDocumento\" =  ? " +    				 
    				 " and \"isActive\" in ('Y','1') ";
    	pst = Con.prepareStatement(sql);
    	pst.setString(1, psRuc);
    	pst.setString(2, psCodEstablecimiento);
    	pst.setString(3, psCodPuntEmision);
    	pst.setString(4, psTiposDocumentos);
    	Rs= pst.executeQuery();
    	while (Rs.next()){    		
    		System.out.println("Secuencial ->"+Rs.getInt(1));
    		liSecuencia = Rs.getInt(1);
    		liFlag = 1;
    		
    	}
    		if (liFlag == 0){
    			liSecuencia = -1;
    		}
    	Rs.close();
    	pst.close();
    	Con.close();
		return liSecuencia;
	}
		
	
	/**/
	
	
		
	public InformacionTributaria obtieneInfoAdicional(String ps_ruc, String ps_codEstablecimiento, String ps_codPuntoEmision, String ps_tipoDocumento) throws SQLException, IOException, NamingException, ClassNotFoundException{
		InformacionTributaria infTrib = new InformacionTributaria();
		//Connection Con = ConexionBase.getConexionPostgres();
		Connection Con = DBDataSource.getInstance().getConnection();;
    	ResultSet Rs;
    	PreparedStatement pst;
    	String sql = " Select \"TipoAmbiente\", \"FormaEmision\" from " + ConexionBase.getSchema() + " fac_punto_emision " +
    			     " where \"Ruc\" = ? " +
    			     " and \"CodEstablecimiento\" = ?  " +
    			     " and \"CodPuntEmision\" = ? " +
    			     " and \"TipoDocumento\" = ? " +
    			     " and \"isActive\" in ('Y','1') ";
    	pst = Con.prepareStatement(sql);
    	
    	pst.setString(1, ps_ruc);
    	pst.setString(2, ps_codEstablecimiento);
    	pst.setString(3, ps_codPuntoEmision);
    	pst.setString(4, ps_tipoDocumento);
    	
    	Rs= pst.executeQuery();
    	while (Rs.next()){ 
    		infTrib.setAmbiente(Integer.parseInt(Rs.getString(1)));
    		infTrib.setTipoEmision(((Rs.getString(2)==null?"1":Rs.getString(2))));
    	}
    	Rs.close();
    	pst.close();
    	Con.close();    	
    	
		return infTrib;
	}
	
//    public static void main (String arg[]) throws SQLException, IOException, NamingException, ClassNotFoundException{
//    	Emisor e = new Emisor();
//    	InformacionTributaria inf = new InformacionTributaria();
//    	//inf = e.obtieneInfoTributaria("0992531940001");
//    	   	
//    	//System.out.println("RazonSocial->"+inf.getRazonSocial());
//    	//System.out.println("RazonComercial->"+inf.getNombreComercial());
//    	String ls_clave_contingencia = e.obtieneClaveContingencia("0992800461001", "2", "0");
//    	System.out.println("Clave de Contingencia::"+ls_clave_contingencia);
//    	//System.out.println("Clave de Acceso de Contingencia::"+LeerDocumentos.generarClaveAccesoContingencia(ls_clave_contingencia));
//    }

	public String getFilexml() {
		return filexml;
	}

	public void setFilexml(String filexml) {
		this.filexml = filexml;
	}

	public int getResultado() {
		return resultado;
	}

	public void setResultado(int resultado) {
		this.resultado = resultado;
	}

	public String getFileXmlBackup() {
		return fileXmlBackup;
	}

	public void setFileXmlBackup(String fileXmlBackup) {
		this.fileXmlBackup = fileXmlBackup;
	}

	public String getFileTxt() {
		return fileTxt;
	}

	public void setFileTxt(String fileTxt) {
		this.fileTxt = fileTxt;
	}
    
}
