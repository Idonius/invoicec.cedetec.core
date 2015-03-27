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
import com.cimait.invoicec.sri.schema.general.Impuesto;
import com.cimait.invoicec.sri.schema.general.InfoTributaria;
import com.cimait.invoicec.sri.schema.invoice.Factura;
import com.cimait.invoicec.sri.schema.invoice.Factura.Detalles;
import com.cimait.invoicec.sri.schema.invoice.Factura.Detalles.Detalle;
import com.cimait.invoicec.sri.schema.invoice.Factura.InfoAdicional;
import com.cimait.invoicec.sri.schema.invoice.Factura.InfoAdicional.CampoAdicional;
import com.cimait.invoicec.sri.schema.invoice.Factura.InfoFactura;
import com.cimait.invoicec.sri.schema.invoice.Factura.InfoFactura.TotalConImpuestos;
import com.cimait.invoicec.sri.schema.invoice.Factura.InfoFactura.TotalConImpuestos.TotalImpuesto;
import com.sun.directory.examples.ModifyDocumentAcceso;

public class SRIInvoice extends SRIDocument {
	private Factura document = null;
	private HashMap<String,String> extraData = null;
	private HashMap<Integer,String> extraDataDetail = null;
	
	@Override
	public void parseFile() throws IOException {
		CSVReader reader = new CSVReader(new FileReader(this.inputFile), '|');
		String[] nextLine;
		this.document = new Factura();
		
		this.document.setId("comprobante");
		this.document.setVersion("1.1.0");
		
		//acumular Detalle
		Factura.Detalles detallesFactura = new Factura.Detalles();
		List<Factura.Detalles.Detalle> lDetalleFactura = new ArrayList<Factura.Detalles.Detalle>();

		//Acumular Informacion Adicional
		InfoAdicional infoAdicional = new InfoAdicional();
		List<CampoAdicional> lCampoAdicional = new ArrayList<Factura.InfoAdicional.CampoAdicional>();
		
		this.extraDataDetail = new HashMap<Integer, String>();
		
		try {
			while ((nextLine = reader.readNext()) != null) {
				if (nextLine[0].equals("IT")) { // 1 - 1
					this.document.setInfoTributaria(getInfoTributaria(nextLine));
				} else if (nextLine[0].equals("CF")) { // 1 - 1
					this.document.setInfoFactura(getInfoFactura(nextLine));
					//datos necesarios para impresion
					setExtraData(nextLine);
					//datos a agregar a infoAdicional
					CampoAdicional cAdicional = new CampoAdicional();
					cAdicional.setNombre("Folio Interno");
					cAdicional.setValue(extraData.get("QADCodFactura"));
					lCampoAdicional.add(cAdicional);
					
					cAdicional = new CampoAdicional();
					cAdicional.setNombre("Monto en Texto");
					cAdicional.setValue("Monto en Texto : " + extraData.get("MontoTexto"));
					lCampoAdicional.add(cAdicional);
					
					cAdicional = new CampoAdicional();
					cAdicional.setNombre("Orden de Compra");
					cAdicional.setValue("Orden de Compra : " + extraData.get("OrdenCompraCliente"));
					lCampoAdicional.add(cAdicional);
					
				} else if (nextLine[0].equals("DF")) { // 1 - N
					lDetalleFactura.add(getDetalles(nextLine));
					this.extraDataDetail.put(lDetalleFactura.size()-1,nextLine[1]);
				} else if (nextLine[0].equals("IA")) { // 1 - N
					lCampoAdicional.add(getInfoAdicional(nextLine));
				}
			}
			//agregar todos los detalles
			detallesFactura.getDetalle().addAll(lDetalleFactura);
			this.document.setDetalles(detallesFactura);
			//agregar todos los campos adicionales
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
		this.extraData.put("QADCodFactura", nextLine[27]);
		this.extraData.put("TelfCliente", nextLine[28]);
		this.extraData.put("DirCliente", nextLine[29]);
		this.extraData.put("MontoTexto", nextLine[30]);
		this.extraData.put("OrdenCompraCliente",nextLine[31]);
		this.extraData.put("CorreoCliente",nextLine[32]);
		this.extraData.put("CorreoInterno",nextLine[33]);
		this.extraData.put("QADCodCliente", nextLine[34]);
	}
	
	@Override
	public HashMap<String,String> getExtraData() {
		return this.extraData;
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
	public InfoAdicional getInfoAdicional() {
		return this.document.getInfoAdicional();
	}
	
	public InfoTributaria getInfoTributaria(String[] nextLine) {
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

	public InfoFactura getInfoFactura(String[] nextLine) {
		InfoFactura infoFactura = new InfoFactura();
		
		infoFactura.setFechaEmision(nextLine[1]);
		infoFactura.setDirEstablecimiento(nextLine[2]);
		infoFactura.setContribuyenteEspecial(nextLine[3]);
		infoFactura.setObligadoContabilidad(nextLine[4]);
		infoFactura.setTipoIdentificacionComprador(nextLine[5]);
		infoFactura.setGuiaRemision(nextLine[6].trim());
		infoFactura.setRazonSocialComprador(nextLine[7].trim());
		infoFactura.setIdentificacionComprador(nextLine[8]);
		infoFactura.setTotalSinImpuestos(new BigDecimal(nextLine[9].trim()));
		infoFactura.setTotalDescuento(new BigDecimal(nextLine[10].trim()));

		TotalConImpuestos totalConImpuestos = new Factura.InfoFactura.TotalConImpuestos();
		List<TotalConImpuestos.TotalImpuesto> lTotalImpuesto = new ArrayList<TotalConImpuestos.TotalImpuesto>();

		TotalConImpuestos.TotalImpuesto tmpTotalImpuesto = new TotalConImpuestos.TotalImpuesto();
		tmpTotalImpuesto.setCodigo(nextLine[16]);
		tmpTotalImpuesto.setCodigoPorcentaje(nextLine[17]);
		tmpTotalImpuesto.setBaseImponible(new BigDecimal(nextLine[18].trim()));
		tmpTotalImpuesto.setValor(new BigDecimal(nextLine[19].trim()));

		if (tmpTotalImpuesto.getValor().doubleValue() != 0.0) {
			lTotalImpuesto.add(tmpTotalImpuesto);
		}
		
		tmpTotalImpuesto = new TotalConImpuestos.TotalImpuesto();
		tmpTotalImpuesto.setCodigo(nextLine[11]);
		tmpTotalImpuesto.setCodigoPorcentaje(nextLine[12]);
		tmpTotalImpuesto.setBaseImponible(new BigDecimal(nextLine[14].trim()));
		tmpTotalImpuesto.setValor(new BigDecimal(nextLine[15].trim()));
		
		if (tmpTotalImpuesto.getValor().doubleValue() != 0.0) {
			lTotalImpuesto.add(tmpTotalImpuesto);
		}

		tmpTotalImpuesto = new TotalConImpuestos.TotalImpuesto();
		tmpTotalImpuesto.setCodigo(nextLine[20]);
		tmpTotalImpuesto.setCodigoPorcentaje(nextLine[21]);
		tmpTotalImpuesto.setBaseImponible(new BigDecimal(nextLine[22].trim()));
		tmpTotalImpuesto.setValor(new BigDecimal(nextLine[23].trim()));

		
		if (tmpTotalImpuesto.getValor().doubleValue() != 0.0) {
			lTotalImpuesto.add(tmpTotalImpuesto);
		}

		totalConImpuestos.getTotalImpuesto().addAll(lTotalImpuesto);

		infoFactura.setTotalConImpuestos(totalConImpuestos);
		infoFactura.setPropina(new BigDecimal(nextLine[24].trim()));
		infoFactura.setImporteTotal(new BigDecimal(nextLine[25].trim()));
		infoFactura.setMoneda(nextLine[26]);

		return infoFactura;
	}

	public Factura.Detalles.Detalle getDetalles(String[] nextLine) {
		Factura.Detalles.Detalle detalleFactura = new Factura.Detalles.Detalle();
		detalleFactura.setCodigoPrincipal(nextLine[2]);
		detalleFactura.setCodigoAuxiliar(nextLine[3]);
		detalleFactura.setDescripcion(nextLine[4]);
		detalleFactura.setCantidad(new BigDecimal(nextLine[5].trim()));
		detalleFactura.setPrecioUnitario(new BigDecimal(nextLine[6].trim()));
		detalleFactura.setDescuento(new BigDecimal(nextLine[7].trim()));
		detalleFactura.setPrecioTotalSinImpuesto(new BigDecimal(nextLine[8]
				.trim()));

		
		if (!nextLine[9].trim().equals("")) {
			List<Factura.Detalles.Detalle.DetallesAdicionales.DetAdicional> lDetallesAdicionales = new ArrayList<Factura.Detalles.Detalle.DetallesAdicionales.DetAdicional>();
	
			Factura.Detalles.Detalle.DetallesAdicionales.DetAdicional detalleAdicional = new Factura.Detalles.Detalle.DetallesAdicionales.DetAdicional();
			detalleAdicional.setNombre(nextLine[9]);
			detalleAdicional.setValor(nextLine[10]);
			lDetallesAdicionales.add(detalleAdicional);
	
			detalleAdicional = new Factura.Detalles.Detalle.DetallesAdicionales.DetAdicional();
			detalleAdicional.setNombre(nextLine[11]);
			lDetallesAdicionales.add(detalleAdicional);
			Factura.Detalles.Detalle.DetallesAdicionales detallesAdicionales = new Factura.Detalles.Detalle.DetallesAdicionales();
			detallesAdicionales.getDetAdicional().addAll(lDetallesAdicionales);

			detalleFactura.setDetallesAdicionales(detallesAdicionales);

		}
		
		// impuesto del detalle

		Factura.Detalles.Detalle.Impuestos impuestosDetalle = new Factura.Detalles.Detalle.Impuestos();

		List<Impuesto> lImpuestoDetalle = new ArrayList<Impuesto>();

		Impuesto impuestoDetalle = new Impuesto();
		impuestoDetalle.setCodigo(nextLine[17]);
		impuestoDetalle.setCodigoPorcentaje(nextLine[18]);
		impuestoDetalle.setTarifa(new BigDecimal(nextLine[19].trim()));
		impuestoDetalle.setBaseImponible(new BigDecimal(nextLine[20].trim()));
		impuestoDetalle.setValor(new BigDecimal(nextLine[21].trim()));

		if (impuestoDetalle.getValor().doubleValue() != 0.0) {
			lImpuestoDetalle.add(impuestoDetalle);
		}
		
		impuestoDetalle = new Impuesto();
		impuestoDetalle.setCodigo(nextLine[12]);
		impuestoDetalle.setCodigoPorcentaje(nextLine[13]);
		impuestoDetalle.setTarifa(new BigDecimal(nextLine[14].trim()));
		impuestoDetalle.setBaseImponible(new BigDecimal(nextLine[15].trim()));
		impuestoDetalle.setValor(new BigDecimal(nextLine[16].trim()));
		
		if (impuestoDetalle.getValor().doubleValue() != 0.0) {
			lImpuestoDetalle.add(impuestoDetalle);
		}

		impuestoDetalle = new Impuesto();
		impuestoDetalle.setCodigo(nextLine[22]);
		impuestoDetalle.setCodigoPorcentaje(nextLine[23]);
		impuestoDetalle.setTarifa(new BigDecimal(nextLine[24].trim()));
		impuestoDetalle.setBaseImponible(new BigDecimal(nextLine[24].trim()));
		impuestoDetalle.setValor(new BigDecimal(nextLine[25].trim()));
		
		if (impuestoDetalle.getValor().doubleValue() != 0.0) {
			lImpuestoDetalle.add(impuestoDetalle);
		}

		impuestosDetalle.getImpuesto().addAll(lImpuestoDetalle);

		detalleFactura.setImpuestos(impuestosDetalle);

		return detalleFactura;
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
        
        JAXBContext jc = JAXBContext.newInstance(Factura.class);
        Marshaller marshaller = jc.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
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
		return this.document.getInfoFactura().getIdentificacionComprador();
	}
	
	@Override
	public String getCustomerName() {
		return this.document.getInfoFactura().getRazonSocialComprador();
	}
	
	@Override 
	public String getCustomerIdentificationType() {
		return this.document.getInfoFactura().getTipoIdentificacionComprador();
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
	public String getType() {
		return this.document.getInfoTributaria().getCodDoc();
	}
	
	@Override
	public InfoTributaria getInfoTributaria() {
		return this.document.getInfoTributaria();
	}
	
	@Override
	public String getIssueDate() {
		return this.document.getInfoFactura().getFechaEmision();
	}

	@Override
	public String getIssueType() {
		return this.document.getInfoTributaria().getTipoEmision();
	}
	
	@Override
    public String getCustomerType() {
    	return "C";
    }

	
	@Override
	public void saveInDB() throws Exception {
		Session session = DBDataSource.getInstance().getFactory().openSession();
        Transaction tx = null ;
        try {
        	tx = session.beginTransaction();
        	
        	//si existe lo borro
        	//detalles 
        	Query qry = session.createQuery("delete FacDetDocumentosEntity where ruc = :RUC and codEstablecimiento = :CODESTABLECIMIENTO and codPuntEmision = :CODPUNTOEMISION and secuencial = :SECUENCIAL and codigoDocumento = :CODIGODOCUMENTO");
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
        	
        	FacCabDocumentosEntity factura = new FacCabDocumentosEntity();
        	
        	factura.setAmbiente(Integer.parseInt(document.getInfoTributaria().getAmbiente()));
        	factura.setRuc(document.getInfoTributaria().getRuc());
        	//factura.setTipoIdentificacion();
        	factura.setCodEstablecimiento(document.getInfoTributaria().getEstab());
        	factura.setCodPuntEmision(document.getInfoTributaria().getPtoEmi()); 
        	factura.setSecuencial(document.getInfoTributaria().getSecuencial()); 
        	factura.setClaveAcceso(document.getInfoTributaria().getClaveAcceso());
        	SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        	Date dt1 = null;
			try {
				dt1 = df.parse(document.getInfoFactura().getFechaEmision());
			} catch (ParseException e) {
				e.printStackTrace();
			}
        	
        	factura.setFechaEmision(new java.sql.Date(dt1.getTime())); 
        	factura.setGuiaRemision(document.getInfoFactura().getGuiaRemision());
        	factura.setRazonSocialComprador(document.getInfoFactura().getRazonSocialComprador());
        	factura.setIdentificacionComprador(document.getInfoFactura().getIdentificacionComprador()); 
        	factura.setTotalSinImpuesto(document.getInfoFactura().getTotalSinImpuestos().doubleValue());
        	factura.setTotalDescuento(document.getInfoFactura().getTotalDescuento().doubleValue());
        	factura.setEmail(this.extraData.get("CorreoCliente")); //deberia estar en InfoAdicional ?
        	factura.setPropina(document.getInfoFactura().getPropina().doubleValue());
        	factura.setMoneda(document.getInfoFactura().getMoneda());
        	//factura.setInfoAdicional(document.getInfoAdicional().getCampoAdicional().get(0))
        	//factura.setPeriodoFiscal(document.getInfoFactura().get) -- solo para retencion
        	//factura.setRise() --solo para NC/ND 
        	//factura.setFechaInicioTransporte() -- guia de remision ?
        	//factura.setfechaFinTransporte(9 -- guia de remision ?
        	//factura.setplaca guia de remision ?
        	//factura.setfechaEmisionDocSustento -- solo NC/ND
        	//factura.setMotivoRazon -- solo NC 
        	//factura.setIdentificacionDestinatario -- guia de remision ?
        	//factura.setRazonSocialDestinatario -- guia de remision ?
        	//factura.setDireccionDestinatario -- guia de remision ? 
        	//factura.setMotivoTraslado -- guia de remision ?
        	//factura.setDocAduaneroUnico --guia de remision 
        	//factura.setCodEstablecimientoDest --guia de remision
        	//factura.setRuta -- guia de remision
        	//factura.setCodDocSustento() --retencion 
        	//factura.setNumDocSustento -- retencion 
        	//factura.setNumAutDocSustento -- ???
        	//factura.setFecEmisionDocSustento -- NC/ND / retencion x3 
        	factura.setAutorizacion("");
        	factura.setFechaautorizacion(null);
        	factura.setImporteTotal(document.getInfoFactura().getImporteTotal().doubleValue());
        	factura.setCodigoDocumento(document.getInfoTributaria().getCodDoc());
        	//factura.setCodDocModificado() -- NC/ND
        	//factura.setNumDocModificado() -- NC/ND 
        	//factura.setMotivoValor() -- ND
        	factura.setTipIdentificacionComprador(document.getInfoFactura().getTipoIdentificacionComprador());
        	factura.setTipoEmision(document.getInfoTributaria().getTipoEmision());
        	
        	//factura.setPartida ??
        	
        	//TotalConImpuestos
        	
        	for (TotalImpuesto totalImpuesto : document.getInfoFactura().getTotalConImpuestos().getTotalImpuesto()) {
				if (totalImpuesto.getCodigo().equals("2") && totalImpuesto.getCodigoPorcentaje().equals("2")) { // IVA + 12%
					factura.setSubtotal12(totalImpuesto.getValor().doubleValue());
				}
				
				if (totalImpuesto.getCodigo().equals("2") && totalImpuesto.getCodigoPorcentaje().equals("0")) { // IVA + 0%
					factura.setSubtotal0(totalImpuesto.getValor().doubleValue());
				}
				
				if (totalImpuesto.getCodigo().equals("2") && totalImpuesto.getCodigoPorcentaje().equals("6")) { // IVA + No Objeto de Impuesto
					factura.setSubtotalNoIva(totalImpuesto.getValor().doubleValue());
				}

				if (totalImpuesto.getCodigo().equals("3"))  { // ICE
					factura.setTotalvalorIce(totalImpuesto.getValor().doubleValue());
				
				}
				
			}
        	factura.setInfoAdicional(this.extraData.get("CorreoInterno"));
        	factura.setIsActive("Y");
        	factura.setEstadoTransaccion("");
        	factura.setMsjError("");
        	factura.setTipo("");
        	factura.setClaveAccesoContigente("");
        	factura.setClaveContingencia("");
        	factura.setDocuAutorizacion("");
        	
        	session.save(factura);
        	
        	//detalle
        	
        	int index = 0;
        	for (Detalle detalleFactura : document.getDetalles().getDetalle()) {
        	
	        	FacDetDocumentosEntity facturaDetalle = new FacDetDocumentosEntity();
			    facturaDetalle.setRuc(document.getInfoTributaria().getRuc());
			    facturaDetalle.setCodEstablecimiento(document.getInfoTributaria().getEstab());
			    facturaDetalle.setCodPuntEmision(document.getInfoTributaria().getPtoEmi());
			    facturaDetalle.setSecuencial(document.getInfoTributaria().getSecuencial());
			    
			    facturaDetalle.setSecuencialDetalle(Integer.parseInt(this.extraDataDetail.get(index)));

			    facturaDetalle.setCodPrincipal(detalleFactura.getCodigoPrincipal());
			    facturaDetalle.setCodAuxiliar(detalleFactura.getCodigoAuxiliar());
			    facturaDetalle.setDescripcion(detalleFactura.getDescripcion());
			    facturaDetalle.setCantidad(detalleFactura.getCantidad().doubleValue());
			    facturaDetalle.setPrecioUnitario(detalleFactura.getPrecioUnitario().doubleValue());
			    facturaDetalle.setDescuento(detalleFactura.getDescuento().doubleValue());
			    facturaDetalle.setPrecioTotalSinImpuesto(detalleFactura.getPrecioTotalSinImpuesto().doubleValue());
			    
			    facturaDetalle.setCodigoDocumento(document.getInfoTributaria().getCodDoc());
			    
			    session.save(facturaDetalle);
			    index++;
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
	public Object getInfo(){
		return this.document.getInfoFactura();
	}
	
	@Override
	public Detalles getInfoDetail() {
		return this.document.getDetalles();
	}
	
	@Override
	public void setData(Object obj) {
		this.document = (Factura)obj;
	}
}
