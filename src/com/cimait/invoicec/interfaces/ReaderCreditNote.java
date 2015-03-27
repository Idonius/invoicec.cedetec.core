package com.cimait.invoicec.interfaces;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;

import au.com.bytecode.opencsv.CSVReader;

public class ReaderCreditNote extends GenericReaderDocument{

	private HashMap<String,Object> invoiceData;
	private HashMap<String,Object> invoiceStruct;
	private HashMap<String,Object> InvoiceHeader;
	private HashMap<String,Object> InvoiceDetail;
	ArrayList<HashMap<String,Object>> InvoiceDetailFieldList;

	public ReaderCreditNote(){
		
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
		    	//System.out.println("Linea:"+contador+" CodigoLinea:"+nextLine[0]);
		    	
		    	if(nextLine[0].equals("CC")){
		    		cabeceraInvoice(nextLine);
		    	}else if(nextLine[0].equals("DC")){
		    		detalleInvoice(nextLine);
		    		InvoiceDetailFieldList.add(InvoiceDetail);
		    	}
		    }
		
		    invoiceData.put("CC",InvoiceHeader);
		    invoiceData.put("DC",InvoiceDetailFieldList);
		     
		    invoiceStruct.put("07",invoiceData);
		    reader.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	private void detalleInvoice(String[] nextLineOut) {
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
		//QAD
		InvoiceDetail.put("QADUnidadMedida", nextLineOut[24].trim());
		InvoiceDetail.put("QADObs", nextLineOut[25].trim());
		
	}
	private void cabeceraInvoice(String[] nextLineOut) {
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
    	
    	InvoiceHeader.put("InvoiceDocRel", nextLineOut[13].trim());
    	InvoiceHeader.put("InvoiceDocRelTipDoc", nextLineOut[14].trim());
    	
    	
    	InvoiceHeader.put("InvoiceNumeracion", nextLineOut[15].trim());
    	
    	InvoiceHeader.put("AdquirenteNumero", nextLineOut[16].trim());
    	InvoiceHeader.put("AdquirenteTipoDocumento", nextLineOut[17].trim());
    	InvoiceHeader.put("AdquirenteRazonSocial", nextLineOut[18].trim());
    	
    	InvoiceHeader.put("invoiceMotivo", nextLineOut[19].trim());
    	
    	InvoiceHeader.put("OperacionGrabadaCode", nextLineOut[20].trim());
    	InvoiceHeader.put("OperacionGrabadaMonto", nextLineOut[21].trim()); 
    	
    	InvoiceHeader.put("OperacionInafectaCode", nextLineOut[22].trim());
    	InvoiceHeader.put("OperacionInafectaMonto", nextLineOut[23].trim());
    	
    	InvoiceHeader.put("OperacionExoneradaCode", nextLineOut[24].trim());
    	InvoiceHeader.put("OperacionExoneradaMonto", nextLineOut[25].trim());
    	
    	
    	InvoiceHeader.put("IGVSumaTotal", nextLineOut[26].trim());
    	InvoiceHeader.put("IGVSumaSubTotal", nextLineOut[27].trim());
    	InvoiceHeader.put("IGVCodeTributo", nextLineOut[28].trim());
    	InvoiceHeader.put("IGVNombreTributo", nextLineOut[29].trim());
    	InvoiceHeader.put("IGVCodeInterTributo", nextLineOut[30].trim());
    	
    	InvoiceHeader.put("ISCSumaTotal", nextLineOut[31].trim());
    	InvoiceHeader.put("ISCSumaSubTotal", nextLineOut[32].trim());
    	InvoiceHeader.put("ISCCodeTributo", nextLineOut[33].trim());
    	InvoiceHeader.put("ISCNombreTributo", nextLineOut[34].trim());
    	InvoiceHeader.put("ISCCodeInterTributo", nextLineOut[35].trim());
    	
    	InvoiceHeader.put("OtrosSumaTotal", nextLineOut[36].trim());
    	InvoiceHeader.put("OtrosSumaSubTotal", nextLineOut[37].trim());
    	InvoiceHeader.put("OtrosCodeTributo", nextLineOut[38].trim());
    	InvoiceHeader.put("OtrosNombreTributo", nextLineOut[39].trim());
    	InvoiceHeader.put("OtrosCodeInterTributo", nextLineOut[40].trim());
    	
    	InvoiceHeader.put("OtrosCargosSumaTotal", nextLineOut[41].trim());
    	
    	InvoiceHeader.put("DescuentosCode", nextLineOut[42].trim());
    	InvoiceHeader.put("DescuentosSumaTotal", nextLineOut[43].trim());
    	
    	InvoiceHeader.put("ImporteTotalVenta", nextLineOut[44].trim());
    	
    	InvoiceHeader.put("InvoiceMoneda", nextLineOut[45].trim());
    	
    	InvoiceHeader.put("DocModificaNum", nextLineOut[46].trim());
    	InvoiceHeader.put("DocModificaCode", nextLineOut[47].trim());
    	
    	InvoiceHeader.put("DocReferenciaNum", nextLineOut[48].trim());
    	InvoiceHeader.put("DocReferenciaCode", nextLineOut[49].trim());
    	
    	InvoiceHeader.put("DocReferenciaAdicional", nextLineOut[50].trim());
    	InvoiceHeader.put("DocReferenciaAdicionalCode", nextLineOut[51].trim());
    	
    	
    	InvoiceHeader.put("UBLversion", nextLineOut[52].trim());
    	InvoiceHeader.put("UBLversionEstruc", nextLineOut[53].trim());
    	//QAD
    	InvoiceHeader.put("QADClienteCod", nextLineOut[54].trim());
    	InvoiceHeader.put("QADClienteDir", nextLineOut[55].trim());
    	InvoiceHeader.put("QADClienteTel", nextLineOut[56].trim());
    	InvoiceHeader.put("QADClienteEmail", nextLineOut[57].trim());
    	InvoiceHeader.put("QADNroFactura", nextLineOut[58].trim());
    	InvoiceHeader.put("QADFechaEmisionRel", nextLineOut[59].trim());
    	InvoiceHeader.put("QADLeyenda", nextLineOut[60].trim());
    	InvoiceHeader.put("QADEmailCreador", nextLineOut[61].trim());
	}
	
	@Override
	public HashMap<String, Object> getData() {
		// TODO Auto-generated method stub
		return invoiceStruct;
	}

}
