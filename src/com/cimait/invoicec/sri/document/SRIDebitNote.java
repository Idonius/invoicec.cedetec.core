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
import com.cimait.invoicec.sri.schema.debitnote.NotaDebito;
import com.cimait.invoicec.sri.schema.debitnote.NotaDebito.InfoAdicional;
import com.cimait.invoicec.sri.schema.debitnote.NotaDebito.InfoAdicional.CampoAdicional;
import com.cimait.invoicec.sri.schema.debitnote.NotaDebito.InfoNotaDebito;
import com.cimait.invoicec.sri.schema.debitnote.NotaDebito.InfoNotaDebito.Impuestos;
import com.cimait.invoicec.sri.schema.debitnote.NotaDebito.Motivos;
import com.cimait.invoicec.sri.schema.debitnote.NotaDebito.Motivos.Motivo;
import com.cimait.invoicec.sri.schema.general.Impuesto;
import com.cimait.invoicec.sri.schema.general.InfoTributaria;
import com.cimait.invoicec.sri.schema.invoice.Factura;
import com.sun.directory.examples.ModifyDocumentAcceso;
import com.cimait.invoicec.entity.FacDetMotivosdebitoEntity;

public class SRIDebitNote extends SRIDocument {
	private NotaDebito document = null;
	private HashMap<String, String> extraData = null;
	
	@Override
	public void parseFile() throws IOException {
				CSVReader reader = new CSVReader(new FileReader(this.inputFile), '|');

				String[] nextLine;
				this.document = new NotaDebito();
				
				this.document.setId("comprobante");
				this.document.setVersion("1.0.0");
				
				//Acumular motivos
				Motivos motivos = new Motivos();
				List<Motivo> lMotivo = new ArrayList<NotaDebito.Motivos.Motivo>();
				//Acumular informacion adicional
				InfoAdicional infoAdicional = new InfoAdicional();
				List<CampoAdicional> lCampoAdicional = new ArrayList<NotaDebito.InfoAdicional.CampoAdicional>();

				try {
					while ((nextLine = reader.readNext()) != null) {
						if (nextLine[0].equals("IT")) { // 1 - 1
							this.document.setInfoTributaria(getInfoTributaria(nextLine));
						} else if (nextLine[0].equals("CD")) { // 1 - 1
							this.document.setInfoNotaDebito(getInfoNotaDebito(nextLine));
							setExtraData(nextLine);
							CampoAdicional cAdicional = new CampoAdicional();
							cAdicional.setNombre("Folio Interno");
							cAdicional.setValue(extraData.get("QADCodFactura"));
							lCampoAdicional.add(cAdicional);
							
							cAdicional = new CampoAdicional();
							cAdicional.setNombre("Monto en Texto");
							cAdicional.setValue("Monto en Texto : " + extraData.get("MontoTexto"));
							lCampoAdicional.add(cAdicional);
							
							cAdicional = new CampoAdicional();
							cAdicional.setNombre("Correo Cliente");
							cAdicional.setValue("Correo Cliente : " + extraData.get("CorreoCliente"));
							lCampoAdicional.add(cAdicional);
						} else if (nextLine[0].equals("DD")) { // 1 - N
							lMotivo.add(getMotivos(nextLine));
						} else if (nextLine[0].equals("IA")) { // 1 - N
							lCampoAdicional.add(getInfoAdicional(nextLine));
						}
					}
					motivos.getMotivo().addAll(lMotivo);
					this.document.setMotivos(motivos);
					
					infoAdicional.getCampoAdicional().addAll(lCampoAdicional);
					this.document.setInfoAdicional(infoAdicional);
				} catch (Exception e) {
					System.out.println("error " + e.getMessage());
					e.printStackTrace();
				}
				reader.close();
	}
	
