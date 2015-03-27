package com.cimait.invoicec.interfaces;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;

import au.com.bytecode.opencsv.CSVReader;

public class ReaderVoided extends GenericReaderDocument{

	private HashMap<String,Object> invoiceData;
	private HashMap<String,Object> invoiceStruct;
	private HashMap<String,Object> InvoiceHeader;
	private HashMap<String,Object> InvoiceDetail;
	ArrayList<HashMap<String,Object>> InvoiceDetailFieldList;

	public ReaderVoided(){
		
	}
	@Override
	public void readCSV(String file) {

		InvoiceDetailFieldList = new ArrayList<HashMap<String,Object>>();
		invoiceData = new HashMap<String, Object>();
		invoiceStruct = new HashMap<String, Object>();
		int contador = 0;
		try{
			CSVReader reader = new CSVReader(new FileReader(file),'|');
		    String [] nextLine;
		    while ((nextLine = reader.readNext()) != null) {
		    	contador ++;
		    	if(nextLine[0].equals("VC")){
		    		cabeceraInvoice(nextLine);
		    	}else if(nextLine[0].equals("VD")){
		    		detalleInvoice(nextLine);
		    		InvoiceDetailFieldList.add(InvoiceDetail);
		    	}
		    }
		
		    invoiceData.put("VC",InvoiceHeader);
		    invoiceData.put("VD",InvoiceDetailFieldList);
		     
		    invoiceStruct.put("RA",invoiceData);
		    reader.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	private void detalleInvoice(String[] nextLineOut) {
		InvoiceDetail = new HashMap<String, Object>();
		InvoiceDetail.put("ItemOrden",nextLineOut[1].trim());
		InvoiceDetail.put("ItemTipoDocumento",nextLineOut[2].trim());
		InvoiceDetail.put("ItemSerieDocumento",nextLineOut[3].trim());
		InvoiceDetail.put("ItemDocumentoCorrelativo",nextLineOut[4].trim());
		InvoiceDetail.put("ItemMotivoBaja",nextLineOut[5].trim());
	
	}
	private void cabeceraInvoice(String[] nextLineOut) {
		InvoiceHeader = new HashMap<String, Object>();
		
		InvoiceHeader.put("CustomerRuc", nextLineOut[1].trim());
		InvoiceHeader.put("CustomerTipoDocumento", nextLineOut[2].trim());
		InvoiceHeader.put("CustomerRazonSocial", nextLineOut[3].trim());
		InvoiceHeader.put("ReferenceDate", nextLineOut[4].trim());
		InvoiceHeader.put("InvoiceNumeracion", nextLineOut[5].trim());
		InvoiceHeader.put("IssueDate", nextLineOut[6].trim());
	}
	
	@Override
	public HashMap<String, Object> getData() {
		// TODO Auto-generated method stub
		return invoiceStruct;
	}

}
