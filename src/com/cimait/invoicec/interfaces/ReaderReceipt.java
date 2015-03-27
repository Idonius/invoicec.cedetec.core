package com.cimait.invoicec.interfaces;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;

import au.com.bytecode.opencsv.*;
 
public class ReaderReceipt extends GenericReaderDocument {

	private HashMap<String,Object> invoiceData;
	private HashMap<String,Object> invoiceStruct;
	private HashMap<String,Object> InvoiceHeader;
	private HashMap<String,Object> InvoiceDetail;
	ArrayList<HashMap<String,Object>> InvoiceDetailFieldList;
	
	public ReaderReceipt(){
		
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
		    	System.out.println("Linea:"+contador+" CodigoLinea:"+nextLine[0]);
		    	
		    	if(nextLine[0].equals("CB")){
		    		cabeceraInvoice(nextLine);
		    	}else if(nextLine[0].equals("DB")){
		    		detalleInvoice(nextLine);
		    		InvoiceDetailFieldList.add(InvoiceDetail);
		    	}
		    }
		
		    invoiceData.put("CB",InvoiceHeader);
		    invoiceData.put("DB",InvoiceDetailFieldList);
		     
		    invoiceStruct.put("03",invoiceData);
		    
		}catch(Exception e){}
		
	}

	private void detalleInvoice(String[] nextLineOut) {
		
		InvoiceDetail = new HashMap<String, Object>();
		InvoiceDetail.put("ItemOrden",nextLineOut[1]);
		InvoiceDetail.put("ItemProdCode",nextLineOut[2]);

		InvoiceDetail.put("ItemUnidadMedida",nextLineOut[3]);
		InvoiceDetail.put("itemCantidad",nextLineOut[4]);
		InvoiceDetail.put("itemDetalleServicio",nextLineOut[5]);
		InvoiceDetail.put("itemValorUnitario",nextLineOut[6]);
		InvoiceDetail.put("itemPrecioUnitVenta",nextLineOut[7]);
		InvoiceDetail.put("itemPrecioUnitCode",nextLineOut[8]);
		
		InvoiceDetail.put("itemIGVTotal",nextLineOut[9]);
		InvoiceDetail.put("itemIGVSubTotal",nextLineOut[10]);
		InvoiceDetail.put("itemIGVAfectacionCode",nextLineOut[11]);
		InvoiceDetail.put("itemIGVTributoCode",nextLineOut[12]);
		InvoiceDetail.put("itemIGVTributoName",nextLineOut[13]);
		InvoiceDetail.put("itemIGVTributoInterCode",nextLineOut[14]);
		
		InvoiceDetail.put("itemISCTotal",nextLineOut[15]);
		InvoiceDetail.put("itemISCSubTotal",nextLineOut[16]);
		InvoiceDetail.put("itemISCSistemaCode",nextLineOut[17]);
		InvoiceDetail.put("itemISCTributoCode",nextLineOut[18]);
		InvoiceDetail.put("itemISCTributoName",nextLineOut[19]);
		InvoiceDetail.put("itemISCTributoInterCode",nextLineOut[20]);
		
		InvoiceDetail.put("itemValorVenta",nextLineOut[21]);
		InvoiceDetail.put("itemReferenciasValor",nextLineOut[22]);
		InvoiceDetail.put("itemReferencialCode",nextLineOut[23]);
 		
	}

	private void cabeceraInvoice(String[] nextLineOut) {
		
		InvoiceHeader = new HashMap<String, Object>();

		
		InvoiceHeader.put("IssueDate", nextLineOut[1]);
		InvoiceHeader.put("CustomerRazonSocial", nextLineOut[2]);
		InvoiceHeader.put("CustomerNombreComercial", nextLineOut[3]);
		InvoiceHeader.put("AddressUbigeo", nextLineOut[4]);
		InvoiceHeader.put("AddressDireccion", nextLineOut[5]);
		InvoiceHeader.put("AddressUrbanizacion", nextLineOut[6]);
		InvoiceHeader.put("AddressProvincia", nextLineOut[7]);
		InvoiceHeader.put("AddressDepartamento", nextLineOut[8]);
		InvoiceHeader.put("AddressDistrito", nextLineOut[9]);
		InvoiceHeader.put("AddressCodePais", nextLineOut[10]);
		InvoiceHeader.put("CustomerRuc", nextLineOut[11]);
		InvoiceHeader.put("CustomerTipoDocumento", nextLineOut[12]);
		InvoiceHeader.put("InvoiceTipoDocumento", nextLineOut[13]);
		InvoiceHeader.put("InvoiceNumeracion", nextLineOut[14]);
		InvoiceHeader.put("AdquirenteNumero", nextLineOut[15]);
		InvoiceHeader.put("AdquirenteTipoDocumento", nextLineOut[16]);
		InvoiceHeader.put("AdquirenteRazonSocial", nextLineOut[17]);
		InvoiceHeader.put("AdquirenteDireccion", nextLineOut[18]);
		InvoiceHeader.put("InvoiceMoneda", nextLineOut[19]);
		InvoiceHeader.put("OperacionGrabadaCode", nextLineOut[20]);
		InvoiceHeader.put("OperacionGrabadaMonto", nextLineOut[21]); 
		InvoiceHeader.put("OperacionInafectaCode", nextLineOut[22]);
		InvoiceHeader.put("OperacionInafectaMonto", nextLineOut[23]);
		InvoiceHeader.put("OperacionExoneradaCode", nextLineOut[24]);
		InvoiceHeader.put("OperacionExoneradaMonto", nextLineOut[25]);
		InvoiceHeader.put("IGVSumaTotal", nextLineOut[26]);
		InvoiceHeader.put("IGVSumaSubTotal", nextLineOut[27]);
		InvoiceHeader.put("IGVCodeTributo", nextLineOut[28]);
		InvoiceHeader.put("IGVNombreTributo", nextLineOut[29]);
		InvoiceHeader.put("IGVCodeInterTributo", nextLineOut[30]);
		
		InvoiceHeader.put("ISCSumaTotal", nextLineOut[31]);
		InvoiceHeader.put("ISCSumaSubTotal", nextLineOut[32]);
		InvoiceHeader.put("ISCCodeTributo", nextLineOut[33]);
		InvoiceHeader.put("ISCNombreTributo", nextLineOut[34]);
		InvoiceHeader.put("ISCCodeInterTributo", nextLineOut[35]);
		InvoiceHeader.put("OtrosSumaTotal", nextLineOut[36]);
		InvoiceHeader.put("OtrosSumaSubTotal", nextLineOut[37]);
		InvoiceHeader.put("OtrosCodeTributo", nextLineOut[38]);
		InvoiceHeader.put("OtrosNombreTributo", nextLineOut[39]);
		InvoiceHeader.put("OtrosCodeInterTributo", nextLineOut[40]);
		
		InvoiceHeader.put("OtrosCargosSumaTotal", nextLineOut[41]);
		InvoiceHeader.put("DescuentosCode", nextLineOut[42]);
		InvoiceHeader.put("DescuentosSumaTotal", nextLineOut[43]);
		InvoiceHeader.put("ImporteTotalVenta", nextLineOut[44]);
		InvoiceHeader.put("GuiaRemisionNumero", nextLineOut[45]);
		InvoiceHeader.put("GuiaRemisionTipo", nextLineOut[46]);
		InvoiceHeader.put("DocRelacionadoNumero", nextLineOut[47]);
		InvoiceHeader.put("DocRelacionadoTipo", nextLineOut[48]);
		InvoiceHeader.put("LeyendaCode", nextLineOut[49]);
		InvoiceHeader.put("LeyendaDescr", nextLineOut[50]);
		
		InvoiceHeader.put("PercepcionCode", nextLineOut[51]);
		InvoiceHeader.put("PercepcionMonto", nextLineOut[52]);
		InvoiceHeader.put("PercepcionTotalMonto", nextLineOut[53]);
		InvoiceHeader.put("UBLversion", nextLineOut[54]);
		InvoiceHeader.put("UBLversionEstruc", nextLineOut[55]);
		InvoiceHeader.put("PercepcionMNCode", nextLineOut[56]);
		InvoiceHeader.put("PercepcionMNBase", nextLineOut[57]);
		InvoiceHeader.put("PercepcionMNMonto", nextLineOut[58]);
		InvoiceHeader.put("PercepcionMNTotalMonto", nextLineOut[59]);
		InvoiceHeader.put("OperacionGratuitaCode", nextLineOut[60]);
		
		InvoiceHeader.put("OperacionGratuitaValor", nextLineOut[61]);
		InvoiceHeader.put("DescuentoGlobalValor", nextLineOut[62]);
		InvoiceHeader.put("DescuentoItemCode", nextLineOut[63]);
		InvoiceHeader.put("DescuentoItemValor", nextLineOut[64]);

	}

	@Override
	public HashMap<String, Object> getData() {
		// TODO Auto-generated method stub
		return invoiceStruct;
	}

}
