package com.cimait.invoicec.sri.document;

import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.w3c.dom.Document;

import au.com.bytecode.opencsv.CSVReader;

import com.cimait.invoicec.bean.Emisor;
import com.cimait.invoicec.db.DBDataSource;
import com.cimait.invoicec.entity.FacCabDocumentosEntity;
import com.cimait.invoicec.entity.FacDetDocumentosEntity;
import com.cimait.invoicec.entity.FacDetRetencionesEntity;
import com.cimait.invoicec.sri.schema.creditnote.NotaCredito.Detalles;
import com.cimait.invoicec.sri.schema.general.InfoTributaria;
import com.cimait.invoicec.sri.schema.invoice.Factura;
import com.cimait.invoicec.sri.schema.invoice.Factura.Detalles.Detalle;
import com.cimait.invoicec.sri.schema.invoice.Factura.InfoFactura.TotalConImpuestos.TotalImpuesto;
import com.cimait.invoicec.sri.schema.retention.ComprobanteRetencion;
import com.cimait.invoicec.sri.schema.retention.ComprobanteRetencion.Impuestos;
import com.cimait.invoicec.sri.schema.retention.ComprobanteRetencion.InfoAdicional;
import com.cimait.invoicec.sri.schema.retention.ComprobanteRetencion.InfoAdicional.CampoAdicional;
import com.cimait.invoicec.sri.schema.retention.ComprobanteRetencion.InfoCompRetencion;
import com.cimait.invoicec.sri.schema.retention.Impuesto;
import com.sun.directory.examples.ModifyDocumentAcceso;

public class SRIRetention extends SRIDocument {
	private ComprobanteRetencion document = null;
	private HashMap<String, String> extraData = null;
	
	@Override
	public void parseFile() throws IOException {
		CSVReader reader = new CSVReader(new FileReader(this.inputFile), '|');
		String[] nextLine;
		
		this.document = new ComprobanteRetencion();
		
		this.document.setId("comprobante");
		this.document.setVersion("1.0.0");
		
		//Acumular informacion adicional
		InfoAdicional infoAdicional = new InfoAdicional();
		List<CampoAdicional> lCamposAdicionales = new ArrayList<ComprobanteRetencion.InfoAdicional.CampoAdicional>();
		
		//acumular impuestos
		List<Impuesto> lImpuestos = new ArrayList<Impuesto>();
		ComprobanteRetencion.Impuestos impuestos = new ComprobanteRetencion.Impuestos();
		
		try {
			while ((nextLine = reader.readNext()) != null) {
				if (nextLine[0].equals("IT")) { // 1 - 1
					this.document.setInfoTributaria(getInfoTributaria(nextLine));
				} else if (nextLine[0].equals("CR")) { // 1 - 1
					this.document.setInfoCompRetencion(getInfoCompRetencion(nextLine));
					setExtraData(nextLine);
				} else if (nextLine[0].equals("IM")) { 
					lImpuestos.add(getImpuesto(nextLine));
				} else if (nextLine[0].equals("IA")) {
					lCamposAdicionales.add((getInfoAdicional(nextLine)));
				}
			}
			
			infoAdicional.getCampoAdicional().addAll(lCamposAdicionales);
			
			impuestos.getImpuesto().addAll(lImpuestos);

			this.document.setImpuestos(impuestos);
			if (lCamposAdicionales.size() != 0 ) { 
				this.document.setInfoAdicional(infoAdicional);
			}
		} catch (Exception e) {
			System.out.println("error " + e.getMessage());
			e.printStackTrace();
		}
		reader.close();
	}
	
