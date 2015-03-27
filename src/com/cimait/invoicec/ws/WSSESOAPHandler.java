package com.cimait.invoicec.ws;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPHeader;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FilenameUtils;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.sun.xml.internal.messaging.saaj.util.ByteInputStream;

public class WSSESOAPHandler implements SOAPHandler<SOAPMessageContext> {
	private String userName = "";
	private String password = "";
	private String fileName = "";
	
	public void setWSSE(String usr, String psw,  String fileName) {
		this.userName = usr;
		this.password = psw;
		this.fileName = fileName;
	}
	
	public static byte[] loadFile(File file) throws IOException {
		InputStream is = new FileInputStream(file);
		long length = file.length();
		byte[] bytes = new byte[(int)length];
		int offset = 0;
		int numRead = 0;
		while (offset < bytes.length && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
				offset += numRead;
		}
		is.close();
		if (offset < bytes.length) {
			throw new IOException("no se puede leer completamente el archivo "+file.getName());
		}
		return bytes;
	}
	
	@Override
	public boolean handleMessage(SOAPMessageContext context) {
		String prefixUri = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd";
		Boolean outboundProperty = (Boolean) context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
		if (outboundProperty.booleanValue()) {
			try {
				SOAPEnvelope envelope = context.getMessage().getSOAPPart().getEnvelope();
				SOAPFactory factory = SOAPFactory.newInstance();
                String prefix = "wsse";
                SOAPElement securityElem =factory.createElement("Security",prefix, prefixUri);;
                SOAPElement tokenElem =  factory.createElement("UsernameToken",prefix,prefixUri);
                SOAPElement userElem =  factory.createElement("Username",prefix,prefixUri);
                userElem.addTextNode(this.userName);
                SOAPElement pwdElem =factory.createElement("Password",prefix,prefixUri);
                pwdElem.addTextNode(this.password);
                tokenElem.addChildElement(userElem);
                tokenElem.addChildElement(pwdElem);
                securityElem.addChildElement(tokenElem);
                if (envelope.getHeader() != null) {
                	envelope.getHeader().detachNode();
                }
                SOAPHeader header = envelope.addHeader();
                header.addChildElement(securityElem);
                
                //manual attachment
                if (!this.fileName.trim().equals("")) {
		                AttachmentPart attachment = context.getMessage().createAttachmentPart();
		                byte[] encoded = Base64.encodeBase64(loadFile(new File(this.fileName)));
		                ByteInputStream bis = new ByteInputStream(encoded,0,encoded.length);
		                attachment.setBase64Content(bis, "application/zip");
		                attachment.setContentId(FilenameUtils.getName(this.fileName));
		                context.getMessage().addAttachmentPart(attachment);
	
		                //add cid
		                SOAPBody  body = envelope.getBody();
		                NodeList nodeList = body.getElementsByTagNameNS("", "contentFile");
		                Element element =   (Element) nodeList.item(0);
		                SOAPBodyElement bodyElement = (SOAPBodyElement) element;
		                bodyElement.removeContents();
		                bodyElement.addChildElement("Include", "xop", "http://www.w3.org/2004/08/xop/include").setAttribute("href", "cid:" + FilenameUtils.getName(this.fileName));
		                //end
                }
			} catch (SOAPException e) {
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return true;
	}

	@Override
	public boolean handleFault(SOAPMessageContext context) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void close(MessageContext context) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Set<QName> getHeaders() {
		// TODO Auto-generated method stub
		return null;
	}



}