	@Override
	public void setExtraData(String[] nextLine){
		this.extraData = new HashMap<String, String>();
		this.extraData.put("QADCodFactura", nextLine[28]);
		this.extraData.put("TelfCliente", nextLine[29]);
		this.extraData.put("DirCliente", nextLine[30]);
		this.extraData.put("MontoTexto", nextLine[31]);
		this.extraData.put("CorreoCliente",nextLine[32]);
		this.extraData.put("CorreoInterno",nextLine[33]);
		this.extraData.put("QADCodCliente", nextLine[34]);
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
	public Object getInfo(){
		return this.document.getInfoNotaDebito();
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
	
	public  InfoNotaDebito getInfoNotaDebito(String[] nextLine) {
	
		InfoNotaDebito infoNotaDebito = new InfoNotaDebito();
	
		infoNotaDebito.setFechaEmision(nextLine[1]);
		infoNotaDebito.setDirEstablecimiento(nextLine[2]);
		infoNotaDebito.setTipoIdentificacionComprador(nextLine[3]);
		infoNotaDebito.setRazonSocialComprador(nextLine[4]);
		infoNotaDebito.setIdentificacionComprador(nextLine[5]);
		infoNotaDebito.setContribuyenteEspecial(nextLine[6]);
		infoNotaDebito.setObligadoContabilidad(nextLine[7]);
		if (!nextLine[8].trim().equals("")){
			infoNotaDebito.setRise(nextLine[8]);
		}
		infoNotaDebito.setCodDocModificado(nextLine[9]);
		infoNotaDebito.setNumDocModificado(nextLine[10]);
		infoNotaDebito.setFechaEmisionDocSustento(nextLine[11]);
		infoNotaDebito.setTotalSinImpuestos(new BigDecimal(nextLine[12].trim()));
		
		//notaDebito.setInfoNotaDebito(infoNotaDebito);
		
		Impuestos impuestos = new Impuestos();
		List<Impuesto> lImpuesto = new ArrayList<Impuesto>();
		
		Impuesto impuesto = new Impuesto();
		impuesto.setCodigo(nextLine[13]);
		impuesto.setCodigoPorcentaje(nextLine[14]);
		impuesto.setTarifa(new BigDecimal(nextLine[15].trim()));
		impuesto.setBaseImponible(new BigDecimal(nextLine[16].trim()));
		impuesto.setValor(new BigDecimal(nextLine[17].trim()));
		lImpuesto.add(impuesto);
		
		impuesto = new Impuesto();
		impuesto.setCodigo(nextLine[18]);
		impuesto.setCodigoPorcentaje(nextLine[19]);
		impuesto.setBaseImponible(new BigDecimal(nextLine[20].trim()));
		impuesto.setValor(new BigDecimal(nextLine[21].trim()));
		
		if (impuesto.getValor().doubleValue() != 0.0) {
			lImpuesto.add(impuesto);
		}

		impuesto = new Impuesto();
		impuesto.setCodigo(nextLine[22]);
		impuesto.setCodigoPorcentaje(nextLine[23]);
		impuesto.setBaseImponible(new BigDecimal(nextLine[24].trim()));
		impuesto.setValor(new BigDecimal(nextLine[25].trim()));
		
		if (impuesto.getValor().doubleValue() != 0.0) {
			lImpuesto.add(impuesto);
		}

		impuestos.getImpuesto().addAll(lImpuesto);
		
		infoNotaDebito.setValorTotal(new BigDecimal(nextLine[26].trim()));
		//MONEDA 27
		infoNotaDebito.setImpuestos(impuestos);

		return infoNotaDebito;
	}
	
	public  Motivo getMotivos(String[] nextLine) {
		Motivo motivo = new Motivo();
		motivo.setRazon(nextLine[1]);
		motivo.setValor(new BigDecimal(nextLine[2].trim()));
		return motivo;
	}
	
	public CampoAdicional getInfoAdicional(String[] nextLine) {
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

        JAXBContext jc = JAXBContext.newInstance(NotaDebito.class);
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
	public String getCustomerRUC() {
		return this.document.getInfoNotaDebito().getIdentificacionComprador();
	}
	
	@Override
	public HashMap<String,String> getExtraData() {
		return this.extraData;
	}

	
	@Override
	public String getCustomerName() {
		return this.document.getInfoNotaDebito().getRazonSocialComprador();
	}
	
	@Override 
	public String getCustomerIdentificationType() {
		return this.document.getInfoNotaDebito().getTipoIdentificacionComprador();
	}

	@Override
	public String getCustomerEmail() {
		return this.extraData.get("CorreoCliente");
	}
	
	@Override
    public String getCustomerType() {
    	return "C";
    }
	
	@Override
	public String getIssueDate() {
		return this.document.getInfoNotaDebito().getFechaEmision();
	}
	
	@Override
	public String getIssueType() {
		return this.document.getInfoTributaria().getTipoEmision();
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
	
	@Override
	public  void saveInDB()  throws HibernateException, SQLException { 
		Session session = DBDataSource.getInstance().getFactory().openSession();
        Transaction tx = null ;
        try {
        	tx = session.beginTransaction();
        	
        	//si existe lo borro
        	//detalles 
        	Query qry = session.createQuery("delete FacDetMotivosdebitoEntity where ruc = :RUC and codEstablecimiento = :CODESTABLECIMIENTO and codPuntEmision = :CODPUNTOEMISION and secuencial = :SECUENCIAL and codigoDocumento = :CODIGODOCUMENTO");
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
        	
        	
        	FacCabDocumentosEntity notaDebito = new FacCabDocumentosEntity();
        	notaDebito.setAmbiente(Integer.parseInt(document.getInfoTributaria().getAmbiente()));
        	notaDebito.setRuc(document.getInfoTributaria().getRuc());
        	notaDebito.setCodEstablecimiento(document.getInfoTributaria().getEstab());
        	notaDebito.setCodPuntEmision(document.getInfoTributaria().getPtoEmi()); 
        	notaDebito.setSecuencial(document.getInfoTributaria().getSecuencial()); 
        	notaDebito.setClaveAcceso(document.getInfoTributaria().getClaveAcceso());
        	SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        	Date dt1 = null;
			try {
				dt1 = df.parse(document.getInfoNotaDebito().getFechaEmision());
			} catch (ParseException e) {
				e.printStackTrace();
			}
        	
        	notaDebito.setFechaEmision(new java.sql.Date(dt1.getTime())); 
        	//notaCredito.setGuiaRemision(document.getInfoNotaCredito().getGuiaRemision());
        	notaDebito.setRazonSocialComprador(document.getInfoNotaDebito().getRazonSocialComprador());
        	notaDebito.setIdentificacionComprador(document.getInfoNotaDebito().getIdentificacionComprador()); 
        	notaDebito.setTotalSinImpuesto(document.getInfoNotaDebito().getTotalSinImpuestos().doubleValue());
        	//notaCredito.setTotalDescuento(document.getInfoNotaCredito().getTotalDescuento().doubleValue());
        	notaDebito.setEmail(this.extraData.get("CorreoCliente")); //deberia estar en InfoAdicional ?
        	//notaCredito.setPropina(document.getInfoFactura().getPropina().doubleValue());
        	//notaDebito.setMoneda(document.getInfoNotaDebito().getMoneda());
        	
        	notaDebito.setRise(document.getInfoNotaDebito().getRise());
        	
        	dt1 = null;
        	try {
				dt1 = df.parse(document.getInfoNotaDebito().getFechaEmisionDocSustento());
			} catch (ParseException e) {
				e.printStackTrace();
			}
        	
        	notaDebito.setFechaEmisionDocSustento(new java.sql.Date(dt1.getTime()));
        	//maneja motivos como detalle
        	//notaDebito.setMotivoRazon(motivoRazon);(document.getInfoNotaDebito().getMotivo());
        	
        	
        	notaDebito.setAutorizacion("");
        	notaDebito.setFechaautorizacion(null);
        	notaDebito.setImporteTotal(document.getInfoNotaDebito().getValorTotal().doubleValue());
        	notaDebito.setCodigoDocumento(document.getInfoTributaria().getCodDoc());
        	
        	notaDebito.setIsActive("Y");
        	notaDebito.setEstadoTransaccion("");
        	notaDebito.setMsjError("");
        	notaDebito.setTipo("");
        	notaDebito.setClaveAccesoContigente("");
        	notaDebito.setClaveContingencia("");
        	notaDebito.setDocuAutorizacion("");
        	notaDebito.setInfoAdicional(this.extraData.get("CorreoInterno"));
        	session.save(notaDebito);
        	
        	for (Motivo motivosNotaDebito : document.getMotivos().getMotivo()) {
        		FacDetMotivosdebitoEntity motivoDetalle = new FacDetMotivosdebitoEntity();
        		
        		motivoDetalle.setRuc(document.getInfoTributaria().getRuc());
        		motivoDetalle.setCodEstablecimiento(document.getInfoTributaria().getEstab());
			    motivoDetalle.setCodPuntEmision(document.getInfoTributaria().getPtoEmi());
			    motivoDetalle.setSecuencial(document.getInfoTributaria().getSecuencial());
 
			    motivoDetalle.setRazon(motivosNotaDebito.getRazon());
			    motivoDetalle.setValor(motivosNotaDebito.getValor().doubleValue());
			    
			    motivoDetalle.setCodigoDocumento(document.getInfoTributaria().getCodDoc());
			    session.save(motivoDetalle);
        	}
        	tx.commit();
		 } catch (HibernateException e) {
	         if (tx!=null) tx.rollback();
	         e.printStackTrace();
	     }finally {
	         session.close();
	     }
	}
	
	@Override
	public Motivos getInfoDetail() {
		return this.document.getMotivos();
	}
	
	@Override
	public void setData(Object obj) {
		this.document = (NotaDebito)obj;
	}
}