	@Override
	public void setEmisor(Emisor emite){
		this.emite =emite;
		try {
			this.document.getInfoTributaria().setClaveAcceso(ModifyDocumentAcceso.generarClaveAcceso(this.emite));
			emite.getInfEmisor().setClaveAcceso(this.document.getInfoTributaria().getClaveAcceso());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void setExtraData(String[] nextLine){
		
		System.out.println("nextLine... "+nextLine.length);
		this.extraData = new HashMap<String, String>();
		this.extraData.put("TelfCliente", nextLine[9]);
		this.extraData.put("DirCliente", nextLine[10]);
		this.extraData.put("CorreoCliente",nextLine[11]);
		this.extraData.put("CorreoInterno",nextLine[12]);
		//this.extraData.put("QADCodCliente", nextLine[13]);
	}
	
	public  InfoTributaria getInfoTributaria(String[] nextLine) {
		InfoTributaria infoTributaria = new InfoTributaria();
		infoTributaria.setAmbiente(nextLine[1]);
		infoTributaria.setTipoEmision(nextLine[2]);
		infoTributaria.setRazonSocial(nextLine[3]);
		infoTributaria.setNombreComercial(nextLine[4]);
		infoTributaria.setRuc(nextLine[5]);
		infoTributaria.setCodDoc(nextLine[6]);
		infoTributaria.setEstab(nextLine[7]);
		infoTributaria.setPtoEmi(nextLine[8]);
		infoTributaria.setSecuencial(nextLine[9]);
		infoTributaria.setDirMatriz(nextLine[10]);
		return infoTributaria;
	}


	public  InfoCompRetencion getInfoCompRetencion(String[] nextLine) {
		InfoCompRetencion infoCompRetencion = new InfoCompRetencion();
		infoCompRetencion.setFechaEmision(nextLine[1]);
		infoCompRetencion.setDirEstablecimiento(nextLine[2]);
		infoCompRetencion.setContribuyenteEspecial(nextLine[3]);
		infoCompRetencion.setObligadoContabilidad(nextLine[4]);
		infoCompRetencion.setTipoIdentificacionSujetoRetenido(nextLine[5]);
		infoCompRetencion.setRazonSocialSujetoRetenido(nextLine[6]);
		infoCompRetencion.setIdentificacionSujetoRetenido(nextLine[7]);
		infoCompRetencion.setPeriodoFiscal(nextLine[8]);
		return infoCompRetencion;
	}

	public  Impuesto getImpuesto(String[] nextLine) {
		Impuesto impuesto = new Impuesto();
		impuesto.setCodigo(nextLine[1]);
		impuesto.setCodigoRetencion(nextLine[2]);
		impuesto.setBaseImponible(new BigDecimal(nextLine[3].trim()));
		impuesto.setPorcentajeRetener(new BigDecimal(nextLine[4].trim()));
		impuesto.setValorRetenido(new BigDecimal(nextLine[5].trim()));
		impuesto.setCodDocSustento(nextLine[6]);
		impuesto.setNumDocSustento(nextLine[7]);
		impuesto.setFechaEmisionDocSustento(nextLine[8]);
		
		return impuesto;
		}

	public  CampoAdicional getInfoAdicional(String[] nextLine) {
		
		CampoAdicional campoAdicional = new CampoAdicional();
		campoAdicional.setNombre(nextLine[1]);
		campoAdicional.setValue(nextLine[2]);
		
		return campoAdicional;
	}
	
	@Override
	public Document getDocument() throws ParserConfigurationException, JAXBException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.newDocument();

        JAXBContext jc = JAXBContext.newInstance(ComprobanteRetencion.class);
        Marshaller marshaller = jc.createMarshaller();
        marshaller.marshal(this.document, doc);
        return doc;
	}
	
	@Override
	public String getRUC() {
		return this.document.getInfoTributaria().getRuc();
	}
	
	@Override
	public String getNumber() {
		return this.document.getInfoTributaria().getEstab().trim() + 
				this.document.getInfoTributaria().getPtoEmi().trim() + 
				this.document.getInfoTributaria().getSecuencial();
	}
	
	@Override
	public String getEnv() {
		return this.document.getInfoTributaria().getAmbiente();
	}
	
	@Override
    public String getCustomerType() {
    	return "P";
    }
	
	@Override
	public HashMap<String,String> getExtraData() {
		return this.extraData;
	}

	
	@Override
	public String getCustomerRUC() {
		return this.document.getInfoCompRetencion().getIdentificacionSujetoRetenido();
	}
	
	@Override
	public String getCustomerName() {
		return this.document.getInfoCompRetencion().getRazonSocialSujetoRetenido();
	}
	
	@Override 
	public String getCustomerIdentificationType() {
		return this.document.getInfoCompRetencion().getTipoIdentificacionSujetoRetenido();
	}

	@Override
	public String getIssueDate() {
		return this.document.getInfoCompRetencion().getFechaEmision();
	}
	
	@Override
	public String getIssueType() {
		return this.document.getInfoTributaria().getTipoEmision();
	}
	
	
	@Override
	public String getCustomerEmail() {
		return this.extraData.get("CorreoCliente");
	}
	
	@Override
	public String getCustomerPhone() {
		return this.extraData.get("TelfCliente");
	}
	
	@Override 
	public String getCustomerAddress() {
		return this.extraData.get("DirCliente");
	}
	
	@Override
	public InfoTributaria getInfoTributaria() {
		return this.document.getInfoTributaria();
	}
	
	@Override
	public InfoAdicional getInfoAdicional() {
		return this.document.getInfoAdicional();
	}
	
	public Impuestos getImpuestos() {
		return this.document.getImpuestos();
	}
	
	@Override
	public Object getInfo(){
		return this.document.getInfoCompRetencion();
	}
	@Override
	public  void saveInDB()  throws Exception { 
		Session session = DBDataSource.getInstance().getFactory().openSession();
        Transaction tx = null ;
        try {
        	tx = session.beginTransaction();
        	
        	//si existe lo borro
        	//detalles 
        	Query qry = session.createQuery("delete FacDetRetencionesEntity where ruc = :RUC and codEstablecimiento = :CODESTABLECIMIENTO and codPuntEmision = :CODPUNTOEMISION and secuencial = :SECUENCIAL and codigoDocumento = :CODIGODOCUMENTO");
        	qry.setParameter("RUC", document.getInfoTributaria().getRuc());
        	qry.setParameter("CODESTABLECIMIENTO", document.getInfoTributaria().getEstab());
        	qry.setParameter("CODPUNTOEMISION", document.getInfoTributaria().getPtoEmi());
        	qry.setParameter("SECUENCIAL", document.getInfoTributaria().getSecuencial());
        	qry.setParameter("CODIGODOCUMENTO",document.getInfoTributaria().getCodDoc());
        	qry.executeUpdate();
        	//cabecera
        	qry = session.createQuery("delete FacCabDocumentosEntity where ambiente = :AMBIENTE and ruc = :RUC and codEstablecimiento = :CODESTABLECIMIENTO and codPuntEmision = :CODPUNTOEMISION and secuencial = :SECUENCIAL and codigoDocumento = :CODIGODOCUMENTO");
        	qry.setParameter("AMBIENTE", Integer.parseInt(document.getInfoTributaria().getAmbiente()));
        	qry.setParameter("RUC", document.getInfoTributaria().getRuc());
        	qry.setParameter("CODESTABLECIMIENTO", document.getInfoTributaria().getEstab());
        	qry.setParameter("CODPUNTOEMISION", document.getInfoTributaria().getPtoEmi());
        	qry.setParameter("SECUENCIAL", document.getInfoTributaria().getSecuencial());
        	qry.setParameter("CODIGODOCUMENTO",document.getInfoTributaria().getCodDoc());
        	qry.executeUpdate();
        	
        	FacCabDocumentosEntity retencion = new FacCabDocumentosEntity();
        	
        	retencion.setAmbiente(Integer.parseInt(document.getInfoTributaria().getAmbiente()));
        	retencion.setRuc(document.getInfoTributaria().getRuc());
        	//factura.setTipoIdentificacion();
        	retencion.setCodEstablecimiento(document.getInfoTributaria().getEstab());
        	retencion.setCodPuntEmision(document.getInfoTributaria().getPtoEmi()); 
        	retencion.setSecuencial(document.getInfoTributaria().getSecuencial()); 
        	retencion.setClaveAcceso(document.getInfoTributaria().getClaveAcceso());
        	SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        	Date dt1 = null;
			try {
				dt1 = df.parse(document.getInfoCompRetencion().getFechaEmision());
			} catch (ParseException e) {
				e.printStackTrace();
			}
        	
        	retencion.setFechaEmision(new java.sql.Date(dt1.getTime())); 
        	//retencion.setGuiaRemision(document.getInfoCompRetencion().getGuiaRemision());
        	retencion.setRazonSocialComprador(document.getInfoCompRetencion().getRazonSocialSujetoRetenido());
        	retencion.setIdentificacionComprador(document.getInfoCompRetencion().getIdentificacionSujetoRetenido()); 
        	//retencion.setTotalSinImpuesto(document.getInfoCompRetencion().getTotalSinImpuestos().doubleValue());
        	//retencion.setTotalDescuento(document.getInfoCompRetencion().getTotalDescuento().doubleValue());
        	retencion.setEmail(this.extraData.get("CorreoCliente")); //deberia estar en InfoAdicional ?
        	//retencion.setPropina(document.getInfoCompRetencion().getPropina().doubleValue());
        	//retencion.setMoneda(document.getInfoCompRetencion().getMoneda());
        	//factura.setInfoAdicional(document.getInfoAdicional().getCampoAdicional().get(0))
        	retencion.setPeriodoFiscal(document.getInfoCompRetencion().getPeriodoFiscal()); //-- solo para retencion
        	//retencion.setRise() --solo para NC/ND 
        	//retencion.setFechaInicioTransporte() -- guia de remision ?
        	//retencion.setfechaFinTransporte(9 -- guia de remision ?
        	//retencion.setplaca guia de remision ?
        	//retencion.setfechaEmisionDocSustento -- solo NC/ND
        	//retencion.setMotivoRazon -- solo NC 
        	//retencion.setIdentificacionDestinatario -- guia de remision ?
        	//retencion.setRazonSocialDestinatario -- guia de remision ?
        	//retencion.setDireccionDestinatario -- guia de remision ? 
        	//retencion.setMotivoTraslado -- guia de remision ?
        	//retencion.setDocAduaneroUnico --guia de remision 
        	//retencion.setCodEstablecimientoDest --guia de remision
        	//retencion.setRuta -- guia de remision
        	//retencion.setCodDocSustento() --retencion 
        	//retencion.setNumDocSustento -- retencion 
        	//retencion.setNumAutDocSustento -- ???
        	//retencion.setFecEmisionDocSustento -- NC/ND / retencion x3 
        	retencion.setAutorizacion("");
        	retencion.setFechaautorizacion(null);
        	//retencion.setImporteTotal(document.getInfoCompRetencion().getImporteTotal().doubleValue());
        	retencion.setCodigoDocumento(document.getInfoTributaria().getCodDoc());
        	//retencion.setCodDocModificado() -- NC/ND
        	//retencion.setNumDocModificado() -- NC/ND 
        	//retencion.setMotivoValor() -- ND
        	retencion.setTipIdentificacionComprador(document.getInfoCompRetencion().getTipoIdentificacionSujetoRetenido());
        	retencion.setTipoEmision(document.getInfoTributaria().getTipoEmision());

        	
        	retencion.setIsActive("Y");
        	retencion.setEstadoTransaccion("");
        	retencion.setMsjError("");
        	retencion.setTipo("");
        	retencion.setClaveAccesoContigente("");
        	retencion.setClaveContingencia("");
        	retencion.setDocuAutorizacion("");
        	retencion.setInfoAdicional(this.extraData.get("CorreoInterno"));
        	session.save(retencion);
        	
        	//detalle
        	
        	
        	for (Impuesto detalleImpuesto : document.getImpuestos().getImpuesto()) {
	        	FacDetRetencionesEntity detalleRetencion = new FacDetRetencionesEntity();
	        	
			    detalleRetencion.setRuc(document.getInfoTributaria().getRuc());
			    detalleRetencion.setCodEstablecimiento(document.getInfoTributaria().getEstab());
			    detalleRetencion.setCodPuntEmision(document.getInfoTributaria().getPtoEmi());
			    detalleRetencion.setSecuencial(document.getInfoTributaria().getSecuencial());
			    detalleRetencion.setCodigoDocumento(document.getInfoTributaria().getCodDoc());
			    
			    //detalleRetencion.setSecuencialRetencion();
			    
			    detalleRetencion.setCodImpuesto(Integer.parseInt(detalleImpuesto.getCodigo()));;
			    detalleImpuesto.getCodDocSustento(); //TODO no figura en tabla 
			    detalleImpuesto.getNumDocSustento(); //TODO no figura en tabla
			    detalleImpuesto.getFechaEmisionDocSustento(); //TODO : no figura en tabla
			    //Ejercicio Fiscal
			    
			    detalleRetencion.setBaseImponible(detalleImpuesto.getBaseImponible().doubleValue());
			    detalleRetencion.setCodPorcentaje(Integer.parseInt(detalleImpuesto.getCodigoRetencion()));
			    detalleRetencion.setPorcentajeRetencion(detalleImpuesto.getPorcentajeRetener().doubleValue());
			    detalleRetencion.setValor(detalleImpuesto.getValorRetenido().doubleValue());
			    session.save(detalleRetencion);
        	}
        	
        	tx.commit();
		 } catch (HibernateException e) {
	         if (tx!=null) tx.rollback();
	         e.printStackTrace();
	         throw new Exception();
	     }finally {
	         session.close();
	     }
	}
	
	@Override
	public Impuestos getInfoDetail() {
		return this.document.getImpuestos();
	}
	
	@Override
	public void setData(Object obj) {
		this.document = (ComprobanteRetencion)obj;
	}
}
