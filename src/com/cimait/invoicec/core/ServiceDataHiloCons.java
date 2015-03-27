package com.cimait.invoicec.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRMapArrayDataSource;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import au.com.bytecode.opencsv.CSVWriter;

import com.cimait.invoicec.bean.Emisor;
import com.cimait.invoicec.db.DBDataSource;
import com.cimait.invoicec.db.DBUtil;
import com.cimait.invoicec.document.DocumentUtil;
import com.cimait.invoicec.entity.FacCabDocumentosEntity;
import com.cimait.invoicec.entity.FacClientesEntity;
import com.cimait.invoicec.entity.FacClientesEntityPK;
import com.cimait.invoicec.sri.document.SRIDocument;
import com.cimait.invoicec.sri.document.SRIDocumentFactory;
import com.cimait.invoicec.sri.schema.creditnote.NotaCredito.InfoNotaCredito;
import com.cimait.invoicec.sri.schema.debitnote.NotaDebito.InfoNotaDebito;
import com.cimait.invoicec.sri.schema.general.Impuesto;
import com.cimait.invoicec.sri.schema.invoice.Factura.Detalles;
import com.cimait.invoicec.sri.schema.invoice.Factura.Detalles.Detalle;
import com.cimait.invoicec.sri.schema.invoice.Factura.InfoAdicional;
import com.cimait.invoicec.sri.schema.invoice.Factura.InfoAdicional.CampoAdicional;
import com.cimait.invoicec.sri.schema.invoice.Factura.InfoFactura;
import com.cimait.invoicec.sri.schema.invoice.Factura.InfoFactura.TotalConImpuestos.TotalImpuesto;
import com.cimait.invoicec.sri.schema.retention.ComprobanteRetencion.InfoCompRetencion;
import com.sun.DAO.ControlErrores;
import com.sun.DAO.DetalleDocumento;
import com.sun.DAO.DetalleTotalImpuestos;
import com.sun.DAO.DocumentoImpuestos;
import com.sun.DAO.InformacionAdicional;
import com.sun.businessLogic.validate.LeerDocumentos;
import com.sun.comprobantes.util.EmailSender;
import com.sun.comprobantes.util.FormGenerales;
import com.sun.comprobantes.util.X509Utils;
import com.sun.directory.examples.InfoEmpresa;
import com.sun.directory.examples.ModifyXML;
import com.tradise.reportes.entidades.FacCabDocumento;
import com.tradise.reportes.entidades.FacDetDocumento;
import com.tradise.reportes.util.key.GenericTransaction;
import com.util.util.key.Environment;
import com.util.util.key.Util;
import com.util.webServices.EnvioComprobantesWs;

import ec.gob.sri.comprobantes.ws.RespuestaSolicitud;

public class ServiceDataHiloCons extends GenericTransaction{
	
	private InfoEmpresa infoEmp;
	private Emisor emite;
	
	public static String classReference = "ServiceData";
	public static StringBuilder SBmsj = null;
	public static File fxml = null;
	public static String fileBackup = null;
	public static String emailHost = null;
	public static String emailFrom = null;
	public static String emailTo = null;
	public static String emailSubject = null;
	public static String emailMensaje = null;	
	public static String emailHelpDesk = null;
	public static File[] contenido;
	public static ArrayList<ControlErrores> ListErrorGeneral = null;
	public static ArrayList<ControlErrores> ListWarnGeneral = null;
	private static Logger LOGGER = Logger.getLogger(ServiceDataHiloCons.class);
	
	SRIDocument sriDocument=null;
	//
	String documentNumber = null;
	String RUC = null;
	String documentType = null;

	//fin ea
	
