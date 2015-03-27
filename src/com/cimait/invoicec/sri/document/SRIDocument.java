package com.cimait.invoicec.sri.document;

import java.io.IOException;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.HashMap;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.hibernate.HibernateException;
import org.w3c.dom.Document;

import com.cimait.invoicec.bean.Emisor;
import com.cimait.invoicec.sri.schema.general.InfoTributaria;
import com.cimait.invoicec.sri.schema.invoice.Factura.InfoAdicional;

public abstract class SRIDocument {
	protected String inputFile;
	protected Emisor emite;
	
	public void setDataprovider(String inputFile) {
		this.inputFile = inputFile;
		try {
			init();
		} catch (Exception e) {
			System.out.println("Error en init() : " + e.getMessage());
		}
	}
	
	private void init() throws IOException {
		parseFile();
	}
	
	public void parseFile() throws IOException {
	}

	@Override
	public String toString() {
		StringWriter writer = new StringWriter();
		StreamResult result = new StreamResult(writer);
		try {
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer t = tf.newTransformer();
			DOMSource source = new DOMSource(getDocument());
			t.transform(source, result);
		} catch (Exception e) {
			System.out.println("Error al convertir documento a string : "
					+ e.getMessage());
			e.printStackTrace();
		}
		return writer.toString();
	}

	public Document getDocument() throws ParserConfigurationException,
			JAXBException {
		return null;
	}
	
	public String getRUC() {
		return null;
	}
	
	public String getNumber() {
		return null;
	}

	public String getEnv() {
		return null;
	}
	
	public void setExtraData(String[] nextLine){
	}
	
	public HashMap<String,String> getExtraData() {
		return null;
	}
	
	
	public String getCustomerRUC() {
		return null;
	}
	
	public String getCustomerName() {
		return null;
	}
	
	public String getCustomerIdentificationType() {
		return null;
	}
	
	public String getCustomerAddress() {
		return null;
	}
	
	public String getCustomerPhone() {
		return null;
	}
	
	public String getCustomerType() {
		return null;
	}
	
	public String getCustomerEmail() {
		return null;
	}
	
	public String getIssueDate() {
		return null;
	}
	
	public String getIssueType() {
		return null;
	}
	
	public String getType() {
		return null;
	}
	
	public Object getInfo(){
		return null;
	}
	
	public Object getInfoDetail() {
		return null;
	}
	public void saveInDB() throws HibernateException, SQLException, Exception {
	}
	
	public void setEmisor(Emisor emite) {
	}
	
	public Object getImpuestos() {
		return null;
	}
	
	public Object getInfoAdicional() {
		return null;
	}
	
	public InfoTributaria getInfoTributaria() {
		return null;
	}
	
	public void setData(Object obj) {
		
	}
}
