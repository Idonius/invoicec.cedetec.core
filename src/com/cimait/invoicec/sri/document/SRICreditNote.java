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
import com.cimait.invoicec.sri.schema.creditnote.Impuesto;
import com.cimait.invoicec.sri.schema.creditnote.NotaCredito;
import com.cimait.invoicec.sri.schema.creditnote.NotaCredito.Detalles.Detalle;
import com.cimait.invoicec.sri.schema.creditnote.NotaCredito.Detalles.Detalle.DetallesAdicionales;
import com.cimait.invoicec.sri.schema.creditnote.NotaCredito.Detalles.Detalle.Impuestos;
import com.cimait.invoicec.sri.schema.creditnote.NotaCredito.InfoAdicional;
import com.cimait.invoicec.sri.schema.creditnote.NotaCredito.InfoAdicional.CampoAdicional;
import com.cimait.invoicec.sri.schema.creditnote.NotaCredito.InfoNotaCredito;
import com.cimait.invoicec.sri.schema.creditnote.TotalConImpuestos;
import com.cimait.invoicec.sri.schema.creditnote.TotalConImpuestos.TotalImpuesto;
import com.cimait.invoicec.sri.schema.general.InfoTributaria;
import com.cimait.invoicec.sri.schema.invoice.Factura;
import com.cimait.invoicec.sri.schema.creditnote.NotaCredito.Detalles;
import com.sun.directory.examples.ModifyDocumentAcceso;

public class SRICreditNote extends SRIDocument {
	private NotaCredito document = null;
	private HashMap<String, String> extraData = null;
	private HashMap<Integer, String> extraDataDetail = null;

	@Override
	public void parseFile() throws IOException {
		CSVReader reader = new CSVReader(new FileReader(inputFile), '|');
		String[] nextLine;
		this.document = new NotaCredito();

		// Acumular detalles
		NotaCredito.Detalles notaCreditoDetalles = new NotaCredito.Detalles();
		List<Detalle> lDetalleNotaCredito = new ArrayList<Detalle>();

		this.document.setId("comprobante");
		this.document.setVersion("1.1.0");

		// Acumular Informacion Adicional
		InfoAdicional infoAdicional = new InfoAdicional();
		List<CampoAdicional> lCampoAdicional = new ArrayList<NotaCredito.InfoAdicional.CampoAdicional>();

		this.extraDataDetail = new HashMap<Integer, String>();

		try {
			while ((nextLine = reader.readNext()) != null) {
				if (nextLine[0].equals("IT")) {
					this.document
							.setInfoTributaria(getInfoTributaria(nextLine));
				} else if (nextLine[0].equals("CC")) {
					this.document
							.setInfoNotaCredito(getInfoNotaCredito(nextLine));
					setExtraData(nextLine);

					CampoAdicional cAdicional = new CampoAdicional();
					cAdicional.setNombre("Folio Interno");
					cAdicional.setValue(extraData.get("QADCodFactura"));
					lCampoAdicional.add(cAdicional);

					cAdicional = new CampoAdicional();
					cAdicional.setNombre("Monto en Texto");
					cAdicional.setValue("Monto en Texto : " + extraData.get("MontoTexto"));
					lCampoAdicional.add(cAdicional);

				} else if (nextLine[0].equals("DC")) {
					lDetalleNotaCredito.add(getDetalles(nextLine));

					this.extraDataDetail.put(lDetalleNotaCredito.size() - 1,
							nextLine[1]);
				} else if (nextLine[0].equals("IA")) {
					lCampoAdicional.add(getInfoAdicional(nextLine));
				}
			}

			notaCreditoDetalles.getDetalle().addAll(lDetalleNotaCredito);
			this.document.setDetalles(notaCreditoDetalles);

			infoAdicional.getCampoAdicional().addAll(lCampoAdicional);
			this.document.setInfoAdicional(infoAdicional);
		} catch (Exception e) {
			System.out.println("error " + e.getMessage());
			e.printStackTrace();
		}
		reader.close();
	}

