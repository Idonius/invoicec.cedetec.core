package com.cimait.invoicec.interfaces;

import java.util.HashMap;

public abstract class  GenericReaderDocument {
	public void readCSV(String file) {
	}
	public HashMap<String,Object> getData() {
		return null;
	}	
	public String getDocumentType() {
		return null;
	}
}
