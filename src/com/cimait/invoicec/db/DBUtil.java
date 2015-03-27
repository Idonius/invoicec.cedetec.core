package com.cimait.invoicec.db;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.naming.NamingException;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.cimait.invoicec.entity.FacCabDocumentosEntity;


public class DBUtil {
		public static String statusDocumento(int ambiente,  String ruc, String documentType  ,String documentNumber)
				throws SQLException, IOException, NamingException,
				ClassNotFoundException {
			String status = "";
			
			Session session = DBDataSource.getInstance().getFactory().openSession();
	        Transaction tx = null ;
	        try {
	            tx = session.beginTransaction(); 
	            FacCabDocumentosEntity documentSearch = new FacCabDocumentosEntity();
	            documentSearch.setRuc(ruc);
	            documentSearch.setCodigoDocumento(documentType);
	            documentSearch.setCodEstablecimiento(documentNumber.substring(0,3));
	            documentSearch.setCodPuntEmision(documentNumber.substring(3,6));
	            documentSearch.setSecuencial(documentNumber.substring(6));
	           
	            FacCabDocumentosEntity documento = (FacCabDocumentosEntity) session.get(FacCabDocumentosEntity.class, documentSearch);
	            if (documento != null) {
	            	status = documento.getEstadoTransaccion();
	            }
	            tx.commit();
			}catch (HibernateException e){ 
				if (tx!=null) tx.rollback();
	            e.printStackTrace();
			}catch (Exception e) {
				e.printStackTrace();
			} finally {
				session.close();
			}
			return (status==null?"":status);
		}
}
