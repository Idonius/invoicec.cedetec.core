package com.cimait.invoicec.core;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRMapArrayDataSource;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import au.com.bytecode.opencsv.CSVWriter;

import com.cimait.invoicec.db.DBDataSource;
import com.cimait.invoicec.entity.FacCabDocumentosEntity;
import com.cimait.invoicec.entity.FacClientesEntity;
import com.cimait.invoicec.entity.FacClientesEntityPK;
import com.cimait.invoicec.entity.FacEmpresaEntity;
import com.cimait.invoicec.sri.document.SRIDocument;
import com.cimait.invoicec.sri.document.SRIDocumentFactory;
import com.cimait.invoicec.sri.schema.creditnote.NotaCredito;
import com.cimait.invoicec.sri.schema.creditnote.NotaCredito.InfoNotaCredito;
import com.cimait.invoicec.sri.schema.debitnote.NotaDebito;
import com.cimait.invoicec.sri.schema.debitnote.NotaDebito.InfoNotaDebito;
import com.cimait.invoicec.sri.schema.general.Impuesto;
import com.cimait.invoicec.sri.schema.invoice.Factura;
import com.cimait.invoicec.sri.schema.invoice.Factura.InfoFactura;
import com.cimait.invoicec.sri.schema.invoice.Factura.InfoAdicional.CampoAdicional;
import com.cimait.invoicec.sri.schema.invoice.Factura.InfoFactura.TotalConImpuestos.TotalImpuesto;
import com.cimait.invoicec.sri.schema.retention.ComprobanteRetencion;
import com.cimait.invoicec.sri.schema.retention.ComprobanteRetencion.InfoCompRetencion;
import com.sun.DAO.ControlErrores;
import com.sun.comprobantes.util.EmailSender;
import com.sun.comprobantes.util.FormGenerales;
import com.sun.directory.examples.ArchivoUtils;
import com.sun.directory.examples.AutorizacionComprobantesWs;
import com.thoughtworks.xstream.XStream;
import com.tradise.reportes.util.key.GenericTransaction;
import com.util.util.key.Environment;
import com.util.webServices.EnvioComprobantesWs;

import ec.gob.sri.comprobantes.util.xml.XStreamUtil;
import ec.gob.sri.comprobantes.ws.RespuestaSolicitud;
import ec.gob.sri.comprobantes.ws.aut.Autorizacion;
import ec.gob.sri.comprobantes.ws.aut.Mensaje;
import ec.gob.sri.comprobantes.ws.aut.RespuestaComprobante;

public class ServiceDataCT  extends GenericTransaction { 
	
	//reusando la misma m 
	public static ArrayList<ControlErrores> ListErrorGeneral = null;
	public static ArrayList<ControlErrores> ListWarnGeneral = null;
	public static String emailHost = null;
	public static String emailFrom = null;
	public static String emailTo = null;
	public static String emailSubject = null;
	public static String emailMensaje = null;	
	public static String emailHelpDesk = null;
	
	
	//helpers
	private Factura tmpFact = null;
	private NotaCredito tmpNC = null;
	private NotaDebito tmpND = null;
	private ComprobanteRetencion tmpCR = null;
	
	private static Logger LOGGER = Logger.getLogger(ServiceDataCT.class);
	
	public  ServiceDataCT(List listError, List ListWarn,List listErrorEstados,List listWarnEstados) {
		ListErrorGeneral = new ArrayList<ControlErrores>();
		ListWarnGeneral = new ArrayList<ControlErrores>();
		for (int i=0; i<listError.size();i++)
   	 	{	ControlErrores ctrl= new ControlErrores(); 
   	 		ctrl.setEstado(listErrorEstados.get(i).toString());
   	 	    ctrl.setMensaje(listError.get(i).toString());
   	 	    ctrl.setTipo("E");
			ListErrorGeneral.add(ctrl);
   	 	}
		
		ListErrorGeneral = new ArrayList<ControlErrores>();
		for (int i=0; i<ListWarn.size();i++)
   	 	{	ControlErrores ctrl= new ControlErrores(); 
   	 		ctrl.setEstado(listWarnEstados.get(i).toString());
   	 	    ctrl.setMensaje(ListWarn.get(i).toString());
   	 	    ctrl.setTipo("W");
			ListErrorGeneral.add(ctrl);
   	 	}
	}
	
	
	public ArrayList<String>  EmisionContingencia(String codigoDocumento , File fxml) throws JAXBException {
		 JAXBContext jaxbContext = null ;
		 
		 ArrayList<String> ret = new ArrayList<String>();
		 
		 if (codigoDocumento.trim().equals("01")) {
			 jaxbContext = JAXBContext.newInstance(Factura.class); 
		     Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			 Factura uXMLDoc = (Factura) jaxbUnmarshaller.unmarshal(fxml);

			this.tmpFact = uXMLDoc;

			ret.add(uXMLDoc.getInfoTributaria().getTipoEmision().trim());
			 ret.add(uXMLDoc.getInfoTributaria().getClaveAcceso().trim());
			 
		 }else if (codigoDocumento.trim().equals("04")) {
			 jaxbContext = JAXBContext.newInstance(NotaCredito.class); 
		     Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			 NotaCredito uXMLDoc = (NotaCredito) jaxbUnmarshaller.unmarshal(fxml);
			
			 tmpNC = uXMLDoc;

			 
			 ret.add(uXMLDoc.getInfoTributaria().getTipoEmision().trim());
			 ret.add(uXMLDoc.getInfoTributaria().getClaveAcceso().trim());
		 }else if (codigoDocumento.trim().equals("05")) {
			 jaxbContext = JAXBContext.newInstance(NotaDebito.class); 
		     Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			 NotaDebito uXMLDoc = (NotaDebito) jaxbUnmarshaller.unmarshal(fxml);

			 tmpND = uXMLDoc;

			 
			 ret.add(uXMLDoc.getInfoTributaria().getTipoEmision().trim());
			 ret.add(uXMLDoc.getInfoTributaria().getClaveAcceso().trim());
		 }else if (codigoDocumento.trim().equals("07")) {
			 jaxbContext = JAXBContext.newInstance(ComprobanteRetencion.class); 
		     Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			 ComprobanteRetencion uXMLDoc = (ComprobanteRetencion) jaxbUnmarshaller.unmarshal(fxml);

			 tmpCR = uXMLDoc;
				
			 ret.add(uXMLDoc.getInfoTributaria().getTipoEmision().trim());
			 ret.add(uXMLDoc.getInfoTributaria().getClaveAcceso().trim());
		 }
		 return ret;
	}
	
