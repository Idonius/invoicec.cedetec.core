package com.cimait.invoicec.core;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import com.cimait.invoicec.bean.Emisor;
import com.cimait.invoicec.db.DBDataSource;
import com.sun.DAO.DetalleDocumento;
import com.sun.DAO.DetalleImpuestosRetenciones;
import com.sun.DAO.DetalleTotalImpuestos;
import com.sun.DAO.DocumentoImpuestos;
import com.sun.DAO.InformacionAdicional;
import com.sun.comprobantes.util.EmailSender;
import com.sun.directory.examples.FileDemo;
import com.sun.directory.examples.InfoEmpresa;
import com.tradise.reportes.entidades.FacCabDocumento;
import com.tradise.reportes.entidades.FacDetDocumento;
import com.util.util.key.Environment;
import com.util.util.key.Util;
	
public class ServiceData extends com.util.util.key.GenericTransaction {

	public static String classReference = "ServiceData";
	public static StringBuilder SBmsj = null;
	public static File fxml = null;
	public static String fileBackup = null;
	public static int contador;
	public static int numHilo;
	
	public static String emailHost = null;
	public static String emailFrom = null;
	public static String emailTo = null;
	public static String emailSubject = null;
	public static String emailMensaje = null;	
	public static String emailHelpDesk = null;
	public static File[] contenido;
	public static List listErrores = null;
	public static List listWarning = null;
	public static List listErroresEstados = null;
	public static List listWarningEstados = null;
	public InfoEmpresa InforEmpresa = null;
	public static List listAtencion = null;
	public static String databaseMotor=null;
 
	private static HashMap<String,Object> invoice;
	private static Logger LOGGER = Logger.getLogger(ServiceData.class);

	
	public static DBDataSource dbDS = null;
		
	public static void iniServiceData(){
		//Host Mail Server
		emailHost = Environment.c.getString("facElectronica.alarm.email.host");
		//Enviado desde
		emailFrom = Environment.c.getString("facElectronica.alarm.email.sender");
		//Enviado para
		emailTo = Environment.c.getString("facElectronica.alarm.email.receivers-list");
		//Asunto
		emailSubject = Environment.c.getString("facElectronica.alarm.email.subject");
		//Email HelpDesk
		emailHelpDesk = Environment.c.getString("facElectronica.alarm.email.helpdesk");
		
		listErrores = Environment.c.getList("facElectronica.general.EMISION.error-wsdls.error-wsdl");		
		listWarning = Environment.c.getList("facElectronica.general.EMISION.warning-wsdls.warning-wsdl");
		listErroresEstados = Environment.c.getList("facElectronica.general.EMISION.error-wsdls.ESTADO");
		listWarningEstados = Environment.c.getList("facElectronica.general.EMISION.warning-wsdls.ESTADO");
		
	}
		
