package com.cimait.invoicec.document;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public final class DocumentUtil {

	public static String printDocument(Document doc) throws IOException, TransformerException {
	    TransformerFactory tf = TransformerFactory.newInstance();
	    Transformer transformer = tf.newTransformer();
	    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
	    transformer.setOutputProperty(OutputKeys.METHOD, "xml");
	    transformer.setOutputProperty(OutputKeys.INDENT, "no");
	    transformer.setOutputProperty(OutputKeys.ENCODING, "ISO-8859-1");
	    StringWriter writer = new StringWriter();
	    transformer.transform(new DOMSource(doc),   new StreamResult(writer));
	    return writer.getBuffer().toString();
	}

	public static byte[]  getBytes(Document doc) throws TransformerException {
	TransformerFactory tf = TransformerFactory.newInstance();
    Transformer trans = tf.newTransformer();
    //trans.setOutputProperty(OutputKeys.ENCODING, "ISO-8859-1");
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    StreamResult result = new StreamResult(bos);
    trans.transform(new DOMSource(doc),  result);
    return bos.toByteArray();
	}
	
	
	public static void saveDocumentToFile(Document doc, String path) {
		try {
            File file = new File(path);
            FileOutputStream fop = new FileOutputStream(file);

            if (!file.exists()) {
                file.createNewFile();
            } 
            fop.write(DocumentUtil.getBytes(doc));
            fop.flush();
            fop.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static XMLGregorianCalendar getXMLGregorianDate(String strDate) throws DatatypeConfigurationException, ParseException {
		DatatypeFactory datatypeFactory = DatatypeFactory.newInstance();
        GregorianCalendar calendar = new GregorianCalendar(TimeZone.getTimeZone("America/Lima"));
        calendar.clear();

        Calendar parsedCalendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat( "yyyy-MM-dd");
        Date rawDate = sdf.parse( strDate );
        parsedCalendar.setTime( rawDate );

        calendar.set( parsedCalendar.get( Calendar.YEAR ),
                        parsedCalendar.get( Calendar.MONTH ),
                        parsedCalendar.get( Calendar.DATE ) );
        XMLGregorianCalendar xmlCalendar = datatypeFactory.newXMLGregorianCalendarDate( calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)+1, calendar.get(Calendar.DAY_OF_MONTH), DatatypeConstants.FIELD_UNDEFINED );
        xmlCalendar.setTimezone( DatatypeConstants.FIELD_UNDEFINED );
		return xmlCalendar;

	}
	
	public static Document  removeNSPrefix(Document doc , String prefix) {
		Element originalDocumentElement = doc.getDocumentElement();
		Element newDocumentElement = null ;
		
		if (prefix.trim().equals("01") || prefix.trim().equals("03")) { 
			newDocumentElement = doc.createElementNS("urn:oasis:names:specification:ubl:schema:xsd:Invoice-2",originalDocumentElement.getNodeName());
			newDocumentElement.setAttribute("xmlns","urn:oasis:names:specification:ubl:schema:xsd:Invoice-2" );
		} else if (prefix.trim().equals("07")) {
			newDocumentElement = doc.createElementNS("urn:oasis:names:specification:ubl:schema:xsd:CreditNote-2",originalDocumentElement.getNodeName());
			newDocumentElement.setAttribute("xmlns","urn:oasis:names:specification:ubl:schema:xsd:CreditNote-2" );
		} else if (prefix.trim().equals("08")) {
			newDocumentElement = doc.createElementNS("urn:oasis:names:specification:ubl:schema:xsd:DebitNote-2",originalDocumentElement.getNodeName());
			newDocumentElement.setAttribute("xmlns","urn:oasis:names:specification:ubl:schema:xsd:DebitNote-2" );
		} else if (prefix.trim().equals("RA")) {
			newDocumentElement = doc.createElementNS("urn:sunat:names:specification:ubl:peru:schema:xsd:VoidedDocuments-1",originalDocumentElement.getNodeName());
			newDocumentElement.setAttribute("xmlns","urn:sunat:names:specification:ubl:peru:schema:xsd:VoidedDocuments-1" );
		}
		newDocumentElement.setAttribute("xmlns:cac","urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2" );
		newDocumentElement.setAttribute("xmlns:cbc","urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2" );
		
		
		if (!prefix.trim().equals("RA")) newDocumentElement.setAttribute("xmlns:ccts","urn:un:unece:uncefact:documentation:2" );
		newDocumentElement.setAttribute("xmlns:ds","http://www.w3.org/2000/09/xmldsig#" );
		newDocumentElement.setAttribute("xmlns:ext","urn:oasis:names:specification:ubl:schema:xsd:CommonExtensionComponents-2" );
		if (!prefix.trim().equals("RA"))  newDocumentElement.setAttribute("xmlns:qdt","urn:oasis:names:specification:ubl:schema:xsd:QualifiedDatatypes-2" );
		newDocumentElement.setAttribute("xmlns:sac","urn:sunat:names:specification:ubl:peru:schema:xsd:SunatAggregateComponents-1" );
		if (!prefix.trim().equals("RA"))  newDocumentElement.setAttribute("xmlns:udt","urn:un:unece:uncefact:data:specification:UnqualifiedDataTypesSchemaModule:2" );
		newDocumentElement.setAttribute("xmlns:xsi","http://www.w3.org/2001/XMLSchema-instance" );
		
		newDocumentElement.setPrefix("");
		NodeList list = originalDocumentElement.getChildNodes();
		while(list.getLength()!=0) {
			newDocumentElement.appendChild(list.item(0));
		}
		doc.replaceChild(newDocumentElement,originalDocumentElement);
		return doc;
	}
	
	public static void saveToZIP(String path, String fileName, ByteArrayInputStream byteArrayInputStream) throws IOException {
		int bufferSize = 2048;
		FileOutputStream dest = new  FileOutputStream(path + FilenameUtils.removeExtension(fileName) + ".ZIP");
	    ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));
	    out.setMethod(ZipOutputStream.DEFLATED);
	    byte data[] = new byte[bufferSize];
	    ZipEntry entry = new ZipEntry(fileName.toUpperCase());
	    out.putNextEntry(entry);
	    int count = 0;
	    while ((count = byteArrayInputStream.read(data,0,bufferSize))!= -1){
		    out.write(data,0,count);
	    }
	    byteArrayInputStream.close();
	    out.close();
	}
	
	public static byte[]  readFromZip(String fileName) throws IOException {
		int bufferSize = 2048;
		ByteArrayOutputStream dest = new ByteArrayOutputStream();
        BufferedInputStream is = null;
        ZipEntry entry;
        ZipFile zipfile = new ZipFile(fileName);
        Enumeration e = zipfile.entries();
        byte[] bytes = null;
        while (e.hasMoreElements()) {
        	entry = (ZipEntry) e.nextElement();
        	bytes = IOUtils.toByteArray(zipfile.getInputStream(entry)); 
        }
        zipfile.close();
        return bytes;
	}
	
	
	public static Document getDOMFromBytes(byte[] bytes) throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	    factory.setNamespaceAware(true);
	    DocumentBuilder builder = factory.newDocumentBuilder();
	    return builder.parse(new ByteArrayInputStream(bytes));
	}
	
}