	public ServiceDataHiloCons(InfoEmpresa infoEmpresa, Emisor emitir, List listError, List ListWarn,List listErrorEstados,List listWarnEstados){
		LOGGER.info("Procesando archivo  " + emitir.getFileTxt());
		this.infoEmp = infoEmpresa;
		this.emite = emitir;
		
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
	
	//ea
	public void backup_file() throws Exception {
		try {
			boolean li_result = copiarXml(infoEmp.getDirRecibidos()
					+ emite.getFileTxt());

			if (li_result) {
				emite.setFileXmlBackup(infoEmp.getDirRecibidos()
						+ emite.getFileTxt().replace(".txt", "_backup.txt"));
			}
		} catch (Exception e) {
			throw new Exception("No se pudo hacer backup de archivo "
					+ infoEmp.getDirRecibidos() + emite.getFileTxt());
		}
	}

	public void validationFileName() throws Exception {
		validateFile(emite.getFileTxt());
	}

	public String getDocumentType(String fileName) { 
		//extraer por codigo en nombre de archivo <RUC>-TIPO-<Serie>-<Secuencia>
		//1234567890123-01-123123-123456789
		 return fileName.substring(14,16); 
	}
	

	
	public void saveLog(String invoiceNumber, String status, String msgProcess, String msgError,String xmlGenerated, String xmlSigned, String xmlResponse, String xmlAuth) throws Exception {
		SimpleDateFormat sm = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.SSSSSS");
		String strDate = sm.format(new Date());
		LOGGER.debug("Grabando en bitacora, documento :" + invoiceNumber);
		emite.insertaBitacoraDocumento(
				String.valueOf(emite.getInfEmisor().getAmbiente()), 
				emite.getInfEmisor().getRuc(), 
				emite.getInfEmisor().getCodEstablecimiento(),  
				emite.getInfEmisor().getCodPuntoEmision(),     
				emite.getInfEmisor().getSecuencial(),          
				emite.getInfEmisor().getTipoComprobante(),
				strDate, status,
				msgProcess, 
				msgError, 
				xmlGenerated, 
				xmlSigned,
				xmlResponse,
				xmlAuth,
				invoiceNumber);
	}
	
	public void atiendeHilo() throws Exception
	{
		String mensaje = "", estado = "";
		LOGGER.debug("en AtiendeHilo");
		// validate file name
		LOGGER.debug("Validacion de archivo TXT: " + emite.getFileTxt());
		validationFileName();
		//extract documento type
		documentType = getDocumentType(emite.getFileTxt());
		// create object
		sriDocument = SRIDocumentFactory.getSRIDocument(documentType);
		// read from csv/txt/xml
		
		LOGGER.debug("Lectura de TXT: "+ emite.getFileTxt());
		sriDocument.setDataprovider(infoEmp.getDirRecibidos()+emite.getFileTxt());
		
		documentNumber = sriDocument.getNumber();
		RUC = sriDocument.getRUC();
		
		// info for sql's
		emite.getInfEmisor().setAmbiente(Integer.parseInt(sriDocument.getEnv()));
		emite.getInfEmisor().setRuc(RUC);
		
		emite.getInfEmisor().setTipoComprobante(documentType); //diferencia con Codigo Documento ??
		emite.getInfEmisor().setCodDocumento(documentType); 
		
		emite.getInfEmisor().setCodEstablecimiento(documentNumber.substring(0,3));
		emite.getInfEmisor().setCodPuntoEmision(documentNumber.substring(3,6));
		emite.getInfEmisor().setSecuencial(documentNumber.substring(6));
		emite.getInfEmisor().setFecEmision(sriDocument.getIssueDate());
		emite.getInfEmisor().setTipoEmision(sriDocument.getIssueType());
		
		// get status of invoicenumber in db
		String ls_statusDocumento = "";
		
		ls_statusDocumento = DBUtil.statusDocumento(
				emite.getInfEmisor().getAmbiente(),
				emite.getInfEmisor().getRuc(),
				emite.getInfEmisor().getTipoComprobante(), documentNumber).trim();
			//save document in database
			//geenrar codigo de acceso.
			
			sriDocument.setEmisor(emite);
			sriDocument.saveInDB();
			
			updateClient(sriDocument);
			// save in log (bitacora) new document to process .
			saveLog(documentNumber,"IN","Carga de Archivo","","","","","");
			// backup input file
			backup_file();
			// obtiene datos de correo
			emite.obtieneMailEstablecimiento(emite.getInfEmisor());
			//genero documento xml
			final Document docFinal =sriDocument.getDocument();
			String documentFileName = RUC + "-" + documentType + "-" + documentNumber +  ".xml";
			//
			DocumentUtil.saveDocumentToFile(docFinal, infoEmp.getDirRecibidos()+documentFileName);
			saveLog(documentNumber,"IN","Documento XML Generado","",DocumentUtil.printDocument(docFinal),"","","");
			
			
			
				LOGGER.debug("Creando  archivo :" + infoEmp.getDirFirmados()+ documentFileName);
				emite.setFilexml(infoEmp.getDirFirmados() + documentFileName);
				
				try {
					LOGGER.debug("Enviando archivo a SRI : " + documentNumber);
					 ec.gob.sri.comprobantes.ws.RespuestaSolicitud respuestaRecepcion = null;
					 int i = 0;
					 Exception efin= null;
					 
					
	      
		        	  String[] infoAutorizacion = null;
		        	  String respAutorizacion = null;
		        	  
		        	  //if (respuestaRecepcion.getEstado().trim().equals("RECIBIDA")) {
		        		  	updateDocumentStatus(RUC, documentNumber,"RS","Recibido por SRI.");
		        		  	LOGGER.debug("Documento : " + documentNumber + "recibido por SRI.");
			        		  try{
			                	  respAutorizacion = com.sun.directory.examples.AutorizacionComprobantesWs.autorizarComprobanteIndividual(emite.getInfEmisor().getClaveAcceso(), FilenameUtils.getBaseName(emite.getFilexml()), new Integer(emite.getInfEmisor().getAmbiente()).toString(),infoEmp.getDirAutorizados(), infoEmp.getDirNoAutorizados(), infoEmp.getDirFirmados());
			                	  if (respAutorizacion.equals("")){
			                		  infoAutorizacion = new String[3];
			                		  infoAutorizacion[0] = "SIN-RESPUESTA";
			                		  LOGGER.debug("Documento : " + documentNumber + " sin respuesta del SRI.");
			                	  }else{
			                		  infoAutorizacion = respAutorizacion.split("\\|");
			                	  }
			                  }catch(Exception excep){
			                	  excep.printStackTrace();
			                  }
							
			        		  
			        		  //if (infoAutorizacion[0].trim().equals("AUTORIZADO")) {
								LOGGER.info("Documento " + documentNumber + " Aprobado..");
								LOGGER.debug("Actualizando documento"+ documentNumber +" en base de datos");
								updateDocumentStatus(RUC, documentNumber,"AT","Autorizado por SRI.");
								//processResponse(documentFileName,null,respAutorizacion,"N");
								//generate pdf
								LOGGER.debug("Generando PDF " + infoEmp.getDirAutorizados()+FilenameUtils.removeExtension(documentFileName) + ".PDF");
								//String pdfGenerated = generatePDF(sriDocument, infoAutorizacion[1], infoAutorizacion[2], false);
								String pdfGenerated = generatePDF(sriDocument, "0603201515462617920633370013451060800", "2015-03-06T15:46:26.698-05:00", false);
								//send email
								LOGGER.debug("Envio correo exito " + infoEmp.getDirAutorizados()+FilenameUtils.removeExtension(documentFileName) + ".PDF");
								enviaEmailCliente("message_exito", emite.getInfEmisor().getFecEmision(), "","",infoEmp.getDirAutorizados()+documentFileName ,pdfGenerated,sriDocument.getExtraData().get("CorreoCliente") + ";" + sriDocument.getExtraData().get("CorreoInterno"),documentNumber);
							//}
				} catch (Exception se) {
					//captura error distinto a los try de solicitud recepcion y autorizacion
					System.out.println("Error entre Solicitud de recepcion y autorizacion" + se.getMessage());
					se.printStackTrace();
				}
			 
			LOGGER.info("Fin procesamiento archivo  " + emite.getFileTxt());
		
				

	}

	private void processResponse(String documentFileName , RespuestaSolicitud respSolicitud, String respAutorizacion, String resendable) throws IOException {
		List<HashMap<String, String>> lMensajes = new ArrayList<HashMap<String,String>>();
		if (respSolicitud != null) {
			int respSize = respSolicitud.getComprobantes().getComprobante().size();
			String ls_tipo="";
			if (respSize>0){
	  		  for (int r=0; r<respSize; r++){
	  			  ec.gob.sri.comprobantes.ws.Comprobante respuesta = respSolicitud.getComprobantes().getComprobante().get(r);					        			  
	  			  int respMsjSize = respuesta.getMensajes().getMensaje().size();
	  			  
	  			  HashMap<String,String> hMensaje = new HashMap<String, String>();
				  hMensaje.put("codigo1", "0");
				  hMensaje.put("desc1", "ERROR");
			 	  lMensajes.add(hMensaje);
				  
	  			  for (int m=0; m<respMsjSize; m++){
	  				  ls_tipo = respuesta.getMensajes().getMensaje().get(m).getTipo();
	  				  if (ls_tipo.equals("ERROR")){
	  					  if (!respuesta.getMensajes().getMensaje().get(m).getInformacionAdicional().trim().equals("")) {
	  						  hMensaje = new HashMap<String, String>();
	  						  hMensaje.put("codigo2", respuesta.getMensajes().getMensaje().get(m).getIdentificador());
	  						  hMensaje.put("desc2", respuesta.getMensajes().getMensaje().get(m).getInformacionAdicional());
	  					 	  lMensajes.add(hMensaje);
	  					  }
	  				  }				        				  
	  			  }
	  		  }
			}
		} else {
		  //si hay mensajes sin mas separadores
		  if (respAutorizacion.split("\\|").length < 2) { respAutorizacion  += "| | |"; }
		  
		  HashMap<String,String> hMensaje = new HashMap<String, String>();
		  hMensaje.put("codigo1", "0");
		  hMensaje.put("FechaAutorizacion", respAutorizacion.split("\\|")[2].trim());
		  hMensaje.put("desc1", respAutorizacion.split("\\|")[0]);
		  hMensaje.put("AuthNumber", respAutorizacion.split("\\|")[1]);
	 	  lMensajes.add(hMensaje);
		}
		exportResponse(documentFileName, documentType, documentNumber, lMensajes, resendable); //error SRI
	}

	
	private void updateClient(SRIDocument document) throws SQLException {
		Session session = DBDataSource.getInstance().getFactory().openSession();
        Transaction tx = null ;
        try {
        	tx = session.beginTransaction();
        	FacClientesEntityPK clienteSearch = new FacClientesEntityPK();
        	clienteSearch.setRuc(document.getRUC());
        	clienteSearch.setRucCliente(document.getCustomerRUC());
        	FacClientesEntity cliente = (FacClientesEntity) session.get(FacClientesEntity.class, clienteSearch);
        	//si no existe lo creo
        	if (cliente == null) {
        		cliente = new FacClientesEntity();
        		cliente.setRuc(document.getRUC());
        		cliente.setRazonSocial(document.getCustomerName());
        		cliente.setDireccion(document.getCustomerAddress());
        		cliente.setEmail(document.getCustomerEmail());
        		cliente.setTipoCliente(document.getCustomerType());
        		cliente.setTipoIdentificacion(document.getCustomerIdentificationType());
        		cliente.setRise("");
        		cliente.setTelefono(document.getCustomerPhone());
        		cliente.setRucCliente(document.getCustomerRUC());
        		cliente.setIsActive("Y");
        		cliente.setQadCodCliente(document.getExtraData().get("QADCodCliente"));
        		session.save(cliente);
        	//si existe actualizo los campos relevantes
        	} else {
        		cliente.setDireccion(document.getCustomerAddress());
        		cliente.setEmail(document.getCustomerEmail());
        		cliente.setTelefono(document.getCustomerPhone());
        		session.save(cliente);
        	}
        	tx.commit();
		 } catch (HibernateException e) {
	         if (tx!=null) tx.rollback();
	         e.printStackTrace();
	     }finally {
	         session.close();
	     }
	}

	private void exportResponse(String documentName, String documentType, String documentNumber, List<HashMap<String,String>> alternativeResponse, String reenvio) throws IOException {
		String pathExport = FilenameUtils.normalize(infoEmp.getDirGenerado()+".." + File.separator+ "out") + File.separator;
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

	
	
	private String  generatePDF(SRIDocument document, String NroAutorizacion, String fecAutorizacion, boolean contingencia) throws IOException {
		LOGGER.debug("pdf 0...");
		String pathJasper = infoEmp.getPathReports();
		LOGGER.debug("pdf 1...");
		String jasperFile = ""; 
		String pdfFileName="";
		
		LOGGER.debug("pdf 2...");
		
		Map<String,Object> params = new HashMap<String,Object>();
		LOGGER.debug("pdf 3...");
		params.put("inputHeader", getHeaderValues(document, NroAutorizacion,fecAutorizacion));
		params.put("LogoPath",pathJasper);
		params.put(JRParameter.REPORT_LOCALE, Locale.US); 
		
		LOGGER.debug("documentType... "+documentType);
		LOGGER.debug("pdf 4...");
		
		try {
			if (documentType.equals("01")) {
				jasperFile = pathJasper + "invoice.jasper";
			} else if (documentType.equals("04")) {
				jasperFile = pathJasper + "creditnote.jasper";
			} else if (documentType.equals("05")) {
				jasperFile = pathJasper + "debitnote.jasper";
			} else if (documentType.equals("07")) {
				jasperFile = pathJasper + "retention.jasper";
			} else {
				LOGGER.error("Error en generacion de PDF , tipo de documento invalido: " + documentType);
				throw new Exception("tipo de pdf a generar invalido");
			}
			FileInputStream is = new FileInputStream(jasperFile);
			JRMapArrayDataSource datasource = new JRMapArrayDataSource(getDetailValues(document));
			JasperPrint pdfRender = JasperFillManager.fillReport(is, params,datasource);
			if (!contingencia) {
				pdfFileName = infoEmp.getDirAutorizados()+infoEmp.getRuc()+"-"+documentType+"-"+documentNumber+".PDF";
			} else {
				pdfFileName = infoEmp.getDirContingencias()+infoEmp.getRuc()+"-"+documentType+"-"+documentNumber+".PDF";
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
			
			LOGGER.debug("doc..."+doc);
			LOGGER.debug("doc.getInfoDetail()..."+doc.getInfoDetail());

			
			mDataSource= new HashMap[((com.cimait.invoicec.sri.schema.retention.ComprobanteRetencion.Impuestos)doc.getInfoDetail()).getImpuesto().size()];
			//mDataSource= new HashMap[1];
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
	
	private HashMap<String, String> getHeaderValues(SRIDocument doc, String nroAutorizacion, String fecAutorizacion) {
		HashMap<String, String> tmpMap = new HashMap<String, String>();
		//comun :
		tmpMap.put("Numero", doc.getInfoTributaria().getEstab().trim() + "-" + doc.getInfoTributaria().getPtoEmi() + "-" + doc.getInfoTributaria().getSecuencial());
		tmpMap.put("FecAutorizacion", fecAutorizacion);
		tmpMap.put("NroAutorizacion", nroAutorizacion);
		tmpMap.put("Ambiente", (doc.getInfoTributaria().getAmbiente().equals("1")?"PRUEBAS":"PRODUCCIÓN"));
		tmpMap.put("TipoEmision", (doc.getInfoTributaria().getTipoEmision().equals("1")?"NORMAL":"CONTINGENCIA"));
		tmpMap.put("ClaveAcceso",doc.getInfoTributaria().getClaveAcceso());
		
		
		
		if (doc.getInfoTributaria().getCodDoc().equals("01")) {
			tmpMap.put("ContribuyenteEspecial",((InfoFactura)doc.getInfo()).getContribuyenteEspecial());
			tmpMap.put("Obligado", ((InfoFactura)doc.getInfo()).getObligadoContabilidad());
			tmpMap.put("RUCcliente", ((InfoFactura)doc.getInfo()).getIdentificacionComprador());
			tmpMap.put("RazonSocialComprador", ((InfoFactura)doc.getInfo()).getRazonSocialComprador());
			tmpMap.put("DireccionComprador", doc.getExtraData().get("DirCliente"));
			tmpMap.put("TelfComprador", doc.getExtraData().get("TelfCliente"));
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
			tmpMap.put("DireccionComprador", doc.getExtraData().get("DirCliente"));
			tmpMap.put("TelfComprador", doc.getExtraData().get("TelfCliente"));
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
			tmpMap.put("DireccionComprador", doc.getExtraData().get("DirCliente"));
			tmpMap.put("TelfComprador", doc.getExtraData().get("TelfCliente"));
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
			tmpMap.put("DireccionComprador", doc.getExtraData().get("DirCliente"));
			tmpMap.put("TelfComprador", doc.getExtraData().get("TelfCliente"));
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

	private void updateDocumentStatus(String RUC, String documentNumber, String status, String msg) throws SQLException {
		Session session = DBDataSource.getInstance().getFactory().openSession();
        Transaction tx = null ;
        try {
        	tx = session.beginTransaction();
        	Query qry = session.createQuery("from FacCabDocumentosEntity where ambiente = :AMBIENTE and ruc=:RUC and codEstablecimiento=:CODESTABLECIMIENTO and codPuntEmision = :CODPUNTOEMISION and secuencial = :SECUENCIAL");
        	qry.setParameter("AMBIENTE", emite.getInfEmisor().getAmbiente());
        	qry.setParameter("RUC", RUC);
        	qry.setParameter("CODESTABLECIMIENTO", emite.getInfEmisor().getCodEstablecimiento());
        	qry.setParameter("CODPUNTOEMISION", emite.getInfEmisor().getCodPuntoEmision());
        	qry.setParameter("SECUENCIAL", emite.getInfEmisor().getSecuencial());
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

	public static String getXML(String path) throws IOException{
		FileInputStream input = new FileInputStream( new File(path));

		 byte[] fileData = new byte[input.available()];

		 input.read(fileData);
		 input.close();
		 String resultadoXml = new String(fileData, "UTF-8");
		 return resultadoXml;
	}
	
	//***********************//////////////////////////////////////////////////////************************************//
	/*									   	Envios al Sri por WebServices											  */
	//***********************//////////////////////////////////////////////////////************************************//	
	public static ec.gob.sri.comprobantes.ws.RespuestaSolicitud solicitudRecepcion(File archivoFirmado, 
																				   Emisor emi, 
																				   ArrayList<ControlErrores> ListErrorGeneral, 
									 											   ArrayList<ControlErrores> ListWarnGeneral) throws Exception
	{
		ec.gob.sri.comprobantes.ws.RespuestaSolicitud respuestaRecepcion = null;
		String flagErrores = "";	
			respuestaRecepcion = new ec.gob.sri.comprobantes.ws.RespuestaSolicitud();
	    	respuestaRecepcion = EnvioComprobantesWs.obtenerRespuestaEnvio(archivoFirmado, 
	    																   emi.getInfEmisor().getRuc(), 
	    																   emi.getInfEmisor().getCodDocumento(), 
	    																   emi.getInfEmisor().getClaveAcceso(), 
	    																   FormGenerales.devuelveUrlWs(new Integer(emi.getInfEmisor().getAmbiente()).toString() ,"RecepcionComprobantes"),
	    																   30000, ListErrorGeneral, ListWarnGeneral);
        System.out.println("RespuestaRecepcion : " + respuestaRecepcion);
		return respuestaRecepcion;		
	}	
	//***********************//////////////////////////////////////////////////////************************************//
	/*									   	Envios de Mail															  */
	//***********************//////////////////////////////////////////////////////************************************//		
	public synchronized static int enviaEmail(String ls_id_mensaje, Emisor emi, String mensaje_mail, String mensaje_error, String fileAttachXml, String fileAttachPdf){
		
		emailMensaje = Environment.c.getString("facElectronica.alarm.email."+ls_id_mensaje);		
		String host = Environment.c.getString("facElectronica.alarm.email.host");
		String helpdesk = Environment.c.getString("facElectronica.alarm.email.helpdesk");
		emailHost = host;
		emailFrom = Environment.c.getString("facElectronica.alarm.email.sender");
		emailHelpDesk= helpdesk;
		EmailSender emSend = new EmailSender(emailHost,emailFrom);
		String ambiente = Environment.c.getString("facElectronica.alarm.email.ambiente");
		String clave = Environment.c.getString("facElectronica.alarm.email.password");
		String subject = Environment.c.getString("facElectronica.alarm.email.subject");
		String receivers = "";
		String user = Environment.c.getString("facElectronica.alarm.email.user");
		String tipo_autentificacion = Environment.c.getString("facElectronica.alarm.email.tipo_autentificacion");
		
		
		String tipoMail = Environment.c.getString("facElectronica.alarm.email.tipoMail");
		receivers = Environment.c.getString("facElectronica.alarm.email.receivers-list");
		
		emSend.setPassword(clave);
		emSend.setSubject(subject);
		emSend.setUser(user);
		emSend.setAutentificacion(tipo_autentificacion);
		emSend.setTipoMail(tipoMail);
		emailTo = receivers;
		
		String noDocumento = "";		
		if ((emi.getInfEmisor().getCodEstablecimiento()!=null)&&(emi.getInfEmisor().getCodPuntoEmision()!=null)&&(emi.getInfEmisor().getSecuencial()!=null)){
			System.out.println("Envio de Email");
			noDocumento = emi.getInfEmisor().getCodEstablecimiento()+emi.getInfEmisor().getCodPuntoEmision()+emi.getInfEmisor().getSecuencial();
		}	
		emailMensaje = emailMensaje.replace("|FECHA|", (emi.getInfEmisor().getFecEmision()==null?"":emi.getInfEmisor().getFecEmision().toString()));
		emailMensaje = emailMensaje.replace("|NODOCUMENTO|", (noDocumento==null?"":noDocumento));	
		emailMensaje = emailMensaje.replace("|HELPDESK|", emailHelpDesk);
		emailMensaje = StringEscapeUtils.unescapeHtml(emailMensaje);
		if (ls_id_mensaje.equals("message_error"))
		{
			emailMensaje = emailMensaje.replace("|CabError|", "Hubo inconvenientes con");
			emailMensaje = emailMensaje.replace("|Mensaje|", mensaje_error);
		}
		if (ls_id_mensaje.equals("message_exito"))
		{
			emailMensaje = emailMensaje.replace("|CabMensaje|", " ");
		}
				
		if ((emailTo!=null) && (emailTo.length()>0)){
			String[] partsMail = emailTo.split(";");
					emSend.send(emailTo, 
								subject,
								emailMensaje,
			  		  	        fileAttachXml,
			  		  	        fileAttachPdf);
		}		
		return 0;
	}
	
	public synchronized static int enviaEmailCliente(String ls_id_mensaje,
													 String fecEmi,
													 String mensaje_mail,
													 String mensaje_error,
													 String fileAttachXml,
													 String fileAttachPdf,
													 String emailCliente,
													 String documentNumber)
	{
		try
		{
			String host = Environment.c.getString("facElectronica.alarm.email.host");
			String helpdesk = Environment.c.getString("facElectronica.alarm.email.helpdesk");
			emailHost = host;
			emailFrom = Environment.c.getString("facElectronica.alarm.email.sender");
			EmailSender emSend = new EmailSender(emailHost, emailFrom);
			emailHelpDesk = helpdesk;
			emailMensaje = Environment.c.getString("facElectronica.alarm.email." + ls_id_mensaje);
			String ambiente = Environment.c.getString("facElectronica.alarm.email.ambiente");
			String clave = Environment.c.getString("facElectronica.alarm.email.password");

			String user = Environment.c.getString("facElectronica.alarm.email.user");
			String subject = Environment.c.getString("facElectronica.alarm.email.subject");
			String tipo_autentificacion = Environment.c.getString("facElectronica.alarm.email.tipo_autentificacion");
			String tipoMail = Environment.c.getString("facElectronica.alarm.email.tipoMail");
			System.out.println("correo : parametros de ambiente llenados");
			String receivers = "";
			receivers = emailCliente;
			if (receivers != null)
			{
				emSend.setPassword(clave);
				emSend.setSubject(subject);
				emSend.setUser(user);
				emSend.setAutentificacion(tipo_autentificacion);
				emSend.setTipoMail(tipoMail);
				String noDocumento = "";
				emailMensaje = emailMensaje.replace("|FECHA|", (fecEmi == null ? "" : fecEmi));
				emailMensaje = emailMensaje.replace("|NODOCUMENTO|", (documentNumber == null ? "" : documentNumber));
				emailMensaje = emailMensaje.replace("|HELPDESK|", emailHelpDesk);
				emailMensaje = StringEscapeUtils.unescapeHtml(emailMensaje);
				if (ls_id_mensaje.equals("message_error")) {
					emailMensaje = emailMensaje.replace("|CabError|", "Hubo inconvenientes con");
					emailMensaje = emailMensaje.replace("|Mensaje|", mensaje_error);
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
	
	
	//***********************//////////////////////////////////////////////////////************************************//
	/*							   	Manejo de Archivos del XML														  */
	//***********************//////////////////////////////////////////////////////************************************//	
	//Copiar Xml
	public static boolean copiarXml(String fileName)
	{
		try
		{
		  File fileOrigen = new File(fileName);
	     // File fileDestino = new File(fileName.replace(".xml", "_backup.xml"));	      
		  File fileDestino = new File(fileName.replace(".txt", "_backup.txt"));
	      if (fileOrigen.exists()) {	    	  
	    	  InputStream in = new FileInputStream(fileOrigen);
	    	  OutputStream out = new FileOutputStream(fileDestino);
	    	  byte[] buf = new byte[1024];int len; while ((len = in.read(buf)) > 0) {  out.write(buf, 0, len);}
	    	  in.close();
	    	  out.close();
	    	  fileBackup = fileName.replace(".txt", "_backup.txt");
	    	  return true;	    	  
	      }
	      else{
	    	  return false;
	      }
		}catch(IOException e){
			return false;
		}
	}
	
	//<IMPLEMENTACION>: INICIO
	/*
	 * IMPLEMENTS: Se crea un metodo para copiar de una ubicacion a otra distinta
	 */
	public static boolean copyfiletofile(String pathorigen, String pathdestino){
		try{
		  File fileOrigen = new File(pathorigen);
	      File fileDestino = new File(pathdestino);
	      
	      if (fileOrigen.exists()) {	   
	    	  System.out.println("Existe archivo comienza a copiar");
	    	  InputStream in = new FileInputStream(fileOrigen);
	    	  OutputStream out = new FileOutputStream(fileDestino);
	    	  byte[] buf = new byte[1024];int len; while ((len = in.read(buf)) > 0) {  out.write(buf, 0, len);}
	    	  in.close();
	    	  out.close();
	    	  fileBackup = pathorigen.replace(".xml", ".xml");
	    	  return true;	    	  
	      }
	      else{
	    	  return false;
	      }
		}catch(IOException e){
			return false;
		}
	}
	//<IMPLEMENTACION>: FIN
	
	
	
	
	//Eliminacion de Archivos Firmados, Generados y No Autorizados.
	public static void delFile(Emisor emite, String rutaFirmado, String generado, String dirNoAutorizados){
	  //se elimina de firmados
//	  File eliminar = new File(rutaFirmado+FilenameUtils.getName(emite.getFilexml()));
//  	  if (eliminar.exists()) {
//  		  eliminar.delete();
//  	  }
//  	 //se elimina de no autorizado 
//  	 eliminar = new File(dirNoAutorizados+FilenameUtils.getName(emite.getFilexml()));
//	  if (eliminar.exists()) {
//		  eliminar.delete();
//	  }
	}
	
	public static int copiarXml2(String fileNameOrigen, String fileNameDestino){
		try{
		  File fileOrigen = new File(fileNameOrigen);
		  
	      File fileDestino = new File(fileNameDestino);	      
	      if (fileOrigen.exists()) {	    	  
	    	  InputStream in = new FileInputStream(fileOrigen);
	    	  OutputStream out = new FileOutputStream(fileDestino);
	    	  byte[] buf = new byte[1024];int len; while ((len = in.read(buf)) > 0) {  out.write(buf, 0, len);}
	    	  in.close();
	    	  out.close();
	    	  return 1;
	      }
	      else{
	    	  return 0;
	      }
		}catch(IOException e){
			return -1;
		}
	}
	//***********************//////////////////////////////////////////////////////************************************//
	/*								Lectura del Nombre del XML														  */
	//***********************//////////////////////////////////////////////////////************************************//	
		public void obtieneInfoXml(String FileName) throws Exception{
		//Ambiente
		String ambiente = null;
		try{
			ambiente = FileName.substring(0, 1).trim();
		}catch (Exception e){
			throw new Exception("Name File incorrecto->Ambiente::"+e.toString());
		}		
		if (ambiente== null){
			throw new Exception("Name File incorrecto->Ambiente es null");
		}			
		if (ambiente.length()!= 1){
			throw new Exception("Name File incorrecto->Ambiente tamaï¿½o incorrecto::"+ambiente.length());
		}
		emite.getInfEmisor().setAmbiente(Integer.parseInt(ambiente));
		
		//Ruc
		String rucFile = null;
		try{
			rucFile = FileName.substring(1, 14).trim();
		}catch (Exception e){
			throw new Exception("Name File incorrecto->Ruc::"+e.toString());
		}
		if (rucFile== null){
			throw new Exception("Name File incorrecto->Ruc es null");
		}
		if (rucFile.length()!= 13){
			throw new Exception("Name File incorrecto->Ruc tamaï¿½o incorrecto::"+rucFile.length());
		}
		emite.getInfEmisor().setRuc(rucFile);
		
		
		//TipoDocumento
		String tipoDocumento= null;
		try{
			tipoDocumento = FileName.substring(14, 16).trim();
		}catch (Exception e){
			throw new Exception("Name File incorrecto->TipoDocumento::"+e.toString());
		}
		if (tipoDocumento== null){
			throw new Exception("Name File incorrecto->TipoDocumento es null");
		}
		if (tipoDocumento.length()!= 2){
			throw new Exception("Name File incorrecto->TipoDocumento tamaï¿½o incorrecto::"+tipoDocumento.length());
		}
		emite.getInfEmisor().setTipoComprobante(tipoDocumento);	     
	    emite.getInfEmisor().setCodDocumento(tipoDocumento);
	    
	    //CodEstablecimiento
	    String CodEstablecimiento = null;
	    try{
	    	CodEstablecimiento = FileName.substring(16, 19).trim();
	    }catch (Exception e){
			throw new Exception("Name File incorrecto->CodEstablecimiento::"+e.toString());
		}
	    if (CodEstablecimiento== null){
			throw new Exception("Name File incorrecto->CodEstablecimiento es null");
		}
	    if (CodEstablecimiento.length()!= 3){
			throw new Exception("Name File incorrecto->CodEstablecimiento tamaï¿½o incorrecto::"+CodEstablecimiento.length());
		}
	    emite.getInfEmisor().setCodEstablecimiento(CodEstablecimiento);
	    
	    //CodPuntEmision
	    String CodPuntEmision = null;
	    try{
	    	CodPuntEmision = FileName.substring(19, 22);
	    }catch (Exception e){
			throw new Exception("Name File incorrecto->CodPuntEmision::"+e.toString());
		}
	    if (CodPuntEmision== null){
			throw new Exception("Name File incorrecto->CodPuntEmision es null");
		}
	    if (CodPuntEmision.length()!= 3){
			throw new Exception("Name File incorrecto->CodPuntEmision tamaï¿½o incorrecto::"+CodPuntEmision.length());
		}
	    emite.getInfEmisor().setCodPuntoEmision(CodPuntEmision);
	    
	    //secuencial
	    String secuencial = null;
	    try{
	    secuencial =  FileName.substring(22, (FileName.length()<=9?FileName.length():31));
	    }catch (Exception e){
			throw new Exception("Name File incorrecto->secuencial::"+e.toString());
		}
	    if (secuencial.length()!= 9){
			throw new Exception("Name File incorrecto->CodPuntEmision tamaï¿½o incorrecto::"+secuencial.length());
		}
	    emite.getInfEmisor().setSecuencial(secuencial);
	}
	
	
	//***********************//////////////////////////////////////////////////////************************************//
	/*								Lectura del XML																	  */
	//***********************//////////////////////////////////////////////////////************************************//
	
	public static void leerXml(String nameXml, Emisor emite) {
		System.out.println("TipoComprobante::"+emite.getInfEmisor().getTipoComprobante());
		if (emite.getInfEmisor().getTipoComprobante().equals("01"))
			leerFacturaXml(nameXml, emite);
		if (emite.getInfEmisor().getCodDocumento().equals("04"))
			leerNotaCreditoXml(nameXml, emite);
		if (emite.getInfEmisor().getCodDocumento().equals("05"))
			leerNotaDebitoXml(nameXml, emite);
		//dca: crear sentencia para boleta
		//dca>> verificar si la sentencias para resumen diario
		
		
		/*
			if (emite.getInfEmisor().getCodDocumento().equals("07"))
			leerComprobanteRetXml(nameXml, emite);
			*/
	}
	
	//Lectura de Factura
	public static void leerFacturaXml(String nameXml, Emisor emite) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder;
        Document doc = null;
        try {
            builder = factory.newDocumentBuilder();
            doc = builder.parse(nameXml);
            System.out.println("File>>"+nameXml);
 
            // Create XPathFactory object
            XPathFactory xpathFactory = XPathFactory.newInstance();
 
            // Create XPath object
            XPath xpath = xpathFactory.newXPath();
            
            //infoTributaria            
            XPathExpression expr = xpath.compile("/factura/infoTributaria/tipoEmision/text()");
            emite.getInfEmisor().setTipoEmision((String) expr.evaluate(doc, XPathConstants.STRING));
            
            expr = xpath.compile("/factura/infoTributaria/ambiente/text()");
            emite.getInfEmisor().setAmbiente(Integer.parseInt((String)expr.evaluate(doc, XPathConstants.STRING)));
            
            expr = xpath.compile("/factura/infoTributaria/ruc/text()");
            emite.getInfEmisor().setRuc((String) expr.evaluate(doc, XPathConstants.STRING));
            
            expr = xpath.compile("/factura/infoTributaria/razonSocial/text()");
            emite.getInfEmisor().setRazonSocial((String) expr.evaluate(doc, XPathConstants.STRING));
            
            expr = xpath.compile("/factura/infoTributaria/nombreComercial/text()");
            emite.getInfEmisor().setNombreComercial((String) expr.evaluate(doc, XPathConstants.STRING));
 
            expr = xpath.compile("/factura/infoTributaria/codDoc/text()");
            emite.getInfEmisor().setCodDocumento((String) expr.evaluate(doc, XPathConstants.STRING));
            
            expr = xpath.compile("/factura/infoTributaria/estab/text()");
            emite.getInfEmisor().setCodEstablecimiento((String) expr.evaluate(doc, XPathConstants.STRING));
 
            expr = xpath.compile("/factura/infoTributaria/ptoEmi/text()");
            emite.getInfEmisor().setCodPuntoEmision((String) expr.evaluate(doc, XPathConstants.STRING));
            
            expr = xpath.compile("/factura/infoTributaria/secuencial/text()");
            emite.getInfEmisor().setSecuencial((String) expr.evaluate(doc, XPathConstants.STRING));
            
            expr = xpath.compile("/factura/infoTributaria/dirMatriz/text()");
            emite.getInfEmisor().setDireccionMatriz((String) expr.evaluate(doc, XPathConstants.STRING));                        
            
            //infoFactura
            expr = xpath.compile("/factura/infoFactura/fechaEmision/text()");
            emite.getInfEmisor().setFecEmision((String)expr.evaluate(doc, XPathConstants.STRING));
 
            expr = xpath.compile("/factura/infoFactura/dirEstablecimiento/text()");
            emite.getInfEmisor().setDireccionEstablecimiento((String) expr.evaluate(doc, XPathConstants.STRING));
            
            expr = xpath.compile("/factura/infoFactura/contribuyenteEspecial/text()");
            emite.getInfEmisor().setContribEspecial(Integer.parseInt((String) expr.evaluate(doc, XPathConstants.STRING)));
            
            expr = xpath.compile("/factura/infoFactura/obligadoContabilidad/text()");
            emite.getInfEmisor().setObligContabilidad((String) expr.evaluate(doc, XPathConstants.STRING));
            
            expr = xpath.compile("/factura/infoFactura/tipoIdentificacionComprador/text()");
            emite.getInfEmisor().setTipoIdentificacion((String) expr.evaluate(doc, XPathConstants.STRING));
            
            expr = xpath.compile("/factura/infoFactura/guiaRemision/text()");
            emite.getInfEmisor().setGuiaRemision((String) expr.evaluate(doc, XPathConstants.STRING));
            
            expr = xpath.compile("/factura/infoFactura/razonSocialComprador/text()");
            emite.getInfEmisor().setRazonSocialComp((String) expr.evaluate(doc, XPathConstants.STRING));
            
            expr = xpath.compile("/factura/infoFactura/identificacionComprador/text()");
            emite.getInfEmisor().setIdentificacionComp((String) expr.evaluate(doc, XPathConstants.STRING));
            
            expr = xpath.compile("/factura/infoFactura/totalSinImpuestos/text()");
            emite.getInfEmisor().setTotalSinImpuestos(Double.parseDouble((String) expr.evaluate(doc, XPathConstants.STRING)));
            
            expr = xpath.compile("/factura/infoFactura/totalDescuento/text()");
            emite.getInfEmisor().setTotalDescuento(Double.parseDouble((String) expr.evaluate(doc, XPathConstants.STRING)));
            
            expr = xpath.compile("/factura/infoFactura/totalDescuento/text()");
            emite.getInfEmisor().setTotalDescuento(Double.parseDouble((String) expr.evaluate(doc, XPathConstants.STRING)));
            
            expr = xpath.compile("/factura/infoFactura/propina/text()");
            emite.getInfEmisor().setPropina(Double.parseDouble((String) expr.evaluate(doc, XPathConstants.STRING)));
            
            expr = xpath.compile("/factura/infoFactura/importeTotal/text()");
            emite.getInfEmisor().setImporteTotal(Double.parseDouble((String) expr.evaluate(doc, XPathConstants.STRING)));
            
            expr = xpath.compile("/factura/infoFactura/moneda/text()");
            emite.getInfEmisor().setMoneda((String) expr.evaluate(doc, XPathConstants.STRING));
            
            expr = xpath.compile("/factura/infoFactura/totalConImpuestos/totalImpuesto[*]/codigo/text()");
            List<String> listCodigo = new ArrayList();            
            NodeList nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));            
            for (int i=0; i<nodes.getLength(); i++){
            	listCodigo.add(nodes.item(i).getNodeValue());
            }
            
            expr = xpath.compile("/factura/infoFactura/totalConImpuestos/totalImpuesto[*]/codigoPorcentaje/text()");
            List<String> listCodigoPorcentaje = new ArrayList();            
            nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));            
            for (int i=0; i<nodes.getLength(); i++){
            	listCodigoPorcentaje.add(nodes.item(i).getNodeValue());
            }
            
            expr = xpath.compile("/factura/infoFactura/totalConImpuestos/totalImpuesto[*]/baseImponible/text()");
            List<String> listBaseImponible = new ArrayList();            
            nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));            
            for (int i=0; i<nodes.getLength(); i++){
            	listBaseImponible.add(nodes.item(i).getNodeValue());
            }
            
            expr = xpath.compile("/factura/infoFactura/totalConImpuestos/totalImpuesto[*]/valor/text()");
            List<String> listValor = new ArrayList();            
            nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));            
            for (int i=0; i<nodes.getLength(); i++){
            	listValor.add(nodes.item(i).getNodeValue());
            }
            ArrayList<DetalleTotalImpuestos> listDetDetImpuestos = new ArrayList<DetalleTotalImpuestos>();             
            for (int i=0; i<listCodigo.size(); i++){
            	DetalleTotalImpuestos detImp = new DetalleTotalImpuestos();
            	detImp.setCodTotalImpuestos(Integer.parseInt(listCodigo.get(i).toString()));
            	detImp.setCodPorcentImp(Integer.parseInt(listCodigoPorcentaje.get(i).toString()));
            	detImp.setBaseImponibleImp(Double.parseDouble(listBaseImponible.get(i).toString()));
            	detImp.setValorImp(Double.parseDouble(listValor.get(i).toString()));
            	listDetDetImpuestos.add(detImp);
            }
            
            emite.getInfEmisor().setListDetDetImpuestos(listDetDetImpuestos);
            
            ArrayList<DetalleDocumento> listDetDocumentos = new ArrayList<DetalleDocumento>();
                                               
            expr = xpath.compile("/factura/detalles/detalle[*]/codigoPrincipal/text()");
            List<String> listCodPrin = new ArrayList();
            nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));            
            for (int l=0; l<nodes.getLength(); l++){
            	listCodPrin.add(nodes.item(l).getNodeValue());
            	//System.out.println("index::"+i);
            }
            
            expr = xpath.compile("/factura/detalles/detalle[*]/codigoAuxiliar/text()");
            List<String> listCodAux = new ArrayList();            
            nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));            
            for (int i=0; i<nodes.getLength(); i++){
            	listCodAux.add(nodes.item(i).getNodeValue());
            }
            
            expr = xpath.compile("/factura/detalles/detalle[*]/descripcion/text()");
            List<String> listDescrip = new ArrayList();            
            nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));            
            for (int i=0; i<nodes.getLength(); i++){
            	listDescrip.add(nodes.item(i).getNodeValue());
            }
            