	public String isAuthorized(FacCabDocumentosEntity doc,FacEmpresaEntity emitter, String claveContingencia) {
		String aut = "";
		String clave = doc.getClaveAcceso();
		if (!claveContingencia.trim().equals("")) clave = claveContingencia; 
		
		String nombreArchivo = doc.getRuc() + "-" + doc.getCodigoDocumento() + "-" + doc.getCodEstablecimiento() + doc.getCodPuntEmision() + doc.getSecuencial(); 
		try {
			 RespuestaComprobante respAut = null;
			 respAut = new AutorizacionComprobantesWs(com.sun.comprobantes.util.FormGenerales.devuelveUrlWs(Integer.toString(doc.getAmbiente()), "AutorizacionComprobantes")).llamadaWSAutorizacionInd(clave);
			 
			 if (respAut != null) {
				 if(respAut.getAutorizaciones().getAutorizacion().size()>0){
			        	LOGGER.info("respuesta::"+respAut.getNumeroComprobantes()+"::Estado::"+respAut.getAutorizaciones().getAutorizacion().get(0).getEstado());
			     }
				 int i= 0; //?? solo el primero
				 for (Autorizacion item : respAut.getAutorizaciones().getAutorizacion()) {
			          item.setComprobante("<![CDATA[" + item.getComprobante() + "]]>");

			          XStream xstream = XStreamUtil.getRespuestaXStream();
			          Writer writer = null;
			          ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			          writer = new OutputStreamWriter(outputStream, "UTF-8");
			          writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			          xstream.toXML(item, writer);
			          String xmlAutorizacion = outputStream.toString("UTF-8");
			          if ((i == 0) && (item.getEstado().equals("AUTORIZADO"))) {
			            aut="AT|" + item.getNumeroAutorizacion()+"|"+item.getFechaAutorizacion();
			            ArchivoUtils.stringToArchivo(emitter.getPathCompAutorizados() + nombreArchivo + ".xml", xmlAutorizacion);
			            break;
			          }
			          if (item.getEstado().equals("NO AUTORIZADO")) {
			        	aut="NA| | ";
			            if (verificarOCSP(item)){            	
			            	LOGGER.debug("No se puede validar el certificado digital.");
			            }else{
			            	ArchivoUtils.stringToArchivo(emitter.getPathCompNoAutorizados() + nombreArchivo + ".xml" , xmlAutorizacion);
			            }
			            break;
			          }
			          i++;
			        }
			 }
		} catch (Exception e) {
			LOGGER.debug("Error en llamada de autorizacion " + e.getMessage());
			e.printStackTrace();
		}
		return aut;
	}
	
	
	public void reenvia(FacCabDocumentosEntity doc) throws Exception {
		//obtengo datos de emisor
		
		 String documentName = doc.getRuc().trim() + "-" + doc.getCodigoDocumento().trim() + "-" + doc.getCodEstablecimiento() + doc.getCodPuntEmision() + doc.getSecuencial() + ".xml";
		 String documentType = doc.getCodigoDocumento();
		 String documentNumber = doc.getCodEstablecimiento() + doc.getCodPuntEmision() + doc.getSecuencial();
		 
		 List<HashMap<String,String>> alternativeResponse = new ArrayList<HashMap<String,String>>();
		
		
		Session session = DBDataSource.getInstance().getFactory().openSession();
		 try {
			 FacEmpresaEntity emitter = (FacEmpresaEntity) session.get(FacEmpresaEntity.class, doc.getRuc());
			 
			 //primero se toma su clave de acceso original por si paso solicitud de recepcion y se desconecto en autorizacion,
			 //se pregunta si ya fue autorizada
			 String strAut = isAuthorized(doc,emitter,"");
			 if (!strAut.equals("")) {
				 if (strAut.split("\\|")[0].equals("AT")) {
					//actualizo status de documento
					 updateDocumentStatus(doc, "AT", "Autorizado por SRI");
					
					 SRIDocument sriDocument = SRIDocumentFactory.getSRIDocument(doc.getCodigoDocumento());
					 
					 if (doc.getCodigoDocumento().trim().equals("01")) {
						 sriDocument.setData(tmpFact);
					 } else if (doc.getCodigoDocumento().trim().equals("04")) {
						 sriDocument.setData(tmpNC);
					 } else if (doc.getCodigoDocumento().trim().equals("05")) {
						 sriDocument.setData(tmpND);
					 } else if (doc.getCodigoDocumento().trim().equals("07")) {
						 sriDocument.setData(tmpCR);
					 }
					 
					 String pdfGenerated = generatePDF(sriDocument, strAut.split("\\|")[1], strAut.split("\\|")[2], false, emitter);
					 
					 HashMap<String, String> hMap = new HashMap<String, String>();
					 hMap.put("AuthNumber",strAut.split("\\|")[1]);
					 hMap.put("FechaAutorizacion", strAut.split("\\|")[2]);
					 hMap.put("codigo1", "AT");
					 hMap.put("desc1", "Autrizado por SRI");
					 
					 exportResponse(documentName, documentType, documentNumber, alternativeResponse, "N", emitter.getPathCompGenerados());
					 enviaEmailCliente("message_exito", doc.getFechaEmision().toString(), "","",emitter.getPathCompAutorizados()+documentName ,pdfGenerated,getCustomer(doc.getRuc(), doc.getIdentificacionComprador()).getEmail() + ";" + doc.getInfoAdicional(),documentNumber);
					//archivo xml con aut ya fue creado en autorizados 
				 } else if (strAut.split("\\|")[0].equals("NA")) {
					 updateDocumentStatus(doc, "NA", "No Autorizado por SRI");
					 HashMap<String, String> hMap = new HashMap<String, String>();
					 hMap.put("AuthNumber","");
					 hMap.put("FechaAutorizacion", "");
					 hMap.put("codigo1", "NA");
					 hMap.put("desc1", "No Autorizado por SRI");
					 
					 exportResponse(documentName, documentType, documentNumber, alternativeResponse, "N", emitter.getPathCompGenerados());
					 enviaEmailCliente("message_error", doc.getFechaEmision().toString(), "","","" ,"",doc.getInfoAdicional(),documentNumber);
					 //archivo xml ya fue creado en no autorizados
				 }
			 } else {
				 //se usa archivo de contingencia en firmados, 
				//si esta en contingencia debe existir archivo XML en directorio firmados , con nueva clave de acceso y tipo de emision.
				 String contingenciaXML = emitter.getPathCompFirmados() + doc.getRuc().trim() + "-" + doc.getCodigoDocumento().trim() + "-" + doc.getCodEstablecimiento() + doc.getCodPuntEmision() + doc.getSecuencial() + ".xml";
				 File fCTXML = new File(contingenciaXML);
				 if (fCTXML.exists()) {
					 //si esta con tipo de emision 2 = CT
					 ArrayList<String> aValueXML = EmisionContingencia(doc.getCodigoDocumento(), fCTXML); 
					 
					 if (aValueXML.get(0).equals("2")) { //contingencia
						 LOGGER.debug("Enviando solicitud de recepcion");
						 try {
							 ec.gob.sri.comprobantes.ws.RespuestaSolicitud respuestaRecepcion = new ec.gob.sri.comprobantes.ws.RespuestaSolicitud();
							 respuestaRecepcion = EnvioComprobantesWs.obtenerRespuestaEnvio(fCTXML, 
									   doc.getRuc(), 
									   doc.getCodigoDocumento(), 
									   aValueXML.get(1), //clave de acceso contingente 
									   FormGenerales.devuelveUrlWs(Integer.toString(doc.getAmbiente()) ,"RecepcionComprobantes"),
									   30000, ListErrorGeneral, ListWarnGeneral);
							 
							 if (respuestaRecepcion != null) {
								 LOGGER.debug(respuestaRecepcion.getEstado());
								 if (respuestaRecepcion.getEstado().trim().equals("RECIBIDA")) {
									 String strAutContingencia = isAuthorized(doc,emitter,aValueXML.get(1));
									 if (!strAutContingencia.equals("")) {
										 if (strAutContingencia.split("\\|")[0].equals("AT")) {
											//actualizo status de documento
											 updateDocumentStatus(doc, "AT", "Autorizado por SRI");
											//archivo xml con aut ya fue creado en autorizados
											//generopdf
											 SRIDocument sriDocument = SRIDocumentFactory.getSRIDocument(doc.getCodigoDocumento());
											 
											 if (doc.getCodigoDocumento().trim().equals("01")) {
												 sriDocument.setData(tmpFact);
											 } else if (doc.getCodigoDocumento().trim().equals("04")) {
												 sriDocument.setData(tmpNC);
											 } else if (doc.getCodigoDocumento().trim().equals("05")) {
												 sriDocument.setData(tmpND);
											 } else if (doc.getCodigoDocumento().trim().equals("07")) {
												 sriDocument.setData(tmpCR);
											 }
											 
											 HashMap<String, String> hMap = new HashMap<String, String>();
											 hMap.put("AuthNumber",strAutContingencia.split("\\|")[1]);
											 hMap.put("FechaAutorizacion", strAutContingencia.split("\\|")[2]);
											 hMap.put("codigo1", "AT");
											 hMap.put("desc1", "Autorizado por SRI");
											 
											 alternativeResponse.add(hMap);
											 
											 String pdfGenerated = generatePDF(sriDocument, strAutContingencia.split("\\|")[1], strAutContingencia.split("\\|")[2], false, emitter);
											 exportResponse(documentName, documentType, documentNumber, alternativeResponse, "N", emitter.getPathCompGenerados());
											 enviaEmailCliente("message_exito", doc.getFechaEmision().toString(), "","",emitter.getPathCompAutorizados()+documentName ,pdfGenerated,getCustomer(doc.getRuc(), doc.getIdentificacionComprador()).getEmail() + ";" + doc.getInfoAdicional(),documentNumber);
										 } else if (strAutContingencia.split("\\|")[0].equals("NA")) {
											 updateDocumentStatus(doc, "NA", "No Autorizado por SRI");
											 //archivo xml ya fue creado en no autorizados
											 HashMap<String, String> hMap = new HashMap<String, String>();
											 hMap.put("AuthNumber","");
											 hMap.put("FechaAutorizacion", "");
											 hMap.put("codigo1", "NA");
											 hMap.put("desc1", "No Autorizado por SRI");
											 
											 exportResponse(documentName, documentType, documentNumber, alternativeResponse, "N", emitter.getPathCompGenerados());
											 enviaEmailCliente("message_error", doc.getFechaEmision().toString(), "","","" ,"",doc.getInfoAdicional(),documentNumber);
										 }
									 } else { //sin respuesta ? se queda en CT
										 LOGGER.debug("Se mantiene en CT");
										 updateDocumentStatus(doc, "CT", "Contingencia por SRI");
									 }
								 } else {
									 //otro error
									 
									 HashMap<String, String> hMap = new HashMap<String, String>();
									 hMap.put("AuthNumber","");
									 hMap.put("FechaAutorizacion", "");
									 hMap.put("codigo1", "DS");
									 hMap.put("desc1", "devuelto SRI");
									 alternativeResponse.add(hMap);
								 }
								 
							 } else {
								 HashMap<String, String> hMap = new HashMap<String, String>();
								 hMap.put("AuthNumber","");
								 hMap.put("FechaAutorizacion", "");
								 hMap.put("codigo1", "DS");
								 hMap.put("desc1", "devuelto SRI");
								 alternativeResponse.add(hMap);
							 }
						 } catch (Exception e) {
							 LOGGER.debug("Error en envio solicitud de recepcion " + e.getMessage());
							 e.printStackTrace();
							 LOGGER.debug("Se mantiene en CT");
							 //actualizo status de documento a que siga en CT
							 updateDocumentStatus(doc, "CT", "Contingencia por SRI");
						 }
					 } else {
						 LOGGER.debug("XML no es de tipo de emision 2 = CT : " + contingenciaXML);
						 updateDocumentStatus(doc, "CT", "Contingencia por SRI");
					 }
				 } else {
					 LOGGER.debug("Archivo no existe : " + contingenciaXML);
					 updateDocumentStatus(doc, "CT", "Contingencia por SRI");
				 }
			 }
		 } catch (HibernateException e) {
	         e.printStackTrace();
	         updateDocumentStatus(doc, "CT", "Contingencia por SRI");
	     }finally {
	         session.close();
	     }		 
	}
	
	
	public void atiendeHilo() throws Exception{
		Session session = DBDataSource.getInstance().getFactory().openSession();
		Transaction tx = null ;
		 try {
        	tx = session.beginTransaction();
        	//TODO : eliminar ruc 
        	Query qry = session.createQuery("from FacCabDocumentosEntity where estadoTransaccion='CT' and ruc = '1792063337001'");
        	List<?> result = qry.list();
        	
        	if (!result.isEmpty()) {
        		LOGGER.debug("Documentos en CT encontrado : " + result.size());
        		FacCabDocumentosEntity doc = (FacCabDocumentosEntity) result.get(0);
        		//actualizar a RR el status del doc 
        		updateDocumentStatus(doc,"RR","En reproceso");
        		tx.commit();
        		LOGGER.debug("reprocesando documento : " + doc.getCodigoDocumento() + "-" + doc.getCodEstablecimiento() + doc.getCodPuntEmision() + "-" + doc.getSecuencial() );
        		reenvia(doc);
        	}
        	
		 } catch (HibernateException e) {
	         if (tx!=null) tx.rollback();
	         e.printStackTrace();
	     }finally {
	         session.close();
	     }
	}
	