	public synchronized static void main( String args[] ) throws Exception {
		// setLogger();
		
		Properties props = new Properties();
		props.load(new FileInputStream("log4j.properties"));
		PropertyConfigurator.configure(props);
		
		
		SBmsj = new StringBuilder();
		// Archivo de Configuracion
		String name_xml = "facturacion.xml";
		LOGGER.info("Iniciando ServiceData...");
		try {
			LOGGER.info("Cargando parametros de ejecucion...");
			Environment.setConfiguration(name_xml);
			Environment.setCtrlFile();
			//Environment.setLogger(Util.log_control);
			ServiceData.iniServiceData();
			
			//pool connection de Db.
			LOGGER.info("Iniciando Conexion a Base de Datos...");
			ServiceData.databaseMotor = "PostgreSQL"; 
		} catch (Exception ex) {
			SBmsj.append(classReference
					+ "::main>>FacturacionElectronica.Service::main::Proceso de Carga de Archivo Xml Configuraciones::::"
					+ ". Proceso de Emision de Documentos no se levanto.");
			LOGGER.error(SBmsj.toString());
//			int li_envio = enviaEmail("message_error", emite, SBmsj.toString(),
//					"", null, null);
			// log.error(SBmsj.toString());
			throw new Exception(SBmsj.toString());
		}
		
		int numThreads = 5; // TODO:captura de facturacion.xml
		// pool de threads
		ExecutorService poolThreads = Executors.newFixedThreadPool(numThreads);
		String ruc = args[0];

		// envia un correo por el error de RUC Null y/o supera el tama�o;
		if ((ruc == null) || ruc.equals("") || (ruc.length() > 13)) {
			SBmsj.append("Error::" + classReference
					+ ":: Debe enviar el parametro de Ruc Correcto. Ruc->"
					+ ruc + ". Proceso de Emision de Documentos no se levanto.");
			// int li_envio = enviaEmail("message_error", emite,
			// SBmsj.toString(),
			// "", "", null);
			// log.error(SBmsj.toString())
			LOGGER.error(SBmsj.toString());;
			throw new Exception(SBmsj.toString());
		}

		ArrayList<Emisor> emite = new ArrayList<Emisor>();
		emite.add(new Emisor());
		int indexEmite = 0;
		// InformacionTributaria infTribAdic = new InformacionTributaria();
		InfoEmpresa infEmp = new InfoEmpresa();

				
		if (!emite.get(indexEmite).existeEmpresa(ruc)) {
			String mensaje = " Empresa no existe o no se encuentra Activa. Ruc->"
					+ ruc + ". Proceso de Emision de Documentos no se levanto.";
			LOGGER.error(mensaje);
			throw new Exception(mensaje);
		}
		
		infEmp = emite.get(indexEmite).obtieneInfoEmpresa(ruc);
		//System.out.println("Directorio::" + infEmp.getDirectorio());

		emite.clear();
		LOGGER.info("Esperando por archivos");
		
		//contador para CT 
		long tInicio = System.currentTimeMillis();
		
		
		while ((Environment.cf.readCtrl().equals("S"))) {
			try {
				String ruta = infEmp.getDirectorio();
				
				contenido = FileDemo.busqueda(ruta, ".txt");
				
				if (contenido != null && contenido.length > 0) {
					for (int i = 0; i < contenido.length; i++) {
						// Preparacion de Xml para procesar
						File fileProcesar = new File(
								contenido[i].getAbsolutePath());
						
						if (fileProcesar.renameTo(new File(infEmp.getDirRecibidos() + fileProcesar.getName()))) {
							//System.out.println("Archivo Movido::"+ infEmp.getDirRecibidos()+ fileProcesar.getName());
							LOGGER.info("Archivo "+ fileProcesar.getName() + " movido a " + infEmp.getDirRecibidos());
							fileProcesar = new File(infEmp.getDirRecibidos()+ fileProcesar.getName());
							if (fileProcesar.exists()) {
								emite.add(new Emisor());
								emite.get(indexEmite).setFileTxt(fileProcesar.getName());
								//poolThreads.execute(new ServiceDataHilo(infEmp,emite.get(indexEmite), listErrores,listWarning, listErroresEstados,listWarningEstados));
								poolThreads.execute(new ServiceDataHiloCons(infEmp,emite.get(indexEmite), listErrores,listWarning, listErroresEstados,listWarningEstados));
							} else {
								LOGGER.error("Archivo " + fileProcesar.getName() +  " a procesar no existe en " + infEmp.getDirRecibidos());
							}

						} else {
							LOGGER.error("recibidos..."+infEmp.getDirRecibidos());
							LOGGER.error("Error al mover archivo " + fileProcesar.getName() + " a " + infEmp.getDirRecibidos());
						}
						indexEmite++;
					}
					emite.clear();
					indexEmite = 0;

				} else {
					LOGGER.debug("No hay archivos que procesar...");
					new Thread().sleep(Util.timeWait);
					
					long tFin = System.currentTimeMillis();
					long tDif = tFin - tInicio;
					double tSec = tDif / 1000.0;
					//cada 5 minutos enviar reproceso de CT
					if (tSec > 40.0) { //5 min = 300 secs
						tInicio = System.currentTimeMillis();
						poolThreads.execute(new ServiceDataCT(listErrores,listWarning, listErroresEstados,listWarningEstados));
					}
				}
			} catch (Exception excep) {
				poolThreads.shutdown();
				poolThreads.awaitTermination(3000, TimeUnit.NANOSECONDS);
				excep.printStackTrace();
			}
		}
		poolThreads.shutdown();
		poolThreads.awaitTermination(3000, TimeUnit.NANOSECONDS);
		LOGGER.info("Fin ServiceData...");
	}
	