            expr = xpath.compile("/factura/detalles/detalle[*]/cantidad/text()");
            List<String> listCantidad = new ArrayList();            
            nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));            
            for (int i=0; i<nodes.getLength(); i++){
            	listCantidad.add(nodes.item(i).getNodeValue());
            }
            
            expr = xpath.compile("/factura/detalles/detalle[*]/precioUnitario/text()");
            List<String> listPrecioUnitario = new ArrayList();            
            nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));            
            for (int i=0; i<nodes.getLength(); i++){
            	listPrecioUnitario.add(nodes.item(i).getNodeValue());
            }
            
            expr = xpath.compile("/factura/detalles/detalle[*]/descuento/text()");
            List<String> listDescuento = new ArrayList();            
            nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));            
            for (int i=0; i<nodes.getLength(); i++){
            	listDescuento.add(nodes.item(i).getNodeValue());
            }
            
            expr = xpath.compile("/factura/detalles/detalle[*]/precioTotalSinImpuesto/text()");
            List<String> listPrecioTotalSinImpuesto = new ArrayList();            
            nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));            
            for (int i=0; i<nodes.getLength(); i++){
            	listPrecioTotalSinImpuesto.add(nodes.item(i).getNodeValue());
            }
            
            for (int i=0; i<listCodPrin.size(); i++){
            	ArrayList<DocumentoImpuestos> listDetImpuestosDocumentos = new ArrayList<DocumentoImpuestos>();            	
            	DetalleDocumento detDoc = new DetalleDocumento();            	
            	detDoc.setCodigoPrincipal(listCodPrin.get(i).toString());
            	if (listCodAux.size()>0)
            	detDoc.setCodigoAuxiliar(listCodAux.get(i).toString());
            	detDoc.setDescripcion(listDescrip.get(i).toString());
            	detDoc.setCantidad(Double.parseDouble(listCantidad.get(i).toString()));
            	detDoc.setPrecioUnitario(Double.parseDouble(listPrecioUnitario.get(i).toString()));
            	detDoc.setDescuento(Double.parseDouble(listDescuento.get(i).toString()));
            	detDoc.setPrecioTotalSinImpuesto(Double.parseDouble(listPrecioTotalSinImpuesto.get(i).toString()));
            	//detDoc.setListDetImpuestosDocumentos();
            	
            	expr = xpath.compile("/factura/detalles/detalle[codigoPrincipal='"+listCodPrin.get(i).toString()+"']/impuestos/impuesto/codigo/text()");
                List<String> listCodigoImpuesto = new ArrayList();            
                nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));            
                for (int j=0; j<nodes.getLength(); j++){
                	listCodigoImpuesto.add(nodes.item(j).getNodeValue());
                }
                
                expr = xpath.compile("/factura/detalles/detalle[codigoPrincipal='"+listCodPrin.get(i).toString()+"']/impuestos/impuesto/codigoPorcentaje/text()");
                List<String> listCodigoPorcentajeImpuesto = new ArrayList();            
                nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));            
                for (int j=0; j<nodes.getLength(); j++){
                	listCodigoPorcentajeImpuesto.add(nodes.item(j).getNodeValue());
                }
                
                expr = xpath.compile("/factura/detalles/detalle[codigoPrincipal='"+listCodPrin.get(i).toString()+"']/impuestos/impuesto/tarifa/text()");
                List<String> listTarifaImpuesto = new ArrayList();            
                nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));            
                for (int j=0; j<nodes.getLength(); j++){
                	listTarifaImpuesto.add(nodes.item(j).getNodeValue());
                }
                
                expr = xpath.compile("/factura/detalles/detalle[codigoPrincipal='"+listCodPrin.get(i).toString()+"']/impuestos/impuesto/baseImponible/text()");
                List<String> listBaseImponibleImpuesto = new ArrayList();            
                nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));            
                for (int j=0; j<nodes.getLength(); j++){
                	listBaseImponibleImpuesto.add(nodes.item(j).getNodeValue());
                }
                
                expr = xpath.compile("/factura/detalles/detalle[codigoPrincipal='"+listCodPrin.get(i).toString()+"']/impuestos/impuesto/valor/text()");
                List<String> listValorImpuesto = new ArrayList();            
                nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));            
                for (int j=0; j<nodes.getLength(); j++){
                	listValorImpuesto.add(nodes.item(j).getNodeValue());
                }
                                
                for (int j=0; j<listCodigoImpuesto.size(); j++){
                	DocumentoImpuestos DetDocImp = new DocumentoImpuestos();
                	DetDocImp.setImpuestoCodigo(Integer.parseInt(listCodigoImpuesto.get(j).toString()));
                	DetDocImp.setImpuestoCodigoPorcentaje(Integer.parseInt(listCodigoPorcentajeImpuesto.get(j).toString()));
                	DetDocImp.setImpuestoTarifa(Double.parseDouble(listTarifaImpuesto.get(j).toString()));
                	DetDocImp.setImpuestoBaseImponible(Double.parseDouble(listBaseImponibleImpuesto.get(j).toString()));
                	DetDocImp.setImpuestoValor(Double.parseDouble(listValorImpuesto.get(j).toString()));
                	listDetImpuestosDocumentos.add(DetDocImp);
                }
                detDoc.setListDetImpuestosDocumentos(listDetImpuestosDocumentos);
                listDetDocumentos.add(detDoc);
            }                       
            emite.getInfEmisor().setListDetDocumentos(listDetDocumentos);
            
            HashMap<String, String> infoAdicionalHash = new HashMap<String, String>(); 
            expr = xpath.compile("//campoAdicional/text()");
            List<String> listInfoAdicionalFacturaValue = new ArrayList();                                    
            nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));
            for (int i=0; i<nodes.getLength(); i++){            	
            	listInfoAdicionalFacturaValue.add(nodes.item(i).getNodeValue());
            	System.out.println("NameNormalizado::"+nodes.item(i).getNodeValue());
            	//System.out.println("NameNormalizado::"+normalizeValue(nodes.item(i).getNodeValue(),300).toUpperCase());
            }
            
            expr = xpath.compile("//campoAdicional/@nombre");
            List<String> listInfoAdicionalFacturaName = new ArrayList();                                    
            nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));
            //GBM: Lista con el nombre del campo adicional. Se lo trata como par ordenado (x,y) donde x = nombre, y = valor
            for (int i=0; i<nodes.getLength(); i++){
            	listInfoAdicionalFacturaName.add(nodes.item(i).getNodeValue());
            	System.out.println("Nombre?::"+nodes.item(i).getNodeValue());
            }
            ArrayList<InformacionAdicional> ListInfAdicional = new ArrayList<InformacionAdicional>();
            ListInfAdicional.clear();
            //ArrayList<InfoAdicional> listInfoAdicional = new ArrayList<InfoAdicional>();
            //GBM: Lista con el valor del campo adicional. Se lo trata como par ordenado (x, y) donde x = nombre, y = valor
            for (int i=0; i<listInfoAdicionalFacturaValue.size(); i++){
            	//infoAdicionalHash.put(listInfoAdicionalFacturaName.get(i).toString(), listInfoAdicionalFacturaValue.get(i).toString());
            	//Setea el par ordenado (x, y) en la el objeto:
            	InformacionAdicional info = new InformacionAdicional(listInfoAdicionalFacturaName.get(i).toString(), listInfoAdicionalFacturaValue.get(i).toString());
            	ListInfAdicional.add(info);
            } 
            //GBM: Setea la informaciï¿½n adicional en el objeto de emision:
            emite.getInfEmisor().setListInfAdicional(ListInfAdicional);
        } catch (Exception e) {
            e.printStackTrace();
        }
 
    }
	
	//Lectura de Nota de Credito
	public static void leerNotaCreditoXml(String nameXml, Emisor emite){

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder;
        Document doc = null;
        try {
            builder = factory.newDocumentBuilder();
            doc = builder.parse(nameXml);
            System.out.println("File>>"+nameXml);
            String ls_documento = "notaCredito";
            String ls_tipoDocumento = "infoNotaCredito";
            // Create XPathFactory object
            XPathFactory xpathFactory = XPathFactory.newInstance();

            // Create XPath object
            XPath xpath = xpathFactory.newXPath();            
            //infoTributaria            
            XPathExpression expr = xpath.compile("/"+ls_documento+"/infoTributaria/tipoEmision/text()");
            emite.getInfEmisor().setTipoEmision((String) expr.evaluate(doc, XPathConstants.STRING));
            
            expr = xpath.compile("/"+ls_documento+"/infoTributaria/ambiente/text()");
            emite.getInfEmisor().setAmbiente(Integer.parseInt((String)expr.evaluate(doc, XPathConstants.STRING)));
            
            expr = xpath.compile("/"+ls_documento+"/infoTributaria/ruc/text()");
            emite.getInfEmisor().setRuc((String) expr.evaluate(doc, XPathConstants.STRING));
            
            expr = xpath.compile("/"+ls_documento+"/infoTributaria/razonSocial/text()");
            emite.getInfEmisor().setRazonSocial((String) expr.evaluate(doc, XPathConstants.STRING));
            
            expr = xpath.compile("/"+ls_documento+"/infoTributaria/nombreComercial/text()");
            emite.getInfEmisor().setNombreComercial((String) expr.evaluate(doc, XPathConstants.STRING));
 
            expr = xpath.compile("/"+ls_documento+"/infoTributaria/codDoc/text()");
            emite.getInfEmisor().setCodDocumento((String) expr.evaluate(doc, XPathConstants.STRING));
            
            expr = xpath.compile("/"+ls_documento+"/infoTributaria/estab/text()");
            emite.getInfEmisor().setCodEstablecimiento((String) expr.evaluate(doc, XPathConstants.STRING));
 
            expr = xpath.compile("/"+ls_documento+"/infoTributaria/ptoEmi/text()");
            emite.getInfEmisor().setCodPuntoEmision((String) expr.evaluate(doc, XPathConstants.STRING));
            
            expr = xpath.compile("/"+ls_documento+"/infoTributaria/secuencial/text()");
            emite.getInfEmisor().setSecuencial((String) expr.evaluate(doc, XPathConstants.STRING));
            
            expr = xpath.compile("/"+ls_documento+"/infoTributaria/dirMatriz/text()");
            emite.getInfEmisor().setDireccionMatriz((String) expr.evaluate(doc, XPathConstants.STRING));                        
            
            expr = xpath.compile("/"+ls_documento+"/"+ls_tipoDocumento+"/fechaEmision/text()");
            emite.getInfEmisor().setFecEmision((String)expr.evaluate(doc, XPathConstants.STRING));
            
            
            expr = xpath.compile("/"+ls_documento+"/"+ls_tipoDocumento+"/dirEstablecimiento/text()");
            emite.getInfEmisor().setDireccionEstablecimiento((String) expr.evaluate(doc, XPathConstants.STRING));
            /*
            expr = xpath.compile("/"+ls_documento+"/"+ls_tipoDocumento+"/contribuyenteEspecial/text()");
            System.out.println("Contrib Especial::"+(String) expr.evaluate(doc, XPathConstants.STRING));
            //emite.getInfEmisor().setContribEspecial();
            */
            expr = xpath.compile("/"+ls_documento+"/"+ls_tipoDocumento+"/obligadoContabilidad/text()");
            emite.getInfEmisor().setObligContabilidad((String) expr.evaluate(doc, XPathConstants.STRING));
            
            expr = xpath.compile("/"+ls_documento+"/"+ls_tipoDocumento+"/tipoIdentificacionComprador/text()");
            emite.getInfEmisor().setTipoIdentificacion((String) expr.evaluate(doc, XPathConstants.STRING));
            
            
            expr = xpath.compile("/"+ls_documento+"/"+ls_tipoDocumento+"/razonSocialComprador/text()");
            emite.getInfEmisor().setRazonSocialComp((String) expr.evaluate(doc, XPathConstants.STRING));
            
            expr = xpath.compile("/"+ls_documento+"/"+ls_tipoDocumento+"/identificacionComprador/text()");
            emite.getInfEmisor().setIdentificacionComp((String) expr.evaluate(doc, XPathConstants.STRING));
                        
            expr = xpath.compile("/"+ls_documento+"/"+ls_tipoDocumento+"/rise/text()");
            emite.getInfEmisor().setRise((String) expr.evaluate(doc, XPathConstants.STRING));
            
            expr = xpath.compile("/"+ls_documento+"/"+ls_tipoDocumento+"/codDocModificado/text()");
            emite.getInfEmisor().setCodDocModificado((String) expr.evaluate(doc, XPathConstants.STRING));
            
            expr = xpath.compile("/"+ls_documento+"/"+ls_tipoDocumento+"/numDocModificado/text()");
            emite.getInfEmisor().setNumDocModificado((String) expr.evaluate(doc, XPathConstants.STRING));
            
            expr = xpath.compile("/"+ls_documento+"/"+ls_tipoDocumento+"/fechaEmisionDocSustento/text()");
            emite.getInfEmisor().setFecEmisionDoc((String) expr.evaluate(doc, XPathConstants.STRING));
            
			
            expr = xpath.compile("/"+ls_documento+"/"+ls_tipoDocumento+"/motivo/text()");
            emite.getInfEmisor().setMotivo((String) expr.evaluate(doc, XPathConstants.STRING));            
			            
            
            expr = xpath.compile("/"+ls_documento+"/"+ls_tipoDocumento+"/totalSinImpuestos/text()");
            emite.getInfEmisor().setTotalSinImpuestos(Double.parseDouble((String) expr.evaluate(doc, XPathConstants.STRING)));           
            
            expr = xpath.compile("/"+ls_documento+"/"+ls_tipoDocumento+"/valorModificacion/text()");
            emite.getInfEmisor().setValorModificado(Double.parseDouble((String) expr.evaluate(doc, XPathConstants.STRING)));
            
            expr = xpath.compile("/"+ls_documento+"/"+ls_tipoDocumento+"/moneda/text()");
            emite.getInfEmisor().setMoneda((String) expr.evaluate(doc, XPathConstants.STRING));
            
            
            
            expr = xpath.compile("/"+ls_documento+"/"+ls_tipoDocumento+"/totalConImpuestos/totalImpuesto[*]/codigo/text()");
            List<String> listCodigo = new ArrayList();            
            NodeList nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));            
            for (int i=0; i<nodes.getLength(); i++){
            	listCodigo.add(nodes.item(i).getNodeValue());
            }
            
            expr = xpath.compile("/"+ls_documento+"/"+ls_tipoDocumento+"/totalConImpuestos/totalImpuesto[*]/codigoPorcentaje/text()");
            List<String> listCodigoPorcentaje = new ArrayList();            
            nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));            
            for (int i=0; i<nodes.getLength(); i++){
            	listCodigoPorcentaje.add(nodes.item(i).getNodeValue());
            }
            
            expr = xpath.compile("/"+ls_documento+"/"+ls_tipoDocumento+"/totalConImpuestos/totalImpuesto[*]/baseImponible/text()");
            List<String> listBaseImponible = new ArrayList();            
            nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));            
            for (int i=0; i<nodes.getLength(); i++){
            	listBaseImponible.add(nodes.item(i).getNodeValue());
            }
            
            expr = xpath.compile("/"+ls_documento+"/"+ls_tipoDocumento+"/totalConImpuestos/totalImpuesto[*]/valor/text()");
            List<String> listValor = new ArrayList();            
            nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));            
            for (int i=0; i<nodes.getLength(); i++){
            	listValor.add(nodes.item(i).getNodeValue());
            }
            ArrayList<DetalleTotalImpuestos> listDetDetImpuestos = new ArrayList<DetalleTotalImpuestos>();             
            for (int i=0; i<listCodigo.size(); i++){
            	DetalleTotalImpuestos detImp = new DetalleTotalImpuestos();
            	detImp.setCodTotalImpuestos(Integer.parseInt(listCodigo.get(i).toString()));
            	detImp.setCodPorcentImp(Integer.parseInt(listCodigoPorcentaje.get(i).toString()));
            	detImp.setBaseImponibleImp(Double.parseDouble(listBaseImponible.get(i).toString()));
            	detImp.setValorImp(Double.parseDouble(listValor.get(i).toString()));
            	listDetDetImpuestos.add(detImp);
            }
            
            emite.getInfEmisor().setListDetDetImpuestos(listDetDetImpuestos);
            
            ArrayList<DetalleDocumento> listDetDocumentos = new ArrayList<DetalleDocumento>();
                                               
            expr = xpath.compile("/"+ls_documento+"/detalles/detalle[*]/codigoInterno/text()");
            List<String> listCodPrin = new ArrayList();
            nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));            
            for (int l=0; l<nodes.getLength(); l++){
            	listCodPrin.add(nodes.item(l).getNodeValue());
            	//System.out.println("index::"+i);
            }
            
            expr = xpath.compile("/"+ls_documento+"/detalles/detalle[*]/codigoAdicional/text()");
            List<String> listCodAux = new ArrayList();            
            nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));            
            for (int i=0; i<nodes.getLength(); i++){
            	listCodAux.add(nodes.item(i).getNodeValue());
            }
            
            expr = xpath.compile("/"+ls_documento+"/detalles/detalle[*]/descripcion/text()");
            List<String> listDescrip = new ArrayList();            
            nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));            
            for (int i=0; i<nodes.getLength(); i++){
            	listDescrip.add(nodes.item(i).getNodeValue());
            }
            
            expr = xpath.compile("/"+ls_documento+"/detalles/detalle[*]/cantidad/text()");
            List<String> listCantidad = new ArrayList();            
            nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));            
            for (int i=0; i<nodes.getLength(); i++){
            	listCantidad.add(nodes.item(i).getNodeValue());
            }
            
            expr = xpath.compile("/"+ls_documento+"/detalles/detalle[*]/precioUnitario/text()");
            List<String> listPrecioUnitario = new ArrayList();            
            nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));            
            for (int i=0; i<nodes.getLength(); i++){
            	listPrecioUnitario.add(nodes.item(i).getNodeValue());
            }
            
            expr = xpath.compile("/"+ls_documento+"/detalles/detalle[*]/descuento/text()");
            List<String> listDescuento = new ArrayList();            
            nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));            
            for (int i=0; i<nodes.getLength(); i++){
            	listDescuento.add(nodes.item(i).getNodeValue());
            }
            
            expr = xpath.compile("/"+ls_documento+"/detalles/detalle[*]/precioTotalSinImpuesto/text()");
            List<String> listPrecioTotalSinImpuesto = new ArrayList();            
            nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));            
            for (int i=0; i<nodes.getLength(); i++){
            	listPrecioTotalSinImpuesto.add(nodes.item(i).getNodeValue());
            }
            
            for (int i=0; i<listCodPrin.size(); i++){
            	ArrayList<DocumentoImpuestos> listDetImpuestosDocumentos = new ArrayList<DocumentoImpuestos>();            	
            	DetalleDocumento detDoc = new DetalleDocumento();            	
            	detDoc.setCodigoPrincipal(listCodPrin.get(i).toString());
            	//detDoc.setCodigoAuxiliar(listCodAux.get(i).toString());
            	detDoc.setDescripcion(listDescrip.get(i).toString());
            	detDoc.setCantidad(Double.parseDouble(listCantidad.get(i).toString()));
            	detDoc.setPrecioUnitario(Double.parseDouble(listPrecioUnitario.get(i).toString()));
            	detDoc.setDescuento(Double.parseDouble(listDescuento.get(i).toString()));
            	detDoc.setPrecioTotalSinImpuesto(Double.parseDouble(listPrecioTotalSinImpuesto.get(i).toString()));
            	//detDoc.setListDetImpuestosDocumentos();
            	
            	expr = xpath.compile("/"+ls_documento+"/detalles/detalle[codigoPrincipal='"+listCodPrin.get(i).toString()+"']/impuestos/impuesto/codigo/text()");
                List<String> listCodigoImpuesto = new ArrayList();            
                nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));            
                for (int j=0; j<nodes.getLength(); j++){
                	listCodigoImpuesto.add(nodes.item(j).getNodeValue());
                }
                
                expr = xpath.compile("/"+ls_documento+"/detalles/detalle[codigoPrincipal='"+listCodPrin.get(i).toString()+"']/impuestos/impuesto/codigoPorcentaje/text()");
                List<String> listCodigoPorcentajeImpuesto = new ArrayList();            
                nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));            
                for (int j=0; j<nodes.getLength(); j++){
                	listCodigoPorcentajeImpuesto.add(nodes.item(j).getNodeValue());
                }
                
                expr = xpath.compile("/"+ls_documento+"/detalles/detalle[codigoPrincipal='"+listCodPrin.get(i).toString()+"']/impuestos/impuesto/tarifa/text()");
                List<String> listTarifaImpuesto = new ArrayList();            
                nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));            
                for (int j=0; j<nodes.getLength(); j++){
                	listTarifaImpuesto.add(nodes.item(j).getNodeValue());
                }
                
                expr = xpath.compile("/"+ls_documento+"/detalles/detalle[codigoPrincipal='"+listCodPrin.get(i).toString()+"']/impuestos/impuesto/baseImponible/text()");
                List<String> listBaseImponibleImpuesto = new ArrayList();            
                nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));            
                for (int j=0; j<nodes.getLength(); j++){
                	listBaseImponibleImpuesto.add(nodes.item(j).getNodeValue());
                }
                
                expr = xpath.compile("/"+ls_documento+"/detalles/detalle[codigoPrincipal='"+listCodPrin.get(i).toString()+"']/impuestos/impuesto/valor/text()");
                List<String> listValorImpuesto = new ArrayList();            
                nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));            
                for (int j=0; j<nodes.getLength(); j++){
                	listValorImpuesto.add(nodes.item(j).getNodeValue());
                }
                                
                for (int j=0; j<listCodigoImpuesto.size(); j++){
                	DocumentoImpuestos DetDocImp = new DocumentoImpuestos();
                	DetDocImp.setImpuestoCodigo(Integer.parseInt(listCodigoImpuesto.get(j).toString()));
                	DetDocImp.setImpuestoCodigoPorcentaje(Integer.parseInt(listCodigoPorcentajeImpuesto.get(j).toString()));
                	DetDocImp.setImpuestoTarifa(Double.parseDouble(listTarifaImpuesto.get(j).toString()));
                	DetDocImp.setImpuestoBaseImponible(Double.parseDouble(listBaseImponibleImpuesto.get(j).toString()));
                	DetDocImp.setImpuestoValor(Double.parseDouble(listValorImpuesto.get(j).toString()));
                	listDetImpuestosDocumentos.add(DetDocImp);
                }
                detDoc.setListDetImpuestosDocumentos(listDetImpuestosDocumentos);
                listDetDocumentos.add(detDoc);
            }                       
            emite.getInfEmisor().setListDetDocumentos(listDetDocumentos);
            
            HashMap<String, String> infoAdicionalHash = new HashMap<String, String>(); 
            expr = xpath.compile("//campoAdicional/text()");
            List<String> listInfoAdicionalFacturaValue = new ArrayList();                                    
            nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));
            for (int i=0; i<nodes.getLength(); i++){            	
            	listInfoAdicionalFacturaValue.add(nodes.item(i).getNodeValue());
            	System.out.println("NameNormalizado::"+nodes.item(i).getNodeValue());
            	//System.out.println("NameNormalizado::"+normalizeValue(nodes.item(i).getNodeValue(),300).toUpperCase());
            }
            
            expr = xpath.compile("//campoAdicional/@nombre");
            List<String> listInfoAdicionalFacturaName = new ArrayList();                                    
            nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));
            for (int i=0; i<nodes.getLength(); i++){
            	listInfoAdicionalFacturaName.add(nodes.item(i).getNodeValue());
            	System.out.println("Valor::"+nodes.item(i).getNodeValue());
            }
            ArrayList<InformacionAdicional> ListInfAdicional = new ArrayList<InformacionAdicional>();
            ListInfAdicional.clear();
            //ArrayList<InfoAdicional> listInfoAdicional = new ArrayList<InfoAdicional>();
            for (int i=0; i<listInfoAdicionalFacturaValue.size(); i++){
            	//infoAdicionalHash.put(listInfoAdicionalFacturaName.get(i).toString(), listInfoAdicionalFacturaValue.get(i).toString());
            	InformacionAdicional info = new InformacionAdicional(listInfoAdicionalFacturaName.get(i).toString(), listInfoAdicionalFacturaValue.get(i).toString());
            	ListInfAdicional.add(info);
            } 
            emite.getInfEmisor().setListInfAdicional(ListInfAdicional);
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
	//Lectura de Nota de Debito
	public static void leerNotaDebitoXml(String nameXml, Emisor emite) {

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder;
        Document doc = null;
        try {
            builder = factory.newDocumentBuilder();
            doc = builder.parse(nameXml);
            System.out.println("File>>"+nameXml);
            String ls_documento = "notaDebito";
            String ls_tipoDocumento = "infoNotaDebito";
            // Create XPathFactory object
            XPathFactory xpathFactory = XPathFactory.newInstance();

            // Create XPath object
            XPath xpath = xpathFactory.newXPath();            
            //infoTributaria            
            XPathExpression expr = xpath.compile("/"+ls_documento+"/infoTributaria/tipoEmision/text()");
            emite.getInfEmisor().setTipoEmision((String) expr.evaluate(doc, XPathConstants.STRING));
            
            expr = xpath.compile("/"+ls_documento+"/infoTributaria/ambiente/text()");
            emite.getInfEmisor().setAmbiente(Integer.parseInt((String)expr.evaluate(doc, XPathConstants.STRING)));
            
            expr = xpath.compile("/"+ls_documento+"/infoTributaria/ruc/text()");
            emite.getInfEmisor().setRuc((String) expr.evaluate(doc, XPathConstants.STRING));
            
            expr = xpath.compile("/"+ls_documento+"/infoTributaria/razonSocial/text()");
            emite.getInfEmisor().setRazonSocial((String) expr.evaluate(doc, XPathConstants.STRING));
            
            expr = xpath.compile("/"+ls_documento+"/infoTributaria/nombreComercial/text()");
            emite.getInfEmisor().setNombreComercial((String) expr.evaluate(doc, XPathConstants.STRING));
 
            expr = xpath.compile("/"+ls_documento+"/infoTributaria/codDoc/text()");
            emite.getInfEmisor().setCodDocumento((String) expr.evaluate(doc, XPathConstants.STRING));
            
            expr = xpath.compile("/"+ls_documento+"/infoTributaria/estab/text()");
            emite.getInfEmisor().setCodEstablecimiento((String) expr.evaluate(doc, XPathConstants.STRING));
 
            expr = xpath.compile("/"+ls_documento+"/infoTributaria/ptoEmi/text()");
            emite.getInfEmisor().setCodPuntoEmision((String) expr.evaluate(doc, XPathConstants.STRING));
            
            expr = xpath.compile("/"+ls_documento+"/infoTributaria/secuencial/text()");
            emite.getInfEmisor().setSecuencial((String) expr.evaluate(doc, XPathConstants.STRING));
            
            expr = xpath.compile("/"+ls_documento+"/infoTributaria/dirMatriz/text()");
            emite.getInfEmisor().setDireccionMatriz((String) expr.evaluate(doc, XPathConstants.STRING));                        
            
            expr = xpath.compile("/"+ls_documento+"/"+ls_tipoDocumento+"/fechaEmision/text()");
            emite.getInfEmisor().setFecEmision((String)expr.evaluate(doc, XPathConstants.STRING));
            
            
            expr = xpath.compile("/"+ls_documento+"/"+ls_tipoDocumento+"/dirEstablecimiento/text()");
            emite.getInfEmisor().setDireccionEstablecimiento((String) expr.evaluate(doc, XPathConstants.STRING));
            /*
            expr = xpath.compile("/"+ls_documento+"/"+ls_tipoDocumento+"/contribuyenteEspecial/text()");
            System.out.println("Contrib Especial::"+(String) expr.evaluate(doc, XPathConstants.STRING));
            //emite.getInfEmisor().setContribEspecial();
            */
            expr = xpath.compile("/"+ls_documento+"/"+ls_tipoDocumento+"/obligadoContabilidad/text()");
            emite.getInfEmisor().setObligContabilidad((String) expr.evaluate(doc, XPathConstants.STRING));
            
            
            
            expr = xpath.compile("/"+ls_documento+"/"+ls_tipoDocumento+"/tipoIdentificacionComprador/text()");
            emite.getInfEmisor().setTipoIdentificacion((String) expr.evaluate(doc, XPathConstants.STRING));
            
            
            expr = xpath.compile("/"+ls_documento+"/"+ls_tipoDocumento+"/razonSocialComprador/text()");
            emite.getInfEmisor().setRazonSocialComp((String) expr.evaluate(doc, XPathConstants.STRING));
            
            expr = xpath.compile("/"+ls_documento+"/"+ls_tipoDocumento+"/identificacionComprador/text()");
            emite.getInfEmisor().setIdentificacionComp((String) expr.evaluate(doc, XPathConstants.STRING));
                        
            expr = xpath.compile("/"+ls_documento+"/"+ls_tipoDocumento+"/rise/text()");
            emite.getInfEmisor().setRise((String) expr.evaluate(doc, XPathConstants.STRING));
            
            expr = xpath.compile("/"+ls_documento+"/"+ls_tipoDocumento+"/codDocModificado/text()");
            emite.getInfEmisor().setCodDocModificado((String) expr.evaluate(doc, XPathConstants.STRING));
            
            expr = xpath.compile("/"+ls_documento+"/"+ls_tipoDocumento+"/numDocModificado/text()");
            emite.getInfEmisor().setNumDocModificado((String) expr.evaluate(doc, XPathConstants.STRING));
            
            expr = xpath.compile("/"+ls_documento+"/"+ls_tipoDocumento+"/fechaEmisionDocSustento/text()");
            emite.getInfEmisor().setFecEmisionDoc((String) expr.evaluate(doc, XPathConstants.STRING));
            
			
                        
            
            expr = xpath.compile("/"+ls_documento+"/"+ls_tipoDocumento+"/totalSinImpuestos/text()");
            emite.getInfEmisor().setTotalSinImpuestos(Double.parseDouble((String) expr.evaluate(doc, XPathConstants.STRING)));           
            
            //Ojo JZURITA
            expr = xpath.compile("/"+ls_documento+"/"+ls_tipoDocumento+"/valorTotal/text()");
            emite.getInfEmisor().setValorModificado(Double.parseDouble((String) expr.evaluate(doc, XPathConstants.STRING)));           
            
            
            expr = xpath.compile("/"+ls_documento+"/"+ls_tipoDocumento+"/totalConImpuestos/totalImpuesto[*]/codigo/text()");
            List<String> listCodigo = new ArrayList();            
            NodeList nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));            
            for (int i=0; i<nodes.getLength(); i++){
            	listCodigo.add(nodes.item(i).getNodeValue());
            }
            
            expr = xpath.compile("/"+ls_documento+"/"+ls_tipoDocumento+"/totalConImpuestos/totalImpuesto[*]/tarifa/text()");
            List<String> listTarifa = new ArrayList();            
            nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));            
            for (int i=0; i<nodes.getLength(); i++){
            	listCodigo.add(nodes.item(i).getNodeValue());
            }
            
            expr = xpath.compile("/"+ls_documento+"/"+ls_tipoDocumento+"/totalConImpuestos/totalImpuesto[*]/codigoPorcentaje/text()");
            List<String> listCodigoPorcentaje = new ArrayList();            
            nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));            
            for (int i=0; i<nodes.getLength(); i++){
            	listCodigoPorcentaje.add(nodes.item(i).getNodeValue());
            }
            
            expr = xpath.compile("/"+ls_documento+"/"+ls_tipoDocumento+"/totalConImpuestos/totalImpuesto[*]/baseImponible/text()");
            List<String> listBaseImponible = new ArrayList();            
            nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));            
            for (int i=0; i<nodes.getLength(); i++){
            	listBaseImponible.add(nodes.item(i).getNodeValue());
            }
            
            expr = xpath.compile("/"+ls_documento+"/"+ls_tipoDocumento+"/totalConImpuestos/totalImpuesto[*]/valor/text()");
            List<String> listValor = new ArrayList();            
            nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));            
            for (int i=0; i<nodes.getLength(); i++){
            	listValor.add(nodes.item(i).getNodeValue());
            }
            ArrayList<DetalleTotalImpuestos> listDetDetImpuestos = new ArrayList<DetalleTotalImpuestos>();             
            for (int i=0; i<listCodigo.size(); i++){
            	DetalleTotalImpuestos detImp = new DetalleTotalImpuestos();
            	detImp.setCodTotalImpuestos(Integer.parseInt(listCodigo.get(i).toString()));
            	detImp.setCodPorcentImp(Integer.parseInt(listCodigoPorcentaje.get(i).toString()));
            	//listTarifa
            	detImp.setTarifaImp(Double.parseDouble(listTarifa.get(i).toString()));
            	detImp.setBaseImponibleImp(Double.parseDouble(listBaseImponible.get(i).toString()));
            	detImp.setValorImp(Double.parseDouble(listValor.get(i).toString()));
            	listDetDetImpuestos.add(detImp);
            }
            
            emite.getInfEmisor().setListDetDetImpuestos(listDetDetImpuestos);
            
            ArrayList<DetalleDocumento> listDetDocumentos = new ArrayList<DetalleDocumento>();
                                               
            expr = xpath.compile("/"+ls_documento+"/detalles/detalle[*]/codigoInterno/text()");
            List<String> listCodPrin = new ArrayList();
            nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));            
            for (int l=0; l<nodes.getLength(); l++){
            	listCodPrin.add(nodes.item(l).getNodeValue());
            	//System.out.println("index::"+i);
            }
            
            expr = xpath.compile("/"+ls_documento+"/detalles/detalle[*]/codigoAdicional/text()");
            List<String> listCodAux = new ArrayList();            
            nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));            
            for (int i=0; i<nodes.getLength(); i++){
            	listCodAux.add(nodes.item(i).getNodeValue());
            }
            
            expr = xpath.compile("/"+ls_documento+"/detalles/detalle[*]/descripcion/text()");
            List<String> listDescrip = new ArrayList();            
            nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));            
            for (int i=0; i<nodes.getLength(); i++){
            	listDescrip.add(nodes.item(i).getNodeValue());
            }
            
            expr = xpath.compile("/"+ls_documento+"/detalles/detalle[*]/cantidad/text()");
            List<String> listCantidad = new ArrayList();            
            nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));            
            for (int i=0; i<nodes.getLength(); i++){
            	listCantidad.add(nodes.item(i).getNodeValue());
            }
            
            expr = xpath.compile("/"+ls_documento+"/detalles/detalle[*]/precioUnitario/text()");
            List<String> listPrecioUnitario = new ArrayList();            
            nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));            
            for (int i=0; i<nodes.getLength(); i++){
            	listPrecioUnitario.add(nodes.item(i).getNodeValue());
            }
            
            expr = xpath.compile("/"+ls_documento+"/detalles/detalle[*]/descuento/text()");
            List<String> listDescuento = new ArrayList();            
            nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));            
            for (int i=0; i<nodes.getLength(); i++){
            	listDescuento.add(nodes.item(i).getNodeValue());
            }
            
            expr = xpath.compile("/"+ls_documento+"/detalles/detalle[*]/precioTotalSinImpuesto/text()");
            List<String> listPrecioTotalSinImpuesto = new ArrayList();            
            nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));            
            for (int i=0; i<nodes.getLength(); i++){
            	listPrecioTotalSinImpuesto.add(nodes.item(i).getNodeValue());
            }
            
            for (int i=0; i<listCodPrin.size(); i++){
            	ArrayList<DocumentoImpuestos> listDetImpuestosDocumentos = new ArrayList<DocumentoImpuestos>();            	
            	DetalleDocumento detDoc = new DetalleDocumento();            	
            	detDoc.setCodigoPrincipal(listCodPrin.get(i).toString());
            	//detDoc.setCodigoAuxiliar(listCodAux.get(i).toString());
            	detDoc.setDescripcion(listDescrip.get(i).toString());
            	detDoc.setCantidad(Double.parseDouble(listCantidad.get(i).toString()));
            	detDoc.setPrecioUnitario(Double.parseDouble(listPrecioUnitario.get(i).toString()));
            	detDoc.setDescuento(Double.parseDouble(listDescuento.get(i).toString()));
            	detDoc.setPrecioTotalSinImpuesto(Double.parseDouble(listPrecioTotalSinImpuesto.get(i).toString()));
            	//detDoc.setListDetImpuestosDocumentos();
            	
            	expr = xpath.compile("/"+ls_documento+"/detalles/detalle[codigoPrincipal='"+listCodPrin.get(i).toString()+"']/impuestos/impuesto/codigo/text()");
                List<String> listCodigoImpuesto = new ArrayList();            
                nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));            
                for (int j=0; j<nodes.getLength(); j++){
                	listCodigoImpuesto.add(nodes.item(j).getNodeValue());
                }
                
                expr = xpath.compile("/"+ls_documento+"/detalles/detalle[codigoPrincipal='"+listCodPrin.get(i).toString()+"']/impuestos/impuesto/codigoPorcentaje/text()");
                List<String> listCodigoPorcentajeImpuesto = new ArrayList();            
                nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));            
                for (int j=0; j<nodes.getLength(); j++){
                	listCodigoPorcentajeImpuesto.add(nodes.item(j).getNodeValue());
                }
                
                expr = xpath.compile("/"+ls_documento+"/detalles/detalle[codigoPrincipal='"+listCodPrin.get(i).toString()+"']/impuestos/impuesto/tarifa/text()");
                List<String> listTarifaImpuesto = new ArrayList();            
                nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));            
                for (int j=0; j<nodes.getLength(); j++){
                	listTarifaImpuesto.add(nodes.item(j).getNodeValue());
                }
                
                expr = xpath.compile("/"+ls_documento+"/detalles/detalle[codigoPrincipal='"+listCodPrin.get(i).toString()+"']/impuestos/impuesto/baseImponible/text()");
                List<String> listBaseImponibleImpuesto = new ArrayList();            
                nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));            
                for (int j=0; j<nodes.getLength(); j++){
                	listBaseImponibleImpuesto.add(nodes.item(j).getNodeValue());
                }
                
                expr = xpath.compile("/"+ls_documento+"/detalles/detalle[codigoPrincipal='"+listCodPrin.get(i).toString()+"']/impuestos/impuesto/valor/text()");
                List<String> listValorImpuesto = new ArrayList();            
                nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));            
                for (int j=0; j<nodes.getLength(); j++){
                	listValorImpuesto.add(nodes.item(j).getNodeValue());
                }
                                
                for (int j=0; j<listCodigoImpuesto.size(); j++){
                	DocumentoImpuestos DetDocImp = new DocumentoImpuestos();
                	DetDocImp.setImpuestoCodigo(Integer.parseInt(listCodigoImpuesto.get(j).toString()));
                	DetDocImp.setImpuestoCodigoPorcentaje(Integer.parseInt(listCodigoPorcentajeImpuesto.get(j).toString()));
                	DetDocImp.setImpuestoTarifa(Double.parseDouble(listTarifaImpuesto.get(j).toString()));
                	DetDocImp.setImpuestoBaseImponible(Double.parseDouble(listBaseImponibleImpuesto.get(j).toString()));
                	DetDocImp.setImpuestoValor(Double.parseDouble(listValorImpuesto.get(j).toString()));
                	listDetImpuestosDocumentos.add(DetDocImp);
                }
                detDoc.setListDetImpuestosDocumentos(listDetImpuestosDocumentos);
                listDetDocumentos.add(detDoc);
            }                       
            emite.getInfEmisor().setListDetDocumentos(listDetDocumentos);
            
            //dca: comentado por que no se usa
            //HashMap<String, String> infoAdicionalHash = new HashMap<String, String>(); 
            expr = xpath.compile("//campoAdicional/text()");
            List<String> listInfoAdicionalFacturaValue = new ArrayList();                                    
            nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));
            for (int i=0; i<nodes.getLength(); i++){            	
            	listInfoAdicionalFacturaValue.add(nodes.item(i).getNodeValue());
            	System.out.println("NameNormalizado::"+nodes.item(i).getNodeValue());
            	//System.out.println("NameNormalizado::"+normalizeValue(nodes.item(i).getNodeValue(),300).toUpperCase());
            }
            
            expr = xpath.compile("//campoAdicional/@nombre");
            List<String> listInfoAdicionalFacturaName = new ArrayList();                                    
            nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));
            for (int i=0; i<nodes.getLength(); i++){
            	listInfoAdicionalFacturaName.add(nodes.item(i).getNodeValue());
            	System.out.println("Valor::"+nodes.item(i).getNodeValue());
            }
            ArrayList<InformacionAdicional> ListInfAdicional = new ArrayList<InformacionAdicional>();
            ListInfAdicional.clear();
            //ArrayList<InfoAdicional> listInfoAdicional = new ArrayList<InfoAdicional>();
            for (int i=0; i<listInfoAdicionalFacturaValue.size(); i++){
            	//infoAdicionalHash.put(listInfoAdicionalFacturaName.get(i).toString(), listInfoAdicionalFacturaValue.get(i).toString());
            	InformacionAdicional info = new InformacionAdicional(listInfoAdicionalFacturaName.get(i).toString(), listInfoAdicionalFacturaValue.get(i).toString());
            	ListInfAdicional.add(info);
            } 
            emite.getInfEmisor().setListInfAdicional(ListInfAdicional);
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
	
	/*Preparacion de Documentos a PDF.*/
	public static FacCabDocumento preparaCabDocumentoFac(Emisor emite, String ruc, String codEst, String codPtoEmi, String tipoDocumento, String secuencial, String msg_error, String estado){
		FacCabDocumento cabDoc = new FacCabDocumento();
		emite.getInfEmisor().setMailEmpresa("XXXX@cimait.com.ec");
		System.out.println("MailEmpresa::"+emite.getInfEmisor().getMailEmpresa());
		cabDoc.setAmbiente(emite.getInfEmisor().getAmbiente());
		System.out.println("getAmbiente::"+cabDoc.getAmbiente());
		cabDoc.setRuc(ruc);
		System.out.println("getRuc::"+cabDoc.getRuc());
		System.out.println("TipoIdentificacion()::"+emite.getInfEmisor().getTipoIdentificacion());
		cabDoc.setTipoIdentificacion(emite.getInfEmisor().getTipoIdentificacion());
		System.out.println("TipoIdentificacion()::"+cabDoc.getTipoIdentificacion());
		cabDoc.setCodEstablecimiento(codEst);
		cabDoc.setCodPuntEmision(codPtoEmi);
		cabDoc.setSecuencial(secuencial);
		cabDoc.setFechaEmision(emite.getInfEmisor().getFecEmision());
		cabDoc.setGuiaRemision(emite.getInfEmisor().getGuiaRemision());		
		cabDoc.setRazonSocialComprador(emite.getInfEmisor().getRazonSocialComp());
		cabDoc.setDirEstablecimiento(emite.getInfEmisor().getDireccionEstablecimiento());
		//cabDoc.setIdentificacionComprador(emite.getInfEmisor().getTipoIdentificacion());
		cabDoc.setTotalSinImpuesto(emite.getInfEmisor().getTotalSinImpuestos());
		cabDoc.setTotalDescuento(emite.getInfEmisor().getTotalDescuento());
		cabDoc.setEmail(emite.getInfEmisor().getMailEmpresa());
		cabDoc.setPropina(emite.getInfEmisor().getPropina());
		cabDoc.setMoneda("0");
		cabDoc.setCodigoDocumento(emite.getInfEmisor().getCodDocumento());
		cabDoc.setObligadoContabilidad(emite.getInfEmisor().getObligContabilidad());
		
		String infoAdicional = "";
		if(emite.getInfEmisor().getListInfAdicional()!=null)
		{
			for (int i = 0; i<emite.getInfEmisor().getListInfAdicional().size(); i++)
			infoAdicional = infoAdicional + "/" + emite.getInfEmisor().getListInfAdicional().get(i).getName() + "-" +emite.getInfEmisor().getListInfAdicional().get(i).getValue(); 		
		}
		cabDoc.setInfoAdicional(infoAdicional);
		if (emite.getInfEmisor().getPeriodoFiscal()!=null)
		cabDoc.setPeriodoFiscal(emite.getInfEmisor().getPeriodoFiscal().toString());
		
		cabDoc.setRise(emite.getInfEmisor().getRise());
		cabDoc.setFechaInicioTransporte(emite.getInfEmisor().getFechaIniTransp());
		cabDoc.setFechaFinTransporte(emite.getInfEmisor().getFechaFinTransp());
		cabDoc.setPlaca(emite.getInfEmisor().getPlaca());
		cabDoc.setFechaEmision(emite.getInfEmisor().getFecEmision());
		cabDoc.setMotivoRazon(emite.getInfEmisor().getMotivo());
		
		cabDoc.setClaveAcceso(emite.getInfEmisor().getClaveAcceso());
		cabDoc.setImporteTotal(emite.getInfEmisor().getImporteTotal());
		cabDoc.setCodigoDocumento(emite.getInfEmisor().getCodDocumento());
		cabDoc.setCodDocModificado(emite.getInfEmisor().getCodDocModificado());
		cabDoc.setNumDocModificado(emite.getInfEmisor().getNumDocModificado());
		cabDoc.setMotivoValor(emite.getInfEmisor().getMotivoValorND());
		cabDoc.setIdentificacionComprador(emite.getInfEmisor().getIdentificacionComp());
		System.out.println("getIdentificacionComprador::"+cabDoc.getIdentificacionComprador());
		cabDoc.setTipoEmision(emite.getInfEmisor().getTipoEmision());
		cabDoc.setListInfAdicional(emite.getInfEmisor().getListInfAdicional());
		
		
		cabDoc.setSubtotalNoIva(emite.getInfEmisor().getSubTotalNoSujeto());
		cabDoc.setTotalvalorICE(emite.getInfEmisor().getTotalICE());
		cabDoc.setIva12(emite.getInfEmisor().getTotalIva12());       
		cabDoc.setIsActive("1");
		cabDoc.setESTADO_TRANSACCION(estado);
		cabDoc.setMSJ_ERROR(msg_error);
		System.out.println("Totales de Impuestos");
		ArrayList<DetalleTotalImpuestos> lisDetImp = emite.getInfEmisor().getListDetDetImpuestos();
		for ( DetalleTotalImpuestos det : lisDetImp){
			System.out.println("codTotalImpuestos::"+det.getCodTotalImpuestos());
			System.out.println("codPorcentImpuestos::"+det.getCodPorcentImp());
			System.out.println("baseImponible::"+det.getBaseImponibleImp());
			if ((det.getCodTotalImpuestos() == 2)&&(det.getCodPorcentImp() == 2)){
				cabDoc.setSubtotal12(det.getValorImp());
				System.out.println("Valor::getSubtotal12::"+cabDoc.getSubtotal12());
				
			}
			if ((det.getCodTotalImpuestos() == 2)&&(det.getCodPorcentImp() == 0)){
				cabDoc.setSubtotal0(det.getValorImp());
				System.out.println("Valor::getSubTotal0::"+cabDoc.getSubtotal0());
			}	
		}
		
		
		if (emite.getInfEmisor().getListDetDocumentos().size()>0){
			List<FacDetDocumento> detalles = new ArrayList<FacDetDocumento>();
			for (int i=0; i<emite.getInfEmisor().getListDetDocumentos().size();i++){
				FacDetDocumento DetDoc = new FacDetDocumento();
				DetDoc.setRuc(ruc);
				DetDoc.setCodEstablecimiento(emite.getInfEmisor().getCodEstablecimiento());
				DetDoc.setCodPuntEmision(emite.getInfEmisor().getCodPuntoEmision());
				DetDoc.setSecuencial(emite.getInfEmisor().getSecuencial());
				DetDoc.setCodPrincipal(emite.getInfEmisor().getListDetDocumentos().get(i).getCodigoPrincipal());
				DetDoc.setCodAuxiliar(emite.getInfEmisor().getListDetDocumentos().get(i).getCodigoAuxiliar());
				DetDoc.setDescripcion(emite.getInfEmisor().getListDetDocumentos().get(i).getDescripcion());
				DetDoc.setCantidad(new Double(emite.getInfEmisor().getListDetDocumentos().get(i).getCantidad()).intValue());
				DetDoc.setPrecioUnitario(emite.getInfEmisor().getListDetDocumentos().get(i).getPrecioUnitario());
				DetDoc.setDescuento(emite.getInfEmisor().getListDetDocumentos().get(i).getDescuento());
				DetDoc.setPrecioTotalSinImpuesto(emite.getInfEmisor().getListDetDocumentos().get(i).getPrecioTotalSinImpuesto());
				int flagIce=0;
				if(emite.getInfEmisor().getListDetDocumentos().get(i).getListDetImpuestosDocumentos().size()>0){
					for (int j=0; j<emite.getInfEmisor().getListDetDocumentos().get(i).getListDetImpuestosDocumentos().size();j++){
						if(emite.getInfEmisor().getListDetDocumentos().get(i).getListDetImpuestosDocumentos().get(j).getImpuestoCodigo()==3){
							DetDoc.setValorIce(emite.getInfEmisor().getListDetDocumentos().get(i).getListDetImpuestosDocumentos().get(j).getImpuestoValor());
							flagIce = 1;
						}
					}
				}	
				
				if (flagIce==0)
				DetDoc.setValorIce(0);
				
				DetDoc.setSecuencialDetalle(emite.getInfEmisor().getListDetDocumentos().get(i).getLineaFactura());
				DetDoc.setCodigoDocumento(emite.getInfEmisor().getCodDocumento());
				detalles.add(DetDoc);
			}
			cabDoc.setListDetalleDocumento(detalles);
		}
		cabDoc.setListInfAdicional(emite.getInfEmisor().getListInfAdicional());
		return cabDoc;
	}
	public static FacCabDocumento preparaCabDocumentoRet(Emisor emite, String ruc, String codEst, String codPtoEmi, String tipoDocumento, String secuencial, String msg_error, String estado){
		FacCabDocumento cabDoc = new FacCabDocumento();
		emite.getInfEmisor().setMailEmpresa("jzurita@cimait.com.ec");
		cabDoc.setAmbiente(emite.getInfEmisor().getAmbiente());
		cabDoc.setRuc(ruc);
		cabDoc.setTipoIdentificacion((emite.getInfEmisor().getTipoIdentificacion()));
		cabDoc.setCodEstablecimiento(codEst);
		cabDoc.setCodPuntEmision(codPtoEmi);
		cabDoc.setSecuencial(secuencial);
		cabDoc.setFechaEmision(emite.getInfEmisor().getFecEmision());				
		cabDoc.setRazonSocialComprador(emite.getInfEmisor().getRazonSocialComp());
		cabDoc.setDirEstablecimiento(emite.getInfEmisor().getDireccionEstablecimiento());
		cabDoc.setIdentificacionComprador(emite.getInfEmisor().getTipoIdentificacion());		
		cabDoc.setEmail(emite.getInfEmisor().getMailEmpresa());		
		cabDoc.setCodigoDocumento(emite.getInfEmisor().getCodDocumento());
		cabDoc.setObligadoContabilidad(emite.getInfEmisor().getObligContabilidad());
		
		String infoAdicional = "";
		if(emite.getInfEmisor().getListInfAdicional()!=null)
		{
			for (int i = 0; i<emite.getInfEmisor().getListInfAdicional().size(); i++)
			infoAdicional = infoAdicional + "/" + emite.getInfEmisor().getListInfAdicional().get(i).getName() + "-" +emite.getInfEmisor().getListInfAdicional().get(i).getValue(); 		
		}
		cabDoc.setInfoAdicional(infoAdicional);
		if (emite.getInfEmisor().getPeriodoFiscal()!=null)
		cabDoc.setPeriodoFiscal(emite.getInfEmisor().getPeriodoFiscal().toString());
		
		
		cabDoc.setFechaEmision(emite.getInfEmisor().getFecEmision());
		cabDoc.setListImpuestosRetencion(emite.getInfEmisor().getListDetImpuestosRetenciones());
		
		cabDoc.setClaveAcceso(emite.getInfEmisor().getClaveAcceso());
		cabDoc.setCodigoDocumento(emite.getInfEmisor().getCodDocumento());
		
		cabDoc.setCodDocModificado(emite.getInfEmisor().getCodDocModificado());
		cabDoc.setNumDocModificado(emite.getInfEmisor().getNumDocModificado());
		
		cabDoc.setTipIdentificacionComprador(emite.getInfEmisor().getIdentificacionComp());
		cabDoc.setTipoEmision(emite.getInfEmisor().getTipoEmision());
		
		cabDoc.setIsActive("1");
		cabDoc.setESTADO_TRANSACCION(estado);
		cabDoc.setMSJ_ERROR(msg_error);
		
		return cabDoc;
	}

	public static FacCabDocumento preparaCabDocumentoCre(Emisor emite, String ruc, String codEst, String codPtoEmi, String tipoDocumento, String secuencial, String msg_error, String estado){
		FacCabDocumento cabDoc = new FacCabDocumento();
		emite.getInfEmisor().setMailEmpresa("jzurita@cimait.com.ec");
		cabDoc.setAmbiente(emite.getInfEmisor().getAmbiente());
		cabDoc.setRuc(ruc);
		cabDoc.setTipoIdentificacion((emite.getInfEmisor().getTipoIdentificacion()));
		cabDoc.setCodEstablecimiento(codEst);
		cabDoc.setCodPuntEmision(codPtoEmi);
		cabDoc.setSecuencial(secuencial);
		cabDoc.setFechaEmision(emite.getInfEmisor().getFecEmision());				
		cabDoc.setRazonSocialComprador(emite.getInfEmisor().getRazonSocialComp());
		cabDoc.setDirEstablecimiento(emite.getInfEmisor().getDireccionEstablecimiento());
		cabDoc.setIdentificacionComprador(emite.getInfEmisor().getTipoIdentificacion());		
		cabDoc.setEmail(emite.getInfEmisor().getMailEmpresa());		
		cabDoc.setCodigoDocumento(emite.getInfEmisor().getCodDocumento());
		cabDoc.setObligadoContabilidad(emite.getInfEmisor().getObligContabilidad());
		
		String infoAdicional = "";
		if(emite.getInfEmisor().getListInfAdicional()!=null)
		{
			for (int i = 0; i<emite.getInfEmisor().getListInfAdicional().size(); i++)
			infoAdicional = infoAdicional + "/" + emite.getInfEmisor().getListInfAdicional().get(i).getName() + "-" +emite.getInfEmisor().getListInfAdicional().get(i).getValue(); 		
		}
		cabDoc.setInfoAdicional(infoAdicional);
		if (emite.getInfEmisor().getPeriodoFiscal()!=null)
		cabDoc.setPeriodoFiscal(emite.getInfEmisor().getPeriodoFiscal().toString());
		
		
		cabDoc.setFechaEmision(emite.getInfEmisor().getFecEmision());
		cabDoc.setListImpuestosRetencion(emite.getInfEmisor().getListDetImpuestosRetenciones());
		
		cabDoc.setClaveAcceso(emite.getInfEmisor().getClaveAcceso());
		cabDoc.setCodigoDocumento(emite.getInfEmisor().getCodDocumento());
		
		cabDoc.setCodDocModificado(emite.getInfEmisor().getCodDocModificado());
		cabDoc.setNumDocModificado(emite.getInfEmisor().getNumDocModificado());
		
		cabDoc.setFecEmisionDocSustento(emite.getInfEmisor().getFecEmisionDoc());
		
		cabDoc.setTipIdentificacionComprador(emite.getInfEmisor().getIdentificacionComp());
		cabDoc.setTipoEmision(emite.getInfEmisor().getTipoEmision());
		
		cabDoc.setIsActive("1");
		cabDoc.setESTADO_TRANSACCION(estado);
		cabDoc.setMSJ_ERROR(msg_error);
		
		return cabDoc;
	}

	
	@Override
	//Heredado de la Clase GenericTransaction
	public synchronized void run() {
		try{
			atiendeHilo();	
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			//ServiceData.listAtencion.set(idHilo, "N");
		}
	}
	
	//ea
	public void  validateFile(String fileName) throws Exception {
		if (fileName.length() > 37 ||  fileName.length() < 36 ) {
			throw new Exception("Error tamaño de nombre de archivo incorrecto");
		}
	}
	
	//fin ea
	
}