	private void exportResponse(String documentName, String documentType, String documentNumber, List<HashMap<String,String>> alternativeResponse, String reenvio, String ruta) throws IOException {
		String pathExport = FilenameUtils.normalize(ruta+".." + File.separator+ "out") + File.separator;
		File exportFile = new File(pathExport + "R-"+ FilenameUtils.removeExtension(documentName)+".TXT");
		 FileWriter fw = new FileWriter(exportFile);
		CSVWriter writer = new CSVWriter(fw, '|',CSVWriter.NO_QUOTE_CHARACTER); 
		
		//header : RC
		//detail : DC
		SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat sdf2 = new SimpleDateFormat("HH:mm:ss");
		String lineHeader[] = new String[12];  
		lineHeader[0] = "RC";
		lineHeader[1] = documentName.substring(0,13); //ruc
		lineHeader[2] = documentNumber; 
		lineHeader[3] = documentType;
		lineHeader[4] = alternativeResponse.get(0).get("AuthNumber");
		lineHeader[5] = sdf1.format(new Date()); //send date
		lineHeader[6] = sdf2.format(new Date()); //send time
		
		if (alternativeResponse.get(0).get("FechaAutorizacion") == null || alternativeResponse.get(0).get("FechaAutorizacion").equals("")) {
			lineHeader[7] = sdf1.format(new Date()); //reponse date
			lineHeader[8] = sdf2.format(new Date()); //response time
		} else {
			lineHeader[7] = alternativeResponse.get(0).get("FechaAutorizacion").split("T")[0];
			lineHeader[8] = alternativeResponse.get(0).get("FechaAutorizacion").split("T")[1];
		}
		
		lineHeader[9] = alternativeResponse.get(0).get("codigo1");
		lineHeader[10] = alternativeResponse.get(0).get("desc1"); //response desc
		lineHeader[11] = reenvio; //resendable
		writer.writeNext(lineHeader);
		for (HashMap<String,String> hMap : alternativeResponse) {
			if (hMap.get("codigo2") != null ) {
			if (!hMap.get("codigo2").trim().equals("")) {
				String lineDetail[] = new String[3];
				lineDetail[0] = "DC";
				lineDetail[1] = hMap.get("codigo2");
				lineDetail[2] = hMap.get("desc2");
				writer.writeNext(lineDetail);
			}
			}
		}
		writer.close();
	}
	
