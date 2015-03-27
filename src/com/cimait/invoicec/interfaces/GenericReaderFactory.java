package com.cimait.invoicec.interfaces;


public class GenericReaderFactory {

public static GenericReaderDocument getReaderDocument(String typeReader) throws Exception  {
	GenericReaderDocument inDoc = null;
	if (typeReader.equals("01")) { 
		inDoc = new ReaderInvoice();
	} else if (typeReader.equals("03")) {
		inDoc = new ReaderReceipt();
	} else if (typeReader.equals("07")) {
		inDoc = new ReaderCreditNote();
	} else if (typeReader.equals("08")) {
		inDoc = new ReaderDebitNote();	
	} else if (typeReader.equals("RA")) {
		inDoc  = new ReaderVoided();
	} else {
		throw new Exception("Error tipo de documento de entrada invalido");
	}
	return inDoc;
}
}
