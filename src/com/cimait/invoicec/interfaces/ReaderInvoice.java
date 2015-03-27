package com.cimait.invoicec.interfaces;

import java.util.HashMap;
import java.util.ArrayList;
import java.io.*;
import au.com.bytecode.opencsv.*;

public class ReaderInvoice extends GenericReaderDocument {
   
	private HashMap<String,Object> invoiceData;
	private HashMap<String,Object> invoiceStruct;
	private HashMap<String,Object> InvoiceHeader;
	private HashMap<String,Object> InvoiceDetail;
	ArrayList<HashMap<String,Object>> InvoiceDetailFieldList;
	
	public ReaderInvoice(){
		
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
		    
		    try{
		    while ((nextLine = reader.readNext()) != null) {
		    	contador ++;
		    	//System.out.println("Linea:"+contador+" CodigoLinea:"+nextLine[0]);
		    	
		    	if(nextLine[0].equals("CF")){
		    		cabeceraInvoice(nextLine);
		    	}else if(nextLine[0].equals("DF")){
		    		detalleInvoice(nextLine);
		    		InvoiceDetailFieldList.add(InvoiceDetail);
		    	}
		    }
		
		    invoiceData.put("CF",InvoiceHeader);
		    invoiceData.put("DF",InvoiceDetailFieldList);
		     
		    invoiceStruct.put("01",invoiceData);
		    }catch (Exception e){
		    	reader.close();
		    	e.printStackTrace();
		    }finally{
		    	reader.close();		    	
		    }
		    
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	@Override
	public HashMap<String,Object> getData() {
		// TODO Auto-generated method stub
		return invoiceStruct;
		//return InvoiceDetail;
	}
	
	private void detalleInvoice(String [] nextLineOut){
		InvoiceDetail = new HashMap<String, Object>();
		InvoiceDetail.put("ItemOrden",nextLineOut[1].trim());
		InvoiceDetail.put("ItemProdCode",nextLineOut[2].trim());

		InvoiceDetail.put("ItemUnidadMedida",nextLineOut[3].trim());
		InvoiceDetail.put("itemCantidad",nextLineOut[4].trim());
		InvoiceDetail.put("itemDetalleServicio",nextLineOut[5].trim());
		InvoiceDetail.put("itemValorUnitario",nextLineOut[6].trim());
		InvoiceDetail.put("itemPrecioUnitVenta",nextLineOut[7].trim());
		InvoiceDetail.put("itemPrecioUnitCode",nextLineOut[8].trim());
		
		InvoiceDetail.put("itemIGVTotal",nextLineOut[9].trim());
		InvoiceDetail.put("itemIGVSubTotal",nextLineOut[10].trim());
		InvoiceDetail.put("itemIGVAfectacionCode",nextLineOut[11].trim());
		InvoiceDetail.put("itemIGVTributoCode",nextLineOut[12].trim());
		InvoiceDetail.put("itemIGVTributoName",nextLineOut[13].trim());
		InvoiceDetail.put("itemIGVTributoInterCode",nextLineOut[14].trim());
		
		InvoiceDetail.put("itemISCTotal",nextLineOut[15].trim());
		InvoiceDetail.put("itemISCSubTotal",nextLineOut[16].trim());
		InvoiceDetail.put("itemISCSistemaCode",nextLineOut[17].trim());
		InvoiceDetail.put("itemISCTributoCode",nextLineOut[18].trim());
		InvoiceDetail.put("itemISCTributoName",nextLineOut[19].trim());
		InvoiceDetail.put("itemISCTributoInterCode",nextLineOut[20].trim());
		
		InvoiceDetail.put("itemValorVenta",nextLineOut[21].trim());
		InvoiceDetail.put("itemReferenciasValor",nextLineOut[22].trim());
		InvoiceDetail.put("itemReferencialCode",nextLineOut[23].trim());
		
		InvoiceDetail.put("DescuentoItemCode", nextLineOut[24].trim());
		InvoiceDetail.put("DescuentoItemValor", nextLineOut[25].trim());
		//QAD
		InvoiceDetail.put("QADUnidadMedida", nextLineOut[26].trim());
		InvoiceDetail.put("QADObs", nextLineOut[27].trim());
	}
	
	
	private void cabeceraInvoice(String[] nextLineOut){
		InvoiceHeader = new HashMap<String, Object>();
		
		InvoiceHeader.put("IssueDate", nextLineOut[1].trim());
		InvoiceHeader.put("CustomerRazonSocial", nextLineOut[2].trim());
    	InvoiceHeader.put("CustomerNombreComercial", nextLineOut[3].trim());
    	
    	InvoiceHeader.put("AddressUbigeo", nextLineOut[4].trim());
    	InvoiceHeader.put("AddressDireccion", nextLineOut[5].trim());
    	InvoiceHeader.put("AddressUrbanizacion", nextLineOut[6].trim());
    	InvoiceHeader.put("AddressProvincia", nextLineOut[7].trim());
    	InvoiceHeader.put("AddressDepartamento", nextLineOut[8].trim());
    	InvoiceHeader.put("AddressDistrito", nextLineOut[9].trim());
    	InvoiceHeader.put("AddressCodePais", nextLineOut[10].trim());
    	
    	InvoiceHeader.put("CustomerRuc", nextLineOut[11].trim());
    	InvoiceHeader.put("CustomerTipoDocumento", nextLineOut[12].trim());
    	
    	InvoiceHeader.put("InvoiceTipoDocumento", nextLineOut[13].trim());
    	InvoiceHeader.put("InvoiceNumeracion", nextLineOut[14].trim());
    	
    	InvoiceHeader.put("AdquirenteNumero", nextLineOut[15].trim());
    	InvoiceHeader.put("AdquirenteTipoDocumento", nextLineOut[16].trim());
    	InvoiceHeader.put("AdquirenteRazonSocial", nextLineOut[17].trim());
    	
    	InvoiceHeader.put("InvoiceMoneda", nextLineOut[18].trim());
    	
    	InvoiceHeader.put("OperacionGrabadaCode", nextLineOut[19].trim());
    	InvoiceHeader.put("OperacionGrabadaMonto", nextLineOut[20].trim()); 
    	
    	InvoiceHeader.put("OperacionInafectaCode", nextLineOut[21].trim());
    	InvoiceHeader.put("OperacionInafectaMonto", nextLineOut[22].trim());
    	
    	InvoiceHeader.put("OperacionExoneradaCode", nextLineOut[23].trim());
    	InvoiceHeader.put("OperacionExoneradaMonto", nextLineOut[24].trim());
    	
    	InvoiceHeader.put("IGVSumaTotal", nextLineOut[25].trim());
    	InvoiceHeader.put("IGVSumaSubTotal", nextLineOut[26].trim());
    	InvoiceHeader.put("IGVCodeTributo", nextLineOut[27].trim());
    	InvoiceHeader.put("IGVNombreTributo", nextLineOut[28].trim());
    	InvoiceHeader.put("IGVCodeInterTributo", nextLineOut[29].trim());
    	
    	InvoiceHeader.put("ISCSumaTotal", nextLineOut[30].trim());
    	InvoiceHeader.put("ISCSumaSubTotal", nextLineOut[31].trim());
    	InvoiceHeader.put("ISCCodeTributo", nextLineOut[32].trim());
    	InvoiceHeader.put("ISCNombreTributo", nextLineOut[33].trim());
    	InvoiceHeader.put("ISCCodeInterTributo", nextLineOut[34].trim());
    	
    	InvoiceHeader.put("OtrosSumaTotal", nextLineOut[35].trim());
    	InvoiceHeader.put("OtrosSumaSubTotal", nextLineOut[36].trim());
    	InvoiceHeader.put("OtrosCodeTributo", nextLineOut[37].trim());
    	InvoiceHeader.put("OtrosNombreTributo", nextLineOut[38].trim());
    	InvoiceHeader.put("OtrosCodeInterTributo", nextLineOut[39].trim());
    	
    	InvoiceHeader.put("OtrosCargosSumaTotal", nextLineOut[40].trim());
    	
    	InvoiceHeader.put("DescuentosCode", nextLineOut[41].trim());
    	InvoiceHeader.put("DescuentosSumaTotal", nextLineOut[42].trim());
    	
    	InvoiceHeader.put("ImporteTotalVenta", nextLineOut[43].trim());
    	
    	InvoiceHeader.put("GuiaRemisionNumero", nextLineOut[44].trim());
    	InvoiceHeader.put("GuiaRemisionTipo", nextLineOut[45].trim());
    	
    	InvoiceHeader.put("DocRelacionadoNumero", nextLineOut[46].trim());
    	InvoiceHeader.put("DocRelacionadoTipo", nextLineOut[47].trim());
    	
    	InvoiceHeader.put("LeyendaCode", nextLineOut[48].trim());
    	InvoiceHeader.put("LeyendaDescr", nextLineOut[49].trim());
    	
    	InvoiceHeader.put("PercepcionCode", nextLineOut[50].trim());
    	InvoiceHeader.put("PercepcionBase", nextLineOut[51].trim());
    	InvoiceHeader.put("PercepcionMonto", nextLineOut[52].trim());
    	InvoiceHeader.put("PercepcionTotalMonto", nextLineOut[53].trim());
    	
    	InvoiceHeader.put("UBLversion", nextLineOut[54].trim());
    	InvoiceHeader.put("UBLversionEstruc", nextLineOut[55].trim());
    	
    	InvoiceHeader.put("ReferencialServCode", nextLineOut[56].trim());
    	InvoiceHeader.put("ReferencialServValor", nextLineOut[57].trim());
    	
    	InvoiceHeader.put("PesqueraCode", nextLineOut[58].trim());
    	InvoiceHeader.put("PesqueraValor", nextLineOut[59].trim());
    	
    	InvoiceHeader.put("EspecieCode", nextLineOut[60].trim());
    	InvoiceHeader.put("EspecieValor", nextLineOut[61].trim());
    	
    	InvoiceHeader.put("LugarDescargaCode", nextLineOut[62].trim());
    	InvoiceHeader.put("LugarDescargaValor", nextLineOut[63].trim());
    	
    	InvoiceHeader.put("FechaDescargaCode", nextLineOut[64].trim());
    	InvoiceHeader.put("FechaDescargaValor", nextLineOut[65].trim());
    	
    	InvoiceHeader.put("MTCcode", nextLineOut[66].trim());
    	InvoiceHeader.put("MTCvalor", nextLineOut[67].trim());
    	
    	InvoiceHeader.put("VehicularCode", nextLineOut[68].trim());
    	InvoiceHeader.put("VehicularValor", nextLineOut[69].trim());
    	
    	InvoiceHeader.put("OrigenCode", nextLineOut[70].trim());
    	InvoiceHeader.put("OrigenValor", nextLineOut[71].trim());
    	
    	InvoiceHeader.put("DestinoCode", nextLineOut[72].trim());
    	InvoiceHeader.put("DestinoValor", nextLineOut[73].trim());
    	
    	InvoiceHeader.put("PreliminarCode", nextLineOut[74].trim());
    	InvoiceHeader.put("PreliminarDescr", nextLineOut[75].trim());
    	InvoiceHeader.put("PreliminarValor", nextLineOut[76].trim());
    	
    	InvoiceHeader.put("fechaConsumoCode", nextLineOut[77].trim());
    	InvoiceHeader.put("fechaConsumoValor", nextLineOut[78].trim());
    	
    	InvoiceHeader.put("OperacionGratuitaCode", nextLineOut[79].trim());
    	InvoiceHeader.put("OperacionGratuitaValor", nextLineOut[80].trim());
    	
    	InvoiceHeader.put("DescuentoGlobalValor", nextLineOut[81].trim());

    	//additional data from QAD -> Print/Invoicec
    	InvoiceHeader.put("QADClienteCod", nextLineOut[82].trim());
    	InvoiceHeader.put("QADClienteDir", nextLineOut[83].trim());
    	InvoiceHeader.put("QADClienteTel", nextLineOut[84].trim());
    	InvoiceHeader.put("QADClienteEmail", nextLineOut[85].trim());
    	InvoiceHeader.put("QADNroFactura", nextLineOut[86].trim());
    	InvoiceHeader.put("QADTerms", nextLineOut[87].trim());
    	InvoiceHeader.put("QADEmailCreador", nextLineOut[88].trim());
    	
	}

}