	@Override
	public void setEmisor(Emisor emite) {
		this.emite = emite;
		try {
			this.document.getInfoTributaria().setClaveAcceso(
					ModifyDocumentAcceso.generarClaveAcceso(this.emite));
			emite.getInfEmisor().setClaveAcceso(
					this.document.getInfoTributaria().getClaveAcceso());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void setExtraData(String[] nextLine) {
		this.extraData = new HashMap<String, String>();
		this.extraData.put("QADCodFactura", nextLine[28]);
		this.extraData.put("TelfCliente", nextLine[29]);
		this.extraData.put("DirCliente", nextLine[30]);
		this.extraData.put("MontoTexto", nextLine[31]);
		this.extraData.put("CorreoCliente", nextLine[32]);
		this.extraData.put("CorreoInterno", nextLine[33]);
		this.extraData.put("QADCodCliente", nextLine[34]);

	}

	@Override
	public Object getInfo(){
		return this.document.getInfoNotaCredito();
	}
	
	@Override
	public InfoTributaria getInfoTributaria() {
		return this.document.getInfoTributaria();
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

	public InfoNotaCredito getInfoNotaCredito(String[] nextLine) {

		InfoNotaCredito infoNotaCredito = new InfoNotaCredito();
		infoNotaCredito.setFechaEmision(nextLine[1]);
		infoNotaCredito.setTipoIdentificacionComprador(nextLine[3]);
		infoNotaCredito.setDirEstablecimiento(nextLine[2]);
		infoNotaCredito.setRazonSocialComprador(nextLine[4]);
		infoNotaCredito.setIdentificacionComprador(nextLine[5]);
		infoNotaCredito.setContribuyenteEspecial(nextLine[6]);
		infoNotaCredito.setObligadoContabilidad(nextLine[7]);
		// infoNotaCredito.setRise(nextLine[8]);
		infoNotaCredito.setCodDocModificado((nextLine[9]));
		infoNotaCredito.setNumDocModificado((nextLine[10]));
		infoNotaCredito.setFechaEmisionDocSustento((nextLine[11]));
		infoNotaCredito
				.setTotalSinImpuestos(new BigDecimal(nextLine[12].trim()));
		infoNotaCredito
				.setValorModificacion(new BigDecimal(nextLine[13].trim()));
		infoNotaCredito.setMoneda(nextLine[27]);

		TotalConImpuestos totalConImpuestos = new TotalConImpuestos();
		List<TotalImpuesto> lTotalImpuestos = new ArrayList<TotalImpuesto>();

		TotalImpuesto totalImpuesto = new TotalImpuesto();
		totalImpuesto.setCodigo(nextLine[18]);
		totalImpuesto.setCodigoPorcentaje(nextLine[19]);
		totalImpuesto.setBaseImponible(new BigDecimal(nextLine[20].trim()));
		totalImpuesto.setValor(new BigDecimal(nextLine[21].trim()));

		if (totalImpuesto.getValor().doubleValue() != 0.0) {
			lTotalImpuestos.add(totalImpuesto);
		}

		totalImpuesto = new TotalImpuesto();
		totalImpuesto.setCodigo(nextLine[14]);
		totalImpuesto.setCodigoPorcentaje(nextLine[15]);
		totalImpuesto.setBaseImponible(new BigDecimal(nextLine[16].trim()));
		totalImpuesto.setValor(new BigDecimal(nextLine[17].trim()));
		if (totalImpuesto.getValor().doubleValue() != 0.0) {
			lTotalImpuestos.add(totalImpuesto);
		}
		totalConImpuestos.getTotalImpuesto().addAll(lTotalImpuestos);

		infoNotaCredito.setTotalConImpuestos(totalConImpuestos);

		infoNotaCredito.setMotivo(nextLine[22]);

		return infoNotaCredito;
	}

	public Detalle getDetalles(String[] nextLine) {

		Detalle detalle = new Detalle();
		detalle.setCodigoInterno(nextLine[2]);
		detalle.setCodigoAdicional(nextLine[3]);
		detalle.setDescripcion(nextLine[4]);
		detalle.setCantidad(new BigDecimal(nextLine[5].trim()));
		detalle.setPrecioUnitario(new BigDecimal(nextLine[6].trim()));

		if (!nextLine[7].trim().equals("0.00")) {
			detalle.setDescuento(new BigDecimal(nextLine[7].trim()));
		}
		detalle.setPrecioTotalSinImpuesto(new BigDecimal(nextLine[8].trim()));

		DetallesAdicionales detallesAdicionales = new DetallesAdicionales();

		List<DetallesAdicionales.DetAdicional> lDetallesAdicionales = new ArrayList<NotaCredito.Detalles.Detalle.DetallesAdicionales.DetAdicional>();

		DetallesAdicionales.DetAdicional detAdicional = new DetallesAdicionales.DetAdicional();
		if (!nextLine[9].trim().equals("")) {
			detAdicional.setNombre(nextLine[9].trim());
			lDetallesAdicionales.add(detAdicional);

			detAdicional = new DetallesAdicionales.DetAdicional();
			detAdicional.setNombre(nextLine[10].trim());
			lDetallesAdicionales.add(detAdicional);

			detAdicional = new DetallesAdicionales.DetAdicional();
			detAdicional.setNombre(nextLine[11].trim());
			lDetallesAdicionales.add(detAdicional);

			detallesAdicionales.getDetAdicional().addAll(lDetallesAdicionales);

			detalle.setDetallesAdicionales(detallesAdicionales);
		}

		Impuestos impuestos = new Impuestos();

		List<Impuesto> lImpuesto = new ArrayList<Impuesto>();

		Impuesto impuesto = new Impuesto();
		impuesto.setCodigo(nextLine[17]);
		impuesto.setCodigoPorcentaje(nextLine[18]);
		impuesto.setTarifa(new BigDecimal(nextLine[19].trim()));
		impuesto.setBaseImponible(new BigDecimal(nextLine[20].trim()));
		impuesto.setValor(new BigDecimal(nextLine[21].trim()));

		if (impuesto.getValor().doubleValue() != 0.0) {
			lImpuesto.add(impuesto);
		}

		impuesto = new Impuesto();
		impuesto.setCodigo(nextLine[12]);
		impuesto.setCodigoPorcentaje(nextLine[13]);
		impuesto.setTarifa(new BigDecimal(nextLine[14].trim()));
		impuesto.setBaseImponible(new BigDecimal(nextLine[15].trim()));
		impuesto.setValor(new BigDecimal(nextLine[16].trim()));
		if (impuesto.getValor().doubleValue() != 0.0) {
			lImpuesto.add(impuesto);
		}
		impuestos.getImpuesto().addAll(lImpuesto);

		detalle.setImpuestos(impuestos);

		return detalle;
	}

	public CampoAdicional getInfoAdicional(String[] nextLine) {

		CampoAdicional campoAdicional = new CampoAdicional();
		campoAdicional.setNombre(nextLine[1]);
		campoAdicional.setValue(nextLine[2]);
		return campoAdicional;
	}

	@Override
	public InfoAdicional getInfoAdicional() {
		return this.document.getInfoAdicional();
	}
	
	@Override
	public String getCustomerRUC() {
		return this.document.getInfoNotaCredito().getIdentificacionComprador();
	}

	@Override
	public String getCustomerName() {
		return this.document.getInfoNotaCredito().getRazonSocialComprador();
	}

	@Override
	public String getCustomerIdentificationType() {
		return this.document.getInfoNotaCredito()
				.getTipoIdentificacionComprador();
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
	public Document getDocument() throws ParserConfigurationException,
			JAXBException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = db.newDocument();

		JAXBContext jc = JAXBContext.newInstance(NotaCredito.class);
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
		return this.document.getInfoTributaria().getEstab().trim()
				+ this.document.getInfoTributaria().getPtoEmi().trim()
				+ this.document.getInfoTributaria().getSecuencial();
	}

	@Override
	public HashMap<String, String> getExtraData() {
		return this.extraData;
	}

	@Override
	public String getIssueDate() {
		return this.document.getInfoNotaCredito().getFechaEmision();
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
	public String getEnv() {
		return this.document.getInfoTributaria().getAmbiente();
	}

	@Override
	public void saveInDB() throws Exception {
		Session session = DBDataSource.getInstance().getFactory().openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();

			// si existe lo borro
			// detalles
			Query qry = session
					.createQuery("delete FacDetDocumentosEntity where ruc = :RUC and codEstablecimiento = :CODESTABLECIMIENTO and codPuntEmision = :CODPUNTOEMISION and secuencial = :SECUENCIAL and codigoDocumento = :CODIGODOCUMENTO");
			qry.setParameter("RUC", document.getInfoTributaria().getRuc());
			qry.setParameter("CODESTABLECIMIENTO", document.getInfoTributaria()
					.getEstab());
			qry.setParameter("CODPUNTOEMISION", document.getInfoTributaria()
					.getPtoEmi());
			qry.setParameter("SECUENCIAL", document.getInfoTributaria()
					.getSecuencial());
			qry.setParameter("CODIGODOCUMENTO", document.getInfoTributaria()
					.getCodDoc());
			qry.executeUpdate();
			// cabecera
			qry = session
					.createQuery("delete FacCabDocumentosEntity where ambiente = :AMBIENTE and ruc = :RUC and codEstablecimiento = :CODESTABLECIMIENTO and codPuntEmision = :CODPUNTOEMISION and secuencial = :SECUENCIAL and codigoDocumento = :CODIGODOCUMENTO");
			qry.setParameter("AMBIENTE", Integer.parseInt(document
					.getInfoTributaria().getAmbiente()));
			qry.setParameter("RUC", document.getInfoTributaria().getRuc());
			qry.setParameter("CODESTABLECIMIENTO", document.getInfoTributaria()
					.getEstab());
			qry.setParameter("CODPUNTOEMISION", document.getInfoTributaria()
					.getPtoEmi());
			qry.setParameter("SECUENCIAL", document.getInfoTributaria()
					.getSecuencial());
			qry.setParameter("CODIGODOCUMENTO", document.getInfoTributaria()
					.getCodDoc());
			qry.executeUpdate();

			FacCabDocumentosEntity notaCredito = new FacCabDocumentosEntity();
			notaCredito.setAmbiente(Integer.parseInt(document
					.getInfoTributaria().getAmbiente()));
			notaCredito.setRuc(document.getInfoTributaria().getRuc());
			// factura.setTipoIdentificacion();
			notaCredito.setCodEstablecimiento(document.getInfoTributaria()
					.getEstab());
			notaCredito.setCodPuntEmision(document.getInfoTributaria()
					.getPtoEmi());
			notaCredito.setSecuencial(document.getInfoTributaria()
					.getSecuencial());
			notaCredito.setClaveAcceso(document.getInfoTributaria()
					.getClaveAcceso());
			SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
			Date dt1 = null;
			try {
				dt1 = df.parse(document.getInfoNotaCredito().getFechaEmision());
			} catch (ParseException e) {
				e.printStackTrace();
			}

			notaCredito.setFechaEmision(new java.sql.Date(dt1.getTime()));
			// notaCredito.setGuiaRemision(document.getInfoNotaCredito().getGuiaRemision());
			notaCredito.setRazonSocialComprador(document.getInfoNotaCredito()
					.getRazonSocialComprador());
			notaCredito.setIdentificacionComprador(document
					.getInfoNotaCredito().getIdentificacionComprador());
			notaCredito.setTotalSinImpuesto(document.getInfoNotaCredito()
					.getTotalSinImpuestos().doubleValue());
			// notaCredito.setTotalDescuento(document.getInfoNotaCredito().getTotalDescuento().doubleValue());
			notaCredito.setEmail(this.extraData.get("CorreoCliente")); // deberia
																		// estar
																		// en
																		// InfoAdicional
																		// ?
			// notaCredito.setPropina(document.getInfoFactura().getPropina().doubleValue());
			notaCredito.setMoneda(document.getInfoNotaCredito().getMoneda());

			notaCredito.setRise(document.getInfoNotaCredito().getRise());

			dt1 = null;
			try {
				dt1 = df.parse(document.getInfoNotaCredito()
						.getFechaEmisionDocSustento());
			} catch (ParseException e) {
				e.printStackTrace();
			}

			notaCredito.setFechaEmisionDocSustento(new java.sql.Date(dt1
					.getTime()));
			notaCredito.setMotivoRazon(document.getInfoNotaCredito()
					.getMotivo());

			notaCredito.setAutorizacion("");
			notaCredito.setFechaautorizacion(null);
			notaCredito.setImporteTotal(document.getInfoNotaCredito()
					.getValorModificacion().doubleValue());
			notaCredito.setCodigoDocumento(document.getInfoTributaria()
					.getCodDoc());

			notaCredito.setIsActive("Y");
			notaCredito.setEstadoTransaccion("");
			notaCredito.setMsjError("");
			notaCredito.setTipo("");
			notaCredito.setClaveAccesoContigente("");
			notaCredito.setClaveContingencia("");
			notaCredito.setDocuAutorizacion("");
			notaCredito.setInfoAdicional(this.extraData.get("CorreoInterno"));
			session.save(notaCredito);

			int index = 0;
			for (Detalle detalleNotaCredito : document.getDetalles()
					.getDetalle()) {
				FacDetDocumentosEntity notaCreditoDetalle = new FacDetDocumentosEntity();

				notaCreditoDetalle
						.setRuc(document.getInfoTributaria().getRuc());
				notaCreditoDetalle.setCodEstablecimiento(document
						.getInfoTributaria().getEstab());
				notaCreditoDetalle.setCodPuntEmision(document
						.getInfoTributaria().getPtoEmi());
				notaCreditoDetalle.setSecuencial(document.getInfoTributaria()
						.getSecuencial());

				notaCreditoDetalle.setSecuencialDetalle(Integer
						.parseInt(this.extraDataDetail.get(index)));
				notaCreditoDetalle.setCodPrincipal(detalleNotaCredito
						.getCodigoInterno());
				notaCreditoDetalle.setCodAuxiliar(detalleNotaCredito
						.getCodigoAdicional());
				notaCreditoDetalle.setDescripcion(detalleNotaCredito
						.getDescripcion());
				notaCreditoDetalle.setCantidad(detalleNotaCredito.getCantidad()
						.doubleValue());
				notaCreditoDetalle.setPrecioUnitario(detalleNotaCredito
						.getPrecioUnitario().doubleValue());

				if (detalleNotaCredito.getDescuento() != null) {
					notaCreditoDetalle.setDescuento(detalleNotaCredito
							.getDescuento().doubleValue());
				}
				notaCreditoDetalle.setPrecioTotalSinImpuesto(detalleNotaCredito
						.getPrecioTotalSinImpuesto().doubleValue());

				notaCreditoDetalle.setCodigoDocumento(document
						.getInfoTributaria().getCodDoc());
				session.save(notaCreditoDetalle);
				index++;
			}

			tx.commit();
		} catch (HibernateException e) {
			if (tx != null)
				tx.rollback();
			e.printStackTrace();
			throw new Exception();
		} finally {
			session.close();
		}
	}
	
	@Override
	public Detalles getInfoDetail() {
		return this.document.getDetalles();
	}
	
	@Override
	public void setData(Object obj) {
		this.document = (NotaCredito)obj;
	}
	
}
