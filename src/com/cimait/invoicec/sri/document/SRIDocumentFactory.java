package com.cimait.invoicec.sri.document;

public class SRIDocumentFactory {

	public static SRIDocument getSRIDocument(String type) throws Exception  {
		SRIDocument gDoc = null;
		if  (type.equals("01")) {
			gDoc = new SRIInvoice();
		} else if (type.equals("04")) {
			gDoc = new SRICreditNote();
		} else if (type.equals("05")) {
			gDoc = new SRIDebitNote();
		} else if (type.equals("07")) {
			gDoc = new SRIRetention();
		} else {
			throw new Exception("Error tipo de document invalido");
		}
		return gDoc;
	}
}