	@Override
	public synchronized void run() {
		try{
			atiendeHilo();	
		}catch(Exception e){
			e.printStackTrace();
		}finally{
		}
	}
	
	public static boolean verificarOCSP(Autorizacion autorizacion)
		    throws Exception
		  {
		    boolean respuesta = false;

		    for (Mensaje m : autorizacion.getMensajes().getMensaje()) {
		      if (m.getIdentificador().equals("61")) {    	
		        respuesta = true;
		      }
		    }
		    return respuesta;
		  }
	
	private void updateDocumentStatus(FacCabDocumentosEntity doc, String status, String msg) throws SQLException {
		Session session = DBDataSource.getInstance().getFactory().openSession();
        Transaction tx = null;
        try {
        	tx = session.beginTransaction();
        	Query qry = session.createQuery("from FacCabDocumentosEntity where ambiente = :AMBIENTE and ruc=:RUC and codEstablecimiento=:CODESTABLECIMIENTO and codPuntEmision = :CODPUNTOEMISION and secuencial = :SECUENCIAL");
        	qry.setParameter("AMBIENTE", doc.getAmbiente());
        	qry.setParameter("RUC", doc.getRuc());
        	qry.setParameter("CODESTABLECIMIENTO", doc.getCodEstablecimiento());
        	qry.setParameter("CODPUNTOEMISION", doc.getCodPuntEmision());
        	qry.setParameter("SECUENCIAL", doc.getSecuencial());
        	List<?> result = qry.list();
        	if (!result.isEmpty()) {
        		FacCabDocumentosEntity documento = (FacCabDocumentosEntity) result.get(0);
        		documento.setEstadoTransaccion(status);
        		documento.setMsjError(msg);
        		session.save(documento);
        	}
        	tx.commit();
		 } catch (HibernateException e) {
	         if (tx!=null) tx.rollback();
	         e.printStackTrace();
	     }finally {
	         session.close();
	     }
	}
	