	public static void moveExist(String path, String filename, String pathMove){
	      File f = new File(path+filename);				      
		  if(f.exists()){
			  f.renameTo(new File(pathMove + filename));
		  }
		  f = new File(path+filename.replace(".xml", "_backup.xml"));
		  if(f.exists()){							  
			  f.renameTo(new File(pathMove + filename.replace(".xml", "_backup.xml")));
		  }
	}
	
	public static void leerXml(String nameXml, Emisor emite) {
		if (emite.getInfEmisor().getCodDocumento().equals("01"))
			leerFacturaXml(nameXml, emite);
		if (emite.getInfEmisor().getCodDocumento().equals("07"))
			leerComprobanteRetXml(nameXml, emite);
		if (emite.getInfEmisor().getCodDocumento().equals("04"))
			leerFacturaNotaCreditoXml(nameXml, emite);
		if (emite.getInfEmisor().getCodDocumento().equals("05"))
			leerFacturaNotaDebitoXml(nameXml, emite);
	}
	
	public static void leerComprobanteRetXml(String nameXml, Emisor emite) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder;
        Document doc = null;
        String ls_documento = "comprobanteRetencion";
        String ls_infoDocumento = "infoCompRetencion";
        try {
            builder = factory.newDocumentBuilder();
            doc = builder.parse(nameXml);
            System.out.println("File>>"+nameXml); 
            // Create XPathFactory object
            XPathFactory xpathFactory = XPathFactory.newInstance();
 
            // Create XPath object
            XPath xpath = xpathFactory.newXPath();
            
            //infoTributaria            
            XPathExpression expr = xpath.compile("/"+ls_documento+"/infoTributaria/tipoEmision/text()");
            emite.getInfEmisor().setTipoEmision((String) expr.evaluate(doc, XPathConstants.STRING));
            /*
            expr = xpath.compile("/"+ls_documento+"/infoTributaria/ambiente/text()");
            emite.getInfEmisor().setAmbiente(Integer.parseInt((String)expr.evaluate(doc, XPathConstants.STRING)));*/
            emite.getInfEmisor().setAmbiente(1);
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
            
            //infoFactura
            expr = xpath.compile("/"+ls_documento+"/"+ls_infoDocumento+"/fechaEmision/text()");
            emite.getInfEmisor().setFecEmision((String)expr.evaluate(doc, XPathConstants.STRING));
            
            expr = xpath.compile("/"+ls_documento+"/"+ls_infoDocumento+"/dirEstablecimiento/text()");
            emite.getInfEmisor().setDireccionEstablecimiento((String) expr.evaluate(doc, XPathConstants.STRING));
            
            /*
            expr = xpath.compile("/"+ls_documento+"/"+ls_infoDocumento+"/contribuyenteEspecial/text()");
            emite.getInfEmisor().setContribEspecial(Integer.parseInt((String) expr.evaluate(doc, XPathConstants.STRING)));
            */
            expr = xpath.compile("/"+ls_documento+"/"+ls_infoDocumento+"/obligadoContabilidad/text()");
            emite.getInfEmisor().setObligContabilidad((String) expr.evaluate(doc, XPathConstants.STRING));
            
            expr = xpath.compile("/"+ls_documento+"/"+ls_infoDocumento+"/tipoIdentificacionSujetoRetenido/text()");
            emite.getInfEmisor().setTipoIdentificacion((String) expr.evaluate(doc, XPathConstants.STRING));
                       
            expr = xpath.compile("/"+ls_documento+"/"+ls_infoDocumento+"/razonSocialSujetoRetenido/text()");
            emite.getInfEmisor().setRazonSocialComp((String) expr.evaluate(doc, XPathConstants.STRING));
            
            expr = xpath.compile("/"+ls_documento+"/"+ls_infoDocumento+"/identificacionSujetoRetenido/text()");
            emite.getInfEmisor().setIdentificacionComp((String) expr.evaluate(doc, XPathConstants.STRING));
            
            expr = xpath.compile("/"+ls_documento+"/"+ls_infoDocumento+"/periodoFiscal/text()");
            emite.getInfEmisor().setPeriodoFiscal((String) expr.evaluate(doc, XPathConstants.STRING));
            
                        
            
            expr = xpath.compile("/"+ls_documento+"/impuestos/impuesto[*]/codigo/text()");
            List<String> listCodigo = new ArrayList();            
            NodeList nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));            
            for (int i=0; i<nodes.getLength(); i++){
            	System.out.println("codigo::"+nodes.item(i).getNodeValue());
            	listCodigo.add(nodes.item(i).getNodeValue());            	
            }
            
            expr = xpath.compile("/"+ls_documento+"/impuestos/impuesto[*]/codigoRetencion/text()");
            List<String> listCodigoRetencion = new ArrayList();            
            nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));            
            for (int i=0; i<nodes.getLength(); i++){
            	System.out.println("codigoRetencion::"+nodes.item(i).getNodeValue());
            	listCodigoRetencion.add(nodes.item(i).getNodeValue());
            }
            
            expr = xpath.compile("/"+ls_documento+"/impuestos/impuesto[*]/baseImponible/text()");
            List<String> listBaseImponible = new ArrayList();            
            nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));            
            for (int i=0; i<nodes.getLength(); i++){
            	System.out.println("baseImponible::"+nodes.item(i).getNodeValue());
            	listBaseImponible.add(nodes.item(i).getNodeValue());
            }
            
            expr = xpath.compile("/"+ls_documento+"/impuestos/impuesto[*]/porcentajeRetener/text()");
            List<String> listPorcentajeRetener = new ArrayList();            
            nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));            
            for (int i=0; i<nodes.getLength(); i++){
            	System.out.println("porcentajeRetener::"+nodes.item(i).getNodeValue());
            	listPorcentajeRetener.add(nodes.item(i).getNodeValue());
            }
            
            expr = xpath.compile("/"+ls_documento+"/impuestos/impuesto[*]/valorRetenido/text()");
            List<String> listValorRetenido = new ArrayList();            
            nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));            
            for (int i=0; i<nodes.getLength(); i++){
            	System.out.println("valorRetenido::"+nodes.item(i).getNodeValue());
            	listValorRetenido.add(nodes.item(i).getNodeValue());
            }
            
            expr = xpath.compile("/"+ls_documento+"/impuestos/impuesto[*]/codDocSustento/text()");
            List<String> listCodDocSustento = new ArrayList();            
            nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));            
            for (int i=0; i<nodes.getLength(); i++){
            	System.out.println("codDocSustento::"+nodes.item(i).getNodeValue());
            	listCodDocSustento.add(nodes.item(i).getNodeValue());
            }
            
            expr = xpath.compile("/"+ls_documento+"/impuestos/impuesto[*]/numDocSustento/text()");
            List<String> listNumDocSustento = new ArrayList();            
            nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));            
            for (int i=0; i<nodes.getLength(); i++){
            	System.out.println("numDocSustento::"+nodes.item(i).getNodeValue());
            	listNumDocSustento.add(nodes.item(i).getNodeValue());
            }
            
            expr = xpath.compile("/"+ls_documento+"/impuestos/impuesto[*]/fechaEmisionDocSustento/text()");
            List<String> listFechaEmisionDocSustento = new ArrayList();            
            nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));            
            for (int i=0; i<nodes.getLength(); i++){
            	System.out.println("fechaEmisionDocSustento::"+nodes.item(i).getNodeValue());
            	listFechaEmisionDocSustento.add(nodes.item(i).getNodeValue());
            }
            
            ArrayList<DetalleImpuestosRetenciones> listDetDetImpuestosRet = new ArrayList<DetalleImpuestosRetenciones>();             
            for (int i=0; i<listCodigo.size(); i++){
            	DetalleImpuestosRetenciones detImp = new DetalleImpuestosRetenciones();
            	detImp.setCodigo(Integer.parseInt(listCodigo.get(i).toString()));
            	detImp.setCodigoRetencion(Integer.parseInt(listCodigoRetencion.get(i).toString()));
            	detImp.setBaseImponible(Double.parseDouble(listBaseImponible.get(i).toString()));
            	detImp.setPorcentajeRetener(Integer.parseInt(listPorcentajeRetener.get(i).toString()));
            	detImp.setValorRetenido(Double.parseDouble(listValorRetenido.get(i).toString()));
            	detImp.setCodDocSustento(listCodDocSustento.get(i).toString());
            	if (listNumDocSustento.size()>0){
            		detImp.setNumDocSustento(listNumDocSustento.get(i).toString());
            	}
            	if (listFechaEmisionDocSustento.size()>0){
            		detImp.setFechaEmisionDocSustento(listFechaEmisionDocSustento.get(i).toString());
            	}
            	listDetDetImpuestosRet.add(detImp);
            }
            
            emite.getInfEmisor().setListDetImpuestosRetenciones(listDetDetImpuestosRet);
            
            
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
	
	public static void leerFacturaNotaCreditoXml(String nameXml, Emisor emite) {
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
	
	public static void leerFacturaNotaDebitoXml(String nameXml, Emisor emite) {
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

	
	/*
	private static String normalizeValue(String value, int maxURLLength) {
		String inputValue = value;
		String ret = value;
		if (inputValue != null) {

			ret = Normalizer.normalize(inputValue.subSequence(0, inputValue.length()), Normalizer.Form.NFKD).replaceAll("[^\\p{ASCII}]+",
					"");

			ret = ret.replaceAll("[^a-zA-Z0-9]", "");

			ret = ret.replaceAll("-+", "");

			if (ret.length() > maxURLLength) {
				ret = ret.substring(0, maxURLLength);
			}

			ret = ret.toLowerCase();
		}
		return ret;
	}*/
	public static FacCabDocumento preparaCabDocumentoFac(com.cimait.invoicec.bean.Emisor emite, String ruc, String codEst, String codPtoEmi, String tipoDocumento, String secuencial, String msg_error, String estado){
		FacCabDocumento cabDoc = new FacCabDocumento();
		emite.getInfEmisor().setMailEmpresa("jzurita@cimait.com.ec");
		cabDoc.setAmbiente(emite.getInfEmisor().getAmbiente());
		cabDoc.setRuc(ruc);
		cabDoc.setTipoIdentificacion(emite.getInfEmisor().getTipoIdentificacion());
		cabDoc.setCodEstablecimiento(codEst);
		cabDoc.setCodPuntEmision(codPtoEmi);
		cabDoc.setSecuencial(secuencial);
		cabDoc.setFechaEmision(emite.getInfEmisor().getFecEmision());
		cabDoc.setGuiaRemision(emite.getInfEmisor().getGuiaRemision());		
		cabDoc.setRazonSocialComprador(emite.getInfEmisor().getRazonSocialComp());
		cabDoc.setDirEstablecimiento(emite.getInfEmisor().getDireccionEstablecimiento());
		cabDoc.setIdentificacionComprador(emite.getInfEmisor().getTipoIdentificacion());
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
		//cabDoc.setIdentificacionDestinatario(emite.getInfEmisor().get);
		//cabDoc.setRazonSocialDestinatario(emite.getInfEmisor().getra);
		/*ps.setString(26, cabDoc.getDireccionDestinatario());
	       ps.setString(27, cabDoc.getMotivoTraslado());
	       ps.setString(28, cabDoc.getDocAduaneroUnico());
	       ps.setString(29, cabDoc.getCodEstablecimientoDest());
	       ps.setString(30, cabDoc.getRuta());
	       ps.setString(31, cabDoc.getCodDocSustento());
	       ps.setString(32, cabDoc.getNumDocSustento());
	       ps.setString(33, cabDoc.getNumAutDocSustento());
	       ps.setDate(34, (Date)cabDoc.getFecEmisionDocSustento());
	     */
		//cabDoc.setAutorizacion(emite.getInfEmisor().getNumeroAutorizacion());
		//cabDoc.setFechaautorizacion(emite.getInfEmisor().getFechaAutorizacion());
		cabDoc.setClaveAcceso(emite.getInfEmisor().getClaveAcceso());
		cabDoc.setImporteTotal(emite.getInfEmisor().getImporteTotal());
		cabDoc.setCodigoDocumento(emite.getInfEmisor().getCodDocumento());
		cabDoc.setCodDocModificado(emite.getInfEmisor().getCodDocModificado());
		cabDoc.setNumDocModificado(emite.getInfEmisor().getNumDocModificado());
		cabDoc.setMotivoValor(emite.getInfEmisor().getMotivoValorND());
		cabDoc.setTipIdentificacionComprador(emite.getInfEmisor().getIdentificacionComp());
		cabDoc.setTipoEmision(emite.getInfEmisor().getTipoEmision());
		cabDoc.setListInfAdicional(emite.getInfEmisor().getListInfAdicional());
		/*              
       ps.setString(45, cabDoc.getPartida());
       */
		cabDoc.setSubtotal12(emite.getInfEmisor().getSubTotal12());
		cabDoc.setSubtotal0(emite.getInfEmisor().getSubTotal0());
		cabDoc.setSubtotalNoIva(emite.getInfEmisor().getSubTotalNoSujeto());
		cabDoc.setTotalvalorICE(emite.getInfEmisor().getTotalICE());
		cabDoc.setIva12(emite.getInfEmisor().getTotalIva12());       
		cabDoc.setIsActive("1");
		cabDoc.setESTADO_TRANSACCION(estado);
		cabDoc.setMSJ_ERROR(msg_error);
		
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
       /*
       String documento = "";
       if (cabDoc.getCodigoDocumento().trim().equals("01")) documento = "FACTURA";
       if (cabDoc.getCodigoDocumento().trim().equals("04")) documento = "NOTA DE CREDITO";
       if (cabDoc.getCodigoDocumento().trim().equals("05")) documento = "NOTA DE DEBITO";
       if (cabDoc.getCodigoDocumento().trim().equals("06")) documento = "GUIA DE REMISION";
       if (cabDoc.getCodigoDocumento().trim().equals("07")) documento = "COMPORBANTE DE RETENECION";
 
		*/
		cabDoc.setListInfAdicional(emite.getInfEmisor().getListInfAdicional());
		return cabDoc;
	}
	public static FacCabDocumento preparaCabDocumentoRet(com.cimait.invoicec.bean.Emisor emite, String ruc, String codEst, String codPtoEmi, String tipoDocumento, String secuencial, String msg_error, String estado){
		FacCabDocumento cabDoc = new FacCabDocumento();
		emite.getInfEmisor().setMailEmpresa("jzurita@cimait.com.ec");
		cabDoc.setAmbiente(emite.getInfEmisor().getAmbiente());
		cabDoc.setRuc(ruc);
		cabDoc.setTipoIdentificacion(emite.getInfEmisor().getTipoIdentificacion());
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

	public static FacCabDocumento preparaCabDocumentoCre(com.cimait.invoicec.bean.Emisor emite, String ruc, String codEst, String codPtoEmi, String tipoDocumento, String secuencial, String msg_error, String estado){
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

	
	public static void delFile(Emisor emite, String rutaFirmado, String generado, String dirNoAutorizados){
		//Eliminacion de Archivos				        		  				        		  
		  File eliminar = new File(rutaFirmado+emite.getFilexml());
  	  if (eliminar.exists()) {
  		  eliminar.delete();
  	  }
  	  File fileDel = new File(generado+emite.getFileTxt());
	  copiarXml2(fileDel.getAbsolutePath(),dirNoAutorizados+fileDel.getName());	
  	  //Eliminacion de Archivos				        		  				        		  
		  eliminar = new File(generado+emite.getFilexml());
  	  if (eliminar.exists()) {
  		  eliminar.delete();
  	  }
  	  
  	  eliminar = new File(generado+emite.getFileXmlBackup());
  	  if (eliminar.exists()) {
  		  eliminar.delete();
  	  }
  	  			            	  
  	  				            	  
  	  if (fileDel.exists()) {
  		  fileDel.delete();
  	  }
  	  System.out.println("Delete File");
	}
	public static int copiarXml(String fileName){
		try{
		  File fileOrigen = new File(fileName);
	      File fileDestino = new File(fileName.replace(".xml", "_backup.xml"));	      
	      if (fileOrigen.exists()) {	    	  
	    	  InputStream in = new FileInputStream(fileOrigen);
	    	  OutputStream out = new FileOutputStream(fileDestino);
	    	  byte[] buf = new byte[1024];int len; while ((len = in.read(buf)) > 0) {  out.write(buf, 0, len);}
	    	  in.close();
	    	  out.close();
	    	  fileBackup = fileName.replace(".xml", "_backup.xml");
	    	  return 1;
	    	  
	      }
	      else{
	    	  return 0;
	      }
		}catch(IOException e){
			return -1;
		}
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
	
	public static int moveFile(String absolutePathOrigen, String pathDestino){		 
   	 try{
	 File dataInputFile = new File(absolutePathOrigen); 
   	 File fileSendPath = new File(pathDestino, dataInputFile.getName());  
   	 dataInputFile.renameTo(fileSendPath);
   	 }catch(Exception e){
   		 return 0;
   	 }
   	 return 1;
	}
	
	public static ec.gob.sri.comprobantes.ws.RespuestaSolicitud solicitudRecepcion(File archivoFirmado, Emisor emi, List listErrores, List listWarn)
	{
		ec.gob.sri.comprobantes.ws.RespuestaSolicitud respuestaRecepcion = null;
		String flagErrores = "";	
		try{
			respuestaRecepcion = new ec.gob.sri.comprobantes.ws.RespuestaSolicitud();
	    	/*
			respuestaRecepcion = EnvioComprobantesWs.obtenerRespuestaEnvio(archivoFirmado, 
	    																   emi.getInfEmisor().getRuc(), 
	    																   emi.getInfEmisor().getCodDocumento(), 
	    																   emi.getInfEmisor().getClaveAcceso(), 
	    																   FormGenerales.devuelveUrlWs(new Integer(emi.getInfEmisor().getAmbiente()).toString() ,"RecepcionComprobantes"),
	    																   30000, listErrores, listWarn);
	    																   */
        }catch(Exception e){
        	flagErrores = "ERROR";    
        	System.out.println("Enviar error por contingencia"+ e.toString());
        	e.printStackTrace();
        	respuestaRecepcion.setEstado("SIN-SERVICIO");        	
        	return respuestaRecepcion;
        }
		return respuestaRecepcion;		
	}

	
	
	public static int enviaEmailCliente(String ls_id_mensaje, Emisor emi, String mensaje_mail, String mensaje_error, String fileAttachXml, String fileAttachPdf, String emailCliente){
		EmailSender emSend = new EmailSender(emailHost,emailFrom);		
		emailMensaje = Environment.c.getString("facElectronica.alarm.email."+ls_id_mensaje);		
		String ambiente = Environment.c.getString("facElectronica.alarm.email.ambiente");
		String clave = Environment.c.getString("facElectronica.alarm.email.password");
		
		String user = Environment.c.getString("facElectronica.alarm.email.user");
		String subject = Environment.c.getString("facElectronica.alarm.email.subject");
		String tipo_autentificacion = Environment.c.getString("facElectronica.alarm.email.tipo_autentificacion");
		String tipoMail = Environment.c.getString("facElectronica.alarm.email.tipoMail");
		String receivers = "";		
		if (ambiente.equals("PRUEBAS")){
			receivers = Environment.c.getString("facElectronica.alarm.email.receivers-list");
		}else{
			String emailCli = emi.getInfEmisor().getMailCliente();
			if (!emailCli.equals("email@email.com")){
				  receivers = emailCliente;
			}else{
				receivers =null;			}
		}	
		if (receivers!=null){
		emSend.setPassword(clave);
		emSend.setSubject(subject);
		emSend.setUser(user);		
		emSend.setAutentificacion(tipo_autentificacion);
		emSend.setTipoMail(tipoMail);		
		emailCliente = receivers;
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
		if ((emailCliente!=null) && (emailCliente.length()>0)){
			String[] partsMail = emailCliente.split(";");
			//for(int i=0;i<partsMail.length;i++)
				//if (partsMail[i].length()>0){
				emSend.send(emailCliente
							//partsMail[i]
						, 
							subject, 
		  		  	        emailMensaje,
		  		  	        fileAttachXml,
		  		  	        fileAttachPdf);
				//}
		}
		}
		return 0;
	}	
	
	public static int enviaEmail(String ls_id_mensaje, Emisor emi, String mensaje_mail, String mensaje_error, String fileAttachXml, String fileAttachPdf){
		EmailSender emSend = new EmailSender(emailHost,emailFrom);
		emailMensaje = Environment.c.getString("facElectronica.alarm.email."+ls_id_mensaje);
		
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
			//for(int i=0;i<partsMail.length;i++)
				//if (partsMail[i].length()>0){
					emSend.send(emailTo
							    //partsMail[i]
							, 
								subject,
								emailMensaje,
			  		  	        fileAttachXml,
			  		  	        fileAttachPdf);
				//}
		}		
		return 0;
	}
	
	
	public static int enviaEmail(String ls_id_mensaje, Emisor emi, String mensaje_mail, String mensaje_error){
		EmailSender emSend = new EmailSender(emailHost,emailFrom);
		emailMensaje = Environment.c.getString("facElectronica.alarm.email."+ls_id_mensaje);
		String ambiente = Environment.c.getString("facElectronica.alarm.email.ambiente");
		String clave = Environment.c.getString("facElectronica.alarm.email.password");
		String subject = Environment.c.getString("facElectronica.alarm.email.subject");
		String tipo_autentificacion = Environment.c.getString("facElectronica.alarm.email.tipo_autentificacion");
		String tipoMail = Environment.c.getString("facElectronica.alarm.email.tipoMail");
		
		String receivers = "";
		
		receivers = Environment.c.getString("facElectronica.alarm.email.receivers-list");				
		emSend.setPassword(clave);
		emSend.setSubject(subject);
		emSend.setUser(emailFrom);
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
			for(int i=0;i<partsMail.length;i++)
				if (partsMail[i].length()>0){
					emSend.send(partsMail[i], 
								subject, 
			  		  	        emailMensaje);
				}
		}	
		
		return 0;
	}

}
