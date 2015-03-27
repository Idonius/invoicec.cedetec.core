package com.cimait.invoicec.ws;

import java.io.ByteArrayInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.HandlerResolver;
import javax.xml.ws.handler.PortInfo;
import javax.xml.ws.soap.SOAPBinding;

import org.apache.commons.io.FilenameUtils;

import pe.gob.sunat.service.SendBillResponse;
import pe.gob.sunat.servicio.registro.comppago.factura.gem.service.BillService;
import pe.gob.sunat.service.SendSummaryResponse;
import pe.gob.sunat.service.GetStatusResponse;


public class SunatWS {
	private String urlDestination = "";
	private String userName = "";
	private String password = "";
	private String fileName = "";
	private String qName = "http://service.gem.factura.comppago.registro.servicio.sunat.gob.pe/";
	private String serviceName = "billService";

	WSSESOAPHandler wsse = null;
	WSSESOAPHandlerResolver wsseHR = null;

	public SunatWS(String url, String qName, String serviceName, String usr,
			String psw, String fileName) {
		this.urlDestination = url;
		this.userName = usr;
		this.password = psw;
		this.fileName = fileName;
		this.qName = qName;
		this.serviceName = serviceName;
		this.wsse = new WSSESOAPHandler();
		this.wsse.setWSSE(this.userName, this.password, this.fileName);
		this.wsseHR = new WSSESOAPHandlerResolver(this.wsse);
	}

	public SendBillResponse sendDocument() throws MalformedURLException {

		// dump soap request
		// System.setProperty("com.sun.xml.ws.transport.http.client.HttpTransportPipe.dump",
		// "true");
		// System.setProperty("com.sun.xml.internal.ws.transport.http.client.HttpTransportPipe.dump",
		// "true");
		// System.setProperty("com.sun.xml.ws.transport.http.HttpAdapter.dump",
		// "true");
		// System.setProperty("com.sun.xml.internal.ws.transport.http.HttpAdapter.dump",
		// "true");
		//

		SendBillResponse response = new SendBillResponse();

		Service service = Service.create(new URL(this.urlDestination),
				new QName(this.qName, this.serviceName));
		service.setHandlerResolver(this.wsseHR);

		BillService port = service.getPort(BillService.class);

		SOAPBinding binding = (SOAPBinding) ((BindingProvider) port)
				.getBinding();
		binding.setMTOMEnabled(true);

		DataHandler dh = null;
		String dummy = "dummy"; //real datahandler happens in soap handler.
		dh = new DataHandler(dummy.getBytes(), "application/octet-stream");
		String fileNameNoPath = FilenameUtils.getName(this.fileName);
		response.setApplicationResponse(port.sendBill(fileNameNoPath, dh));
		//System.out.println(response);
		return response;
	}
	
	public SendSummaryResponse sendSummary() throws MalformedURLException {
		SendSummaryResponse response = new SendSummaryResponse();

		Service service = Service.create(new URL(this.urlDestination),
				new QName(this.qName, this.serviceName));
		service.setHandlerResolver(this.wsseHR);

		BillService port = service.getPort(BillService.class);

		SOAPBinding binding = (SOAPBinding) ((BindingProvider) port)
				.getBinding();
		binding.setMTOMEnabled(true);

		DataHandler dh = null;
		String dummy = "dummy"; //real datahandler happens in soap handler.
		dh = new DataHandler(dummy.getBytes(), "application/octet-stream");
		String fileNameNoPath = FilenameUtils.getName(this.fileName);
		response.setTicket(port.sendSummary(fileNameNoPath, dh));
		//System.out.println(response);
		return response;
		
	}
	
	
	public GetStatusResponse getStatus(String ticket) throws MalformedURLException {
		GetStatusResponse response = new GetStatusResponse();

		Service service = Service.create(new URL(this.urlDestination),
				new QName(this.qName, this.serviceName));
		service.setHandlerResolver(this.wsseHR);

		BillService port = service.getPort(BillService.class);

		SOAPBinding binding = (SOAPBinding) ((BindingProvider) port)
				.getBinding();
		binding.setMTOMEnabled(true);

		DataHandler dh = null;
		String dummy = "dummy"; //real datahandler happens in soap handler.
		dh = new DataHandler(dummy.getBytes(), "application/octet-stream");
		String fileNameNoPath = FilenameUtils.getName(this.fileName);
		response.setStatus(port.getStatus(ticket));
		//System.out.println(response);
		return response;
	}
	
}