	private String  generatePDF(SRIDocument document, String NroAutorizacion, String fecAutorizacion, boolean contingencia, FacEmpresaEntity emitter) throws IOException, HibernateException, SQLException {
		String pathJasper = emitter.getPathJasper();
		String jasperFile = ""; 
		String pdfFileName="";
		
		Map<String,Object> params = new HashMap<String,Object>();

		params.put("inputHeader", getHeaderValues(document, NroAutorizacion,fecAutorizacion));
		params.put("LogoPath",pathJasper);
		params.put(JRParameter.REPORT_LOCALE, Locale.US); 
		
		try {
			if (document.getInfoTributaria().getCodDoc().equals("01")) {
				jasperFile = pathJasper + "invoice.jasper";
			} else if (document.getInfoTributaria().getCodDoc().equals("04")) {
				jasperFile = pathJasper + "creditnote.jasper";
			} else if (document.getInfoTributaria().getCodDoc().equals("05")) {
				jasperFile = pathJasper + "debitnote.jasper";
			} else if (document.getInfoTributaria().getCodDoc().equals("07")) {
				jasperFile = pathJasper + "retention.jasper";
			} else {
				throw new Exception("tipo de pdf a generar invalido");
			}
			FileInputStream is = new FileInputStream(jasperFile);
			JRMapArrayDataSource datasource = new JRMapArrayDataSource(getDetailValues(document));
			//JasperPrint pdfRender = JasperFillManager.fillReport(is, params,new JREmptyDataSource());
			JasperPrint pdfRender = JasperFillManager.fillReport(is, params,datasource);
			if (!contingencia) {
				pdfFileName = emitter.getPathCompAutorizados()+ document.getRUC()+"-"+document.getInfoTributaria().getCodDoc()+"-"+document.getInfoTributaria().getEstab()+document.getInfoTributaria().getPtoEmi()+document.getInfoTributaria().getSecuencial()+".PDF";
			} else {
				pdfFileName = emitter.getPathCompContingencia()+ document.getRUC()+"-"+document.getInfoTributaria().getCodDoc()+"-"+document.getInfoTributaria().getEstab()+document.getInfoTributaria().getPtoEmi()+document.getInfoTributaria().getSecuencial()+".PDF";
			}
			JasperExportManager.exportReportToPdfFile(pdfRender,	pdfFileName);
			is.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return pdfFileName;
	}
	
	private Map[] getDetailValues(SRIDocument doc) {
		Map mDataSource[] = null; 
		if (doc.getInfoTributaria().getCodDoc().equals("01")) {
			mDataSource= new HashMap[((com.cimait.invoicec.sri.schema.invoice.Factura.Detalles)doc.getInfoDetail()).getDetalle().size()];
			int i = 0;
			for(com.cimait.invoicec.sri.schema.invoice.Factura.Detalles.Detalle detalle : ((com.cimait.invoicec.sri.schema.invoice.Factura.Detalles)doc.getInfoDetail()).getDetalle()) {
				HashMap<String, Object> tmpMap = new HashMap<String, Object>();
				tmpMap.put("CodigoPrincipal",detalle.getCodigoPrincipal());
				tmpMap.put("Descripcion", detalle.getDescripcion());
				tmpMap.put("Cantidad", detalle.getCantidad().toString());
				tmpMap.put("PrecioUnitario",detalle.getPrecioUnitario().toString());
				tmpMap.put("Descuento",detalle.getDescuento().toString());
				tmpMap.put("TotalSinImpuestos",detalle.getPrecioTotalSinImpuesto().toString());
				mDataSource[i++] = tmpMap;
			}
		} else if (doc.getInfoTributaria().getCodDoc().equals("04")) {
			mDataSource= new HashMap[((com.cimait.invoicec.sri.schema.creditnote.NotaCredito.Detalles)doc.getInfoDetail()).getDetalle().size()];
			int i = 0;
			for (com.cimait.invoicec.sri.schema.creditnote.NotaCredito.Detalles.Detalle detalle : ((com.cimait.invoicec.sri.schema.creditnote.NotaCredito.Detalles)doc.getInfoDetail()).getDetalle()) {
				HashMap<String, Object> tmpMap = new HashMap<String, Object>();
				tmpMap.put("CodigoPrincipal", detalle.getCodigoInterno());
				tmpMap.put("Descripcion", detalle.getDescripcion());
				tmpMap.put("Cantidad", detalle.getCantidad().toString());
				tmpMap.put("PrecioUnitario", detalle.getPrecioUnitario().toString());
				tmpMap.put("Descuento", (detalle.getDescuento() == null?"0.00":detalle.getDescuento().toString()));
				tmpMap.put("TotalSinImpuestos", detalle.getPrecioTotalSinImpuesto().toString());
				mDataSource[i++] = tmpMap;
			}
		} else if (doc.getInfoTributaria().getCodDoc().equals("05")) {
			mDataSource= new HashMap[((com.cimait.invoicec.sri.schema.debitnote.NotaDebito.Motivos)doc.getInfoDetail()).getMotivo().size()];
			int i = 0;
			for (com.cimait.invoicec.sri.schema.debitnote.NotaDebito.Motivos.Motivo detalle : ((com.cimait.invoicec.sri.schema.debitnote.NotaDebito.Motivos)doc.getInfoDetail()).getMotivo()) {
				HashMap<String, Object> tmpMap = new HashMap<String, Object>();
				tmpMap.put("Razon", detalle.getRazon());
				tmpMap.put("Valor", detalle.getValor().toString());
				mDataSource[i++] = tmpMap;
			}
		} else if (doc.getInfoTributaria().getCodDoc().equals("07")) {
			mDataSource= new HashMap[((com.cimait.invoicec.sri.schema.retention.ComprobanteRetencion.Impuestos)doc.getInfoDetail()).getImpuesto().size()];
			int i = 0;
			for (com.cimait.invoicec.sri.schema.retention.Impuesto detalle : ((com.cimait.invoicec.sri.schema.retention.ComprobanteRetencion.Impuestos) doc.getImpuestos()).getImpuesto()) {
				HashMap<String, Object> tmpMap = new HashMap<String, Object>();
				
				String strTipoDocumentoMod = "";
				String tipoDocumentoMod = detalle.getCodDocSustento();
				if (tipoDocumentoMod.trim().equals("01")) {
					strTipoDocumentoMod = "FACTURA";
				} else if (tipoDocumentoMod.trim().equals("04")) {
					strTipoDocumentoMod = "NOTA DE CREDITO";
				} else if (tipoDocumentoMod.trim().equals("05")) {
					strTipoDocumentoMod = "NOTA DE DEBITO";
				} else if (tipoDocumentoMod.trim().equals("07")) {
					strTipoDocumentoMod = "COMPROBANTE DE RETENCIÓN";
				}
				
				tmpMap.put("TipoComprobante", strTipoDocumentoMod);
				tmpMap.put("NroComprobante", detalle.getNumDocSustento());
				tmpMap.put("FecComprobante", detalle.getFechaEmisionDocSustento());
				//periodo fiscal por cabecera
				//tmpMap.put("PerFiscal", detalle.get)
				
				tmpMap.put("BaseImponible",detalle.getBaseImponible().toString());
				//extraer codigo de retencion
				String strTipoImpuesto = "";
				if (detalle.getCodigo().equals("1")) {
					strTipoImpuesto = "RENTA";
				} else if (detalle.getCodigo().equals("2")) {
					strTipoImpuesto = "IVA";
				} else if (detalle.getCodigo().equals("2")) {
					strTipoImpuesto = "ISD";
				}
				
				tmpMap.put("Impuesto", strTipoImpuesto);

				tmpMap.put("PorcentajeRetencion", detalle.getPorcentajeRetener().toString());
				tmpMap.put("ValorRetenido", detalle.getValorRetenido().toString());
				mDataSource[i++] = tmpMap;
			}
		}
		return mDataSource;
	}
	
	public FacClientesEntity getCustomer(String ruc, String rucCustomer) throws HibernateException, SQLException {
		Session session = DBDataSource.getInstance().getFactory().openSession();
		FacClientesEntity cliente = null;
		  try {
		FacClientesEntityPK clienteSearch = new FacClientesEntityPK();
    	clienteSearch.setRuc(ruc);
    	clienteSearch.setRucCliente(rucCustomer);
    	cliente = (FacClientesEntity) session.get(FacClientesEntity.class, clienteSearch);
		  } catch (HibernateException e) {
		         e.printStackTrace();
		     }finally {
		         session.close();
		     }
		return cliente ;
	}
	
	
	private HashMap<String, String> getHeaderValues(SRIDocument doc, String nroAutorizacion, String fecAutorizacion) throws HibernateException, SQLException {
		HashMap<String, String> tmpMap = new HashMap<String, String>();
		//comun :
		tmpMap.put("Numero", doc.getInfoTributaria().getEstab().trim() + "-" + doc.getInfoTributaria().getPtoEmi() + "-" + doc.getInfoTributaria().getSecuencial());
		tmpMap.put("FecAutorizacion", fecAutorizacion);
		tmpMap.put("NroAutorizacion", nroAutorizacion);
		tmpMap.put("Ambiente", (doc.getInfoTributaria().getAmbiente().equals("1")?"PRUEBAS":"PRODUCCIÓN"));
		tmpMap.put("TipoEmision", (doc.getInfoTributaria().getTipoEmision().equals("1")?"NORMAL":"CONTINGENCIA"));
		tmpMap.put("ClaveAcceso",doc.getInfoTributaria().getClaveAcceso());
		
		
		FacClientesEntity cliente = null;
		
		
		if (doc.getInfoTributaria().getCodDoc().equals("01")) {
			tmpMap.put("ContribuyenteEspecial",((InfoFactura)doc.getInfo()).getContribuyenteEspecial());
			tmpMap.put("Obligado", ((InfoFactura)doc.getInfo()).getObligadoContabilidad());
			tmpMap.put("RUCcliente", ((InfoFactura)doc.getInfo()).getIdentificacionComprador());
			tmpMap.put("RazonSocialComprador", ((InfoFactura)doc.getInfo()).getRazonSocialComprador());
			
			cliente = getCustomer(doc.getRUC(),((InfoFactura)doc.getInfo()).getIdentificacionComprador()); 
			
			if (cliente != null ) {
				tmpMap.put("DireccionComprador", cliente.getDireccion());
				tmpMap.put("TelfComprador", cliente.getTelefono());
			} else {
				tmpMap.put("DireccionComprador", "");
				tmpMap.put("TelfComprador", "");
			}
			
			
			tmpMap.put("FechaEmision", ((InfoFactura)doc.getInfo()).getFechaEmision());
			tmpMap.put("GuiaRemision", ((InfoFactura)doc.getInfo()).getGuiaRemision());
			tmpMap.put("Subtotal", ((InfoFactura)doc.getInfo()).getTotalSinImpuestos().toString());
			tmpMap.put("Total",((InfoFactura)doc.getInfo()).getImporteTotal().toString());
			//impuestos : 
			for (TotalImpuesto impuesto : ((InfoFactura)doc.getInfo()).getTotalConImpuestos().getTotalImpuesto()) {
				if (impuesto.getCodigo().equals("2")) {
					 if (impuesto.getCodigoPorcentaje().equals("2")){
						tmpMap.put("IVA",impuesto.getValor().toString());
					 }
				 }	
				if (impuesto.getCodigo().equals("2") && impuesto.getCodigoPorcentaje().equals("0")) {
					tmpMap.put("IVA1",impuesto.getValor().toString());
				} 	else {
					tmpMap.put("IVA1", "0.00");
				}
			}	
			
			//infoAdicional
			com.cimait.invoicec.sri.schema.invoice.Factura.InfoAdicional infoAdicional = (com.cimait.invoicec.sri.schema.invoice.Factura.InfoAdicional)doc.getInfoAdicional();
			String strInfoAdicional = "";
			for (CampoAdicional campoAdicional : infoAdicional.getCampoAdicional()) {
				strInfoAdicional = strInfoAdicional + campoAdicional.getValue() + "\n";
			}
			tmpMap.put("InformacionAdicional", strInfoAdicional);
		} else if (doc.getInfoTributaria().getCodDoc().equals("04")) {
			tmpMap.put("ContribuyenteEspecial",((InfoNotaCredito)doc.getInfo()).getContribuyenteEspecial());
			tmpMap.put("Obligado", ((InfoNotaCredito)doc.getInfo()).getObligadoContabilidad());
			tmpMap.put("RUCcliente", ((InfoNotaCredito)doc.getInfo()).getIdentificacionComprador());
			tmpMap.put("RazonSocialComprador", ((InfoNotaCredito)doc.getInfo()).getRazonSocialComprador());
			
			cliente = getCustomer(doc.getRUC(),((InfoFactura)doc.getInfo()).getIdentificacionComprador());
			
			if (cliente != null ) {
				tmpMap.put("DireccionComprador", cliente.getDireccion());
				tmpMap.put("TelfComprador", cliente.getTelefono());
			} else {
				tmpMap.put("DireccionComprador", "");
				tmpMap.put("TelfComprador", "");
			}
			
			tmpMap.put("FechaEmision", ((InfoNotaCredito)doc.getInfo()).getFechaEmision());
			tmpMap.put("Subtotal", ((InfoNotaCredito)doc.getInfo()).getTotalSinImpuestos().toString());
			tmpMap.put("Total", ((InfoNotaCredito)doc.getInfo()).getValorModificacion().toString());
			
			//impuestos
			
			for (com.cimait.invoicec.sri.schema.creditnote.TotalConImpuestos.TotalImpuesto impuesto : ((InfoNotaCredito)doc.getInfo()).getTotalConImpuestos().getTotalImpuesto()) {
				if (impuesto.getCodigo().equals("2") && impuesto.getCodigoPorcentaje().equals("0")) {
					tmpMap.put("IVA1", impuesto.getValor().toString());
				} else {
					tmpMap.put("IVA1", "0.00");
				}
				if (impuesto.getCodigo().equals("2") && impuesto.getCodigoPorcentaje().equals("2")) {
					tmpMap.put("IVA", impuesto.getValor().toString());
				}
			}
			
			String strTipoDocumentoMod = "";
			String tipoDocumentoMod = ((InfoNotaCredito)doc.getInfo()).getCodDocModificado();
			if (tipoDocumentoMod.trim().equals("01")) {
				strTipoDocumentoMod = "FACTURA";
			} else if (tipoDocumentoMod.trim().equals("04")) {
				strTipoDocumentoMod = "NOTA DE CREDITO";
			} else if (tipoDocumentoMod.trim().equals("05")) {
				strTipoDocumentoMod = "NOTA DE DEBITO";
			} else if (tipoDocumentoMod.trim().equals("07")) {
				strTipoDocumentoMod = "COMPROBANTE DE RETENCIÓN";
			}
			tmpMap.put("TipoDocumentoMod", strTipoDocumentoMod);
			tmpMap.put("NroDocumentoMod", ((InfoNotaCredito)doc.getInfo()).getNumDocModificado());
			tmpMap.put("FechaEmisionDocMod", ((InfoNotaCredito)doc.getInfo()).getFechaEmisionDocSustento());
			tmpMap.put("Motivo", ((InfoNotaCredito)doc.getInfo()).getMotivo());

			//infoAdicional
			com.cimait.invoicec.sri.schema.creditnote.NotaCredito.InfoAdicional infoAdicional = (com.cimait.invoicec.sri.schema.creditnote.NotaCredito.InfoAdicional)doc.getInfoAdicional();
			String strInfoAdicional = "";
			for (com.cimait.invoicec.sri.schema.creditnote.NotaCredito.InfoAdicional.CampoAdicional campoAdicional : infoAdicional.getCampoAdicional()) {
				strInfoAdicional = strInfoAdicional + campoAdicional.getValue() + "\n";
			}
			tmpMap.put("InformacionAdicional", strInfoAdicional);
			
		}else if (doc.getInfoTributaria().getCodDoc().equals("05")) {
			tmpMap.put("ContribuyenteEspecial",((InfoNotaDebito)doc.getInfo()).getContribuyenteEspecial());
			tmpMap.put("Obligado", ((InfoNotaDebito)doc.getInfo()).getObligadoContabilidad());
			tmpMap.put("RUCcliente", ((InfoNotaDebito)doc.getInfo()).getIdentificacionComprador());
			tmpMap.put("RazonSocialComprador", ((InfoNotaDebito)doc.getInfo()).getRazonSocialComprador());
			
			cliente = getCustomer(doc.getRUC(),((InfoFactura)doc.getInfo()).getIdentificacionComprador());
			
			if (cliente != null ) {
				tmpMap.put("DireccionComprador", cliente.getDireccion());
				tmpMap.put("TelfComprador", cliente.getTelefono());
			} else {
				tmpMap.put("DireccionComprador","");
				tmpMap.put("TelfComprador", "");
			}
			tmpMap.put("FechaEmision", ((InfoNotaDebito)doc.getInfo()).getFechaEmision());
			tmpMap.put("Subtotal", ((InfoNotaDebito)doc.getInfo()).getTotalSinImpuestos().toString());
			tmpMap.put("Total",((InfoNotaDebito)doc.getInfo()).getValorTotal().toString());
			
			String strTipoDocumentoMod = "";
			String tipoDocumentoMod = ((InfoNotaDebito)doc.getInfo()).getCodDocModificado();
			if (tipoDocumentoMod.trim().equals("01")) {
				strTipoDocumentoMod = "FACTURA";
			} else if (tipoDocumentoMod.trim().equals("04")) {
				strTipoDocumentoMod = "NOTA DE CREDITO";
			} else if (tipoDocumentoMod.trim().equals("05")) {
				strTipoDocumentoMod = "NOTA DE DEBITO";
			} else if (tipoDocumentoMod.trim().equals("07")) {
				strTipoDocumentoMod = "COMPROBANTE DE RETENCIÓN";
			}
			
			tmpMap.put("TipoDocumentoMod", strTipoDocumentoMod);
			tmpMap.put("NroDocumentoMod", ((InfoNotaDebito)doc.getInfo()).getNumDocModificado());
			tmpMap.put("FechaEmisionDocMod", ((InfoNotaDebito)doc.getInfo()).getFechaEmisionDocSustento());
			//impuestos
			for (Impuesto impuesto : ((InfoNotaDebito)doc.getInfo()).getImpuestos().getImpuesto()) {
				if (impuesto.getCodigo().equals("2") && impuesto.getCodigoPorcentaje().equals("0")) {
					tmpMap.put("IVA1", impuesto.getValor().toString());
				} else {
					tmpMap.put("IVA1", "0.00");
				}
				if (impuesto.getCodigo().equals("2") && impuesto.getCodigoPorcentaje().equals("2")) {
					tmpMap.put("IVA", impuesto.getValor().toString());
				}
			}
			//infoAdicional
			com.cimait.invoicec.sri.schema.debitnote.NotaDebito.InfoAdicional infoAdicional = (com.cimait.invoicec.sri.schema.debitnote.NotaDebito.InfoAdicional)doc.getInfoAdicional();
			String strInfoAdicional = "";
			for (com.cimait.invoicec.sri.schema.debitnote.NotaDebito.InfoAdicional.CampoAdicional campoAdicional : infoAdicional.getCampoAdicional()) {
				strInfoAdicional = strInfoAdicional + campoAdicional.getValue() + "\n";
			}
			tmpMap.put("InformacionAdicional", strInfoAdicional);
		}else if	(doc.getInfoTributaria().getCodDoc().equals("07")) {
			tmpMap.put("ContribuyenteEspecial",((InfoCompRetencion)doc.getInfo()).getContribuyenteEspecial());
			tmpMap.put("Obligado", ((InfoCompRetencion)doc.getInfo()).getObligadoContabilidad());
			tmpMap.put("RUCcliente", ((InfoCompRetencion)doc.getInfo()).getIdentificacionSujetoRetenido());
			tmpMap.put("RazonSocialComprador", ((InfoCompRetencion)doc.getInfo()).getRazonSocialSujetoRetenido());
			
			cliente = getCustomer(doc.getRUC(),((InfoCompRetencion)doc.getInfo()).getIdentificacionSujetoRetenido());
			
			if (cliente != null ) {
				tmpMap.put("DireccionComprador", cliente.getDireccion());
				tmpMap.put("TelfComprador", cliente.getTelefono());
			} else {
				tmpMap.put("DireccionComprador", "");
				tmpMap.put("TelfComprador", "");
			}
			tmpMap.put("FechaEmision", ((InfoCompRetencion)doc.getInfo()).getFechaEmision());
			tmpMap.put("PerFiscal", ((InfoCompRetencion)doc.getInfo()).getPeriodoFiscal());
			
			//infoAdicional
			com.cimait.invoicec.sri.schema.retention.ComprobanteRetencion.InfoAdicional infoAdicional = (com.cimait.invoicec.sri.schema.retention.ComprobanteRetencion.InfoAdicional)doc.getInfoAdicional();
			String strInfoAdicional = "";
			if (infoAdicional != null ) {
			for (com.cimait.invoicec.sri.schema.retention.ComprobanteRetencion.InfoAdicional.CampoAdicional campoAdicional : infoAdicional.getCampoAdicional()) {
				strInfoAdicional = strInfoAdicional + campoAdicional.getValue() + "\n";
			}
			tmpMap.put("InformacionAdicional", strInfoAdicional);
			}
		}	
		
		return tmpMap;
	}

	public synchronized static int enviaEmailCliente(String ls_id_mensaje,
			String fecEmi, String mensaje_mail, String mensaje_error,
			String fileAttachXml, String fileAttachPdf, String emailCliente,
			String documentNumber) {
		try {
			String host = Environment.c
					.getString("facElectronica.alarm.email.host");
			String helpdesk = Environment.c
					.getString("facElectronica.alarm.email.helpdesk");
			emailHost = host;
			emailFrom = Environment.c
					.getString("facElectronica.alarm.email.sender");
			EmailSender emSend = new EmailSender(emailHost, emailFrom);
			emailHelpDesk = helpdesk;
			emailMensaje = Environment.c
					.getString("facElectronica.alarm.email." + ls_id_mensaje);
			String ambiente = Environment.c
					.getString("facElectronica.alarm.email.ambiente");
			String clave = Environment.c
					.getString("facElectronica.alarm.email.password");

			String user = Environment.c
					.getString("facElectronica.alarm.email.user");
			String subject = Environment.c
					.getString("facElectronica.alarm.email.subject");
			String tipo_autentificacion = Environment.c
					.getString("facElectronica.alarm.email.tipo_autentificacion");
			String tipoMail = Environment.c
					.getString("facElectronica.alarm.email.tipoMail");
			System.out.println("correo : parametros de ambiente llenados");
			String receivers = "";
			receivers = emailCliente;
			if (receivers != null) {
				emSend.setPassword(clave);
				emSend.setSubject(subject);
				emSend.setUser(user);
				emSend.setAutentificacion(tipo_autentificacion);
				emSend.setTipoMail(tipoMail);
				String noDocumento = "";
				emailMensaje = emailMensaje.replace("|FECHA|", (fecEmi == null ? "" : fecEmi));
				emailMensaje = emailMensaje.replace("|NODOCUMENTO|",
						(documentNumber == null ? "" : documentNumber));
				emailMensaje = emailMensaje
						.replace("|HELPDESK|", emailHelpDesk);
				emailMensaje = StringEscapeUtils.unescapeHtml(emailMensaje);
				if (ls_id_mensaje.equals("message_error")) {
					emailMensaje = emailMensaje.replace("|CabError|",
							"Hubo inconvenientes con");
					emailMensaje = emailMensaje.replace("|Mensaje|",
							mensaje_error);
				}
				if (ls_id_mensaje.equals("message_exito")) {
					emailMensaje = emailMensaje.replace("|CabMensaje|", " ");
				}
				System.out.println("Correo : configuracion de destinatarios");
				System.out.println("Enviando correo a : " + emailCliente);
				if ((emailCliente != null) && (emailCliente.length() > 0)) {
					String[] partsMail = emailCliente.split(";");
					// for(int i=0;i<partsMail.length;i++)
					// if (partsMail[i].length()>0){
					emSend.send(
							emailCliente
							// partsMail[i]
							, subject, emailMensaje, fileAttachXml,
							fileAttachPdf);
					// }
				}
				System.out.println("Fin envio correo.");
			} else {
				System.out.println("lista de correos a enviar vacia");
			}
		} catch (Exception e) {
			System.out.println("Error al enviar correo ... ");
			e.printStackTrace();
		}
		return 0;
	}
	
}
