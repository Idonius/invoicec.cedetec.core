package com.tradise.reportes.reportes;
 
import com.sun.DAO.DetalleTotalImpuestos;
import com.sun.reportes.detalles.DetallesAdicionales;
import com.sun.reportes.detalles.InfoAdicional;
import com.tradise.reportes.entidades.FacCabDocumento;
import com.tradise.reportes.entidades.FacDetAdicional;
import com.tradise.reportes.entidades.FacDetDocumento;
import com.tradise.reportes.entidades.FacDetMotivosdebito;
import com.tradise.reportes.entidades.FacDetRetencione;
import com.tradise.reportes.entidades.FacEmpresa;
import com.tradise.reportes.entidades.FacGeneral;
import com.tradise.reportes.entidades.FacProducto;
import com.tradise.reportes.servicios.ReporteServicio;
import com.util.util.key.Environment;
import com.util.util.key.GenericTransaction;

import ec.gob.sri.comprobantes.administracion.modelo.Emisor;
import ec.gob.sri.comprobantes.modelo.reportes.DetalleGuiaReporte;
import ec.gob.sri.comprobantes.modelo.reportes.DetallesAdicionalesReporte;
import ec.gob.sri.comprobantes.modelo.reportes.GuiaRemisionReporte;
import ec.gob.sri.comprobantes.modelo.reportes.InformacionAdicional;
import ec.gob.sri.comprobantes.sql.EmisorSQL;
import ec.gob.sri.comprobantes.util.reportes.JasperViwerSRI;
import java.awt.Dimension;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.view.JRSaveContributor;
import net.sf.jasperreports.view.save.JRPdfSaveContributor;
 
 public class ReporteUtil extends GenericTransaction
 {
   private String Ruc;
   private String codEst;
   private String codPuntEm;
   private String codDoc;
   private String secuencial;
   private ReporteServicio servicio = new ReporteServicio();
   private static String classReference;
 
   private static Emisor obtenerEmisor()
     throws SQLException, ClassNotFoundException
   {
     EmisorSQL emisSQL = new EmisorSQL();
     return emisSQL.obtenerDatosEmisor();
   }
 
   public String generarReporteFac(String urlReporte, String numfact)
     throws SQLException, ClassNotFoundException
   {
     FileInputStream is = null;
     JRDataSource dataSource = null;
     List detallesAdiciones = new ArrayList();
     List infoAdicional = new ArrayList();
     List detDocumento = new ArrayList();
     List detAdicional = new ArrayList();
     try
     {
       detDocumento = this.servicio.buscarDatosDetallesDocumentos(this.Ruc, this.codEst, this.codPuntEm, this.codDoc, this.secuencial);
       detAdicional = this.servicio.buscarDetAdicional(this.Ruc, this.codEst, this.codPuntEm, this.codDoc, this.secuencial);
       if (!detAdicional.isEmpty()) {
         for (int i = 0; i < detAdicional.size(); i++) {
           InformacionAdicional infoAd = new InformacionAdicional();
           infoAd.setNombre(((FacDetAdicional)detAdicional.get(i)).getNombre());
           infoAd.setValor(((FacDetAdicional)detAdicional.get(i)).getValor());
           infoAdicional.add(i, infoAd);
         }
       }
       if (!detDocumento.isEmpty())
         for (int i = 0; i < detDocumento.size(); i++) {
           FacProducto producto = new FacProducto();
           DetallesAdicionalesReporte detAd = new DetallesAdicionalesReporte();
           detAd.setCodigoPrincipal(((FacDetDocumento)detDocumento.get(i)).getCodPrincipal());
           producto = this.servicio.buscarProductos(Integer.valueOf(((FacDetDocumento)detDocumento.get(i)).getCodPrincipal().trim()).intValue());
           detAd.setDetalle1(producto.getAtributo1());
           detAd.setDetalle2(producto.getAtributo2());
           detAd.setDetalle3(producto.getAtributo3());
           detAd.setDescuento(String.valueOf(((FacDetDocumento)detDocumento.get(i)).getDescuento()));
           detAd.setCodigoAuxiliar(((FacDetDocumento)detDocumento.get(i)).getCodAuxiliar());
           detAd.setDescripcion(((FacDetDocumento)detDocumento.get(i)).getDescripcion());
           detAd.setCantidad(((FacDetDocumento)detDocumento.get(i)).getCantidad().toString());
           detAd.setPrecioTotalSinImpuesto(String.valueOf(((FacDetDocumento)detDocumento.get(i)).getPrecioTotalSinImpuesto()));
           detAd.setPrecioUnitario(String.valueOf(((FacDetDocumento)detDocumento.get(i)).getPrecioUnitario()));
           detAd.setInfoAdicional(infoAdicional.isEmpty() ? null : infoAdicional);
           detallesAdiciones.add(i, detAd);
         }
     }
     catch (Exception e) {
       e.printStackTrace();
     }
     try {
       System.out.println("El objeto datasource contiene la info adicional...");
       dataSource = new JRBeanCollectionDataSource(detallesAdiciones);
	   System.out.println("urlReporte::"+urlReporte);
       is = new FileInputStream(urlReporte);
       //GBM: PASANDO PARÁMETROS (DATOS) AL REPORTE. EN EL OBJETO "dataSource" ESTÁ PASANDO LA INFORMACIÓN ADICIONAL DE LA FACTURA
       JasperPrint reporte_view = JasperFillManager.fillReport(is, obtenerMapaParametrosReportes(obtenerParametrosInfoTriobutaria(), obtenerInfoFactura()), dataSource);
	   System.out.println("numfact::"+numfact);				
	   System.out.println("reporte_view::"+reporte_view);
       JasperExportManager.exportReportToPdfFile(reporte_view, numfact);
     }
     catch (FileNotFoundException ex) {
       Logger.getLogger(ReporteUtil.class.getName()).log(Level.SEVERE, null, ex);
       try
       {
         if (is != null)
           is.close();
       }
       catch (IOException ex1) {
         Logger.getLogger(ReporteUtil.class.getName()).log(Level.SEVERE, null, ex1);
       }
     }
     catch (JRException e)
     {
       Logger.getLogger(ReporteUtil.class.getName()).log(Level.SEVERE, null, e);
       try
       {
         if (is != null)
           is.close();
       }
       catch (IOException ex) {
         Logger.getLogger(ReporteUtil.class.getName()).log(Level.SEVERE, null, ex);
       }
     }
     finally
     {
       try
       {
         if (is != null)
           is.close();
       }
       catch (IOException ex) {
         Logger.getLogger(ReporteUtil.class.getName()).log(Level.SEVERE, null, ex);
       }
     }
				return numfact;
   }

   public static void main(String[] arg) throws SQLException, ClassNotFoundException {
     ReporteUtil rep = new ReporteUtil();
 
     rep.setRuc("0992531940001");
     rep.setCodEst("001");
/* 138 */     rep.setCodPuntEm("011");
/* 139 */     rep.setCodDoc("04");
/* 140 */     rep.setSecuencial("000007001");
 
/* 150 */     String name = rep.getCodDoc() + rep.getCodEst() + rep.getCodPuntEm() + rep.getRuc() + rep.getSecuencial();
/* 151 */     if (rep.getCodDoc().equals("01")) 
	          rep.generarReporteFac("C://Users//Administrador//Desktop//Reportes//reportes//factura.jasper", "C://resources//reportes//" + name + ".pdf");
/* 152 */     if (rep.getCodDoc().equals("04")) rep.generarReporteNotaCredito("C://resources//reportes//notaCreditoFinal.jasper", "C://resources//reportes//" + name + ".pdf");
/* 153 */     if (rep.getCodDoc().equals("05")) rep.generarReporteNotaDebito("C://resources//reportes//notaDebitoFinal.jasper", "C://resources//reportes//" + name + ".pdf");
/* 154 */     if (rep.getCodDoc().equals("06")) rep.generarReporteGuia("C://resources//reportes//guiaRemisionFinal.jasper", "C://resources//reportes//" + name + ".pdf");
/* 155 */     //if (rep.getCodDoc().equals("07")) rep.generarReporteRetencion("C://resources//reportes//comprobanteRetencion.jasper", "C://resources//reportes//" + name + ".pdf");
   }
 
   public static String generaPdfDocumentos(String ruc, String codEst, String codPtoEmi, String tipoDocumento, String secuencial, String rutaJasper, String rutaReporte, String nameReporte)
     throws Exception
   {
				String reporte = "";
/* 172 */     ReporteUtil rep = new ReporteUtil();
 
/* 174 */     rep.setRuc(ruc);
/* 175 */     rep.setCodEst(codEst);
/* 176 */     rep.setCodPuntEm(codPtoEmi);
/* 177 */     rep.setCodDoc(tipoDocumento);
/* 178 */     rep.setSecuencial(secuencial);
/* 179 */     classReference = "ReporteUtil";
/* 180 */     String name_xml = "facturacion.xml";
 
/* 189 */     String jasperFile = "";
     try {
/* 191 */       jasperFile = Environment.c.getString("facElectronica.pdf.jasper.doc" + ruc + "_" + rep.getCodDoc()) == null ? "" : Environment.c.getString("facElectronica.pdf.jasper.doc" + ruc + "_" + rep.getCodDoc());
     }
     catch (Exception e) {
/* 194 */       jasperFile = Environment.c.getString("facElectronica.pdf.jasper.doc" + ruc + "_" + rep.getCodDoc()) == null ? "" : Environment.c.getString("facElectronica.pdf.jasper.doc" + ruc + "_" + rep.getCodDoc());
     }
/* 196 */     if ((jasperFile.equals("")) || (jasperFile == null)) {
/* 197 */       jasperFile = Environment.c.getString("facElectronica.pdf.jasper.doc" + ruc + "_" + rep.getCodDoc()) == null ? "" : Environment.c.getString("facElectronica.pdf.jasper.doc" + ruc + "_" + rep.getCodDoc());
     }
/* 199 */     if ((jasperFile.equals("")) || (jasperFile == null)) {
/* 200 */       if (rep.getCodDoc().equals("01"))
/* 201 */         jasperFile = "factura.jasper";
/* 202 */       if (rep.getCodDoc().equals("04"))
/* 203 */         jasperFile = "notaCreditoFinal.jasper";
/* 204 */       if (rep.getCodDoc().equals("05"))
/* 205 */         jasperFile = "notaDebitoFinal.jasper";
/* 206 */       if (rep.getCodDoc().equals("06"))
/* 207 */         jasperFile = "guiaRemisionFinal.jasper";
/* 208 */       if (rep.getCodDoc().equals("07"))
/* 209 */         jasperFile = "comprobanteRetencion.jasper";
     }
     try
     {
/* 213 */       if (rep.getCodDoc().equals("01"))
/* 214 */         reporte = rep.generarReporteFac(rutaJasper + jasperFile, rutaReporte + nameReporte);
/* 215 */       if (rep.getCodDoc().equals("04"))
/* 216 */         rep.generarReporteNotaCredito(rutaJasper + jasperFile, rutaReporte + nameReporte);
/* 217 */       if (rep.getCodDoc().equals("05"))
/* 218 */         rep.generarReporteNotaDebito(rutaJasper + jasperFile, rutaReporte + nameReporte);
/* 219 */       if (rep.getCodDoc().equals("06"))
/* 220 */         rep.generarReporteGuia(rutaJasper + jasperFile, rutaReporte + nameReporte);
/* 221 */       //if (rep.getCodDoc().equals("07"))
/* 222 */        // rep.generarReporteRetencion(rutaJasper + jasperFile, rutaReporte + nameReporte);
     }
     catch (SQLException e)
     {				
/* 226 */       e.printStackTrace();
				return "";
     }
     catch (ClassNotFoundException e) {
/* 229 */       e.printStackTrace();
				return reporte;
     }
 
/* 232 */     return reporte;
   }
   public static String generaPdfDocumentos(com.cimait.invoicec.bean.Emisor emite, String ruc, String codEst, String codPtoEmi, String tipoDocumento, String secuencial, String rutaJasper, String rutaReporte, String nameReporte)
     throws Exception
   {
/* 172 */     ReporteUtil rep = new ReporteUtil();
 
/* 174 */     rep.setRuc(ruc);
/* 175 */     rep.setCodEst(codEst);
/* 176 */     rep.setCodPuntEm(codPtoEmi);
/* 177 */     rep.setCodDoc(tipoDocumento);
/* 178 */     rep.setSecuencial(secuencial);
/* 179 */     classReference = "ReporteUtil";
/* 180 */     String name_xml = "facturacion.xml";
     String reportePdf="";
/* 189 */     String jasperFile = "";
     try {
/* 191 */       jasperFile = Environment.c.getString("facElectronica.pdf.jasper.doc" + ruc + "_" + rep.getCodDoc()) == null ? "" : Environment.c.getString("facElectronica.pdf.jasper.doc" + ruc + "_" + rep.getCodDoc());
     }
     catch (Exception e) {
/* 194 */       jasperFile = Environment.c.getString("facElectronica.pdf.jasper.doc" + ruc + "_" + rep.getCodDoc()) == null ? "" : Environment.c.getString("facElectronica.pdf.jasper.doc" + ruc + "_" + rep.getCodDoc());
     }
/* 196 */     if ((jasperFile.equals("")) || (jasperFile == null)) {
/* 197 */       jasperFile = Environment.c.getString("facElectronica.pdf.jasper.doc" + ruc + "_" + rep.getCodDoc()) == null ? "" : Environment.c.getString("facElectronica.pdf.jasper.doc" + ruc + "_" + rep.getCodDoc());
     }
/* 199 */     if ((jasperFile.equals("")) || (jasperFile == null)) {
/* 200 */       if (rep.getCodDoc().equals("01"))
/* 201 */         jasperFile = "factura.jasper";
/* 202 */       if (rep.getCodDoc().equals("04"))
/* 203 */         jasperFile = "notaCreditoFinal.jasper";
/* 204 */       if (rep.getCodDoc().equals("05"))
/* 205 */         jasperFile = "notaDebitoFinal.jasper";
/* 206 */       if (rep.getCodDoc().equals("06"))
/* 207 */         jasperFile = "guiaRemisionFinal.jasper";
/* 208 */       if (rep.getCodDoc().equals("07"))
/* 209 */         jasperFile = "comprobanteRetencion.jasper";
     }
     try
     {
				
/* 213 */       if (rep.getCodDoc().equals("01"))
/* 214 */         reportePdf=rep.generarReporteFac(emite,rutaJasper + jasperFile, rutaReporte + nameReporte);
/* 215 */       if (rep.getCodDoc().equals("04"))
/* 216 */         reportePdf= rep.generarReporteCred(emite,rutaJasper + jasperFile, rutaReporte + nameReporte);
/* 217 */       if (rep.getCodDoc().equals("05"))
/* 218 */         rep.generarReporteNotaDebito(rutaJasper + jasperFile, rutaReporte + nameReporte);
/* 219 */       if (rep.getCodDoc().equals("06"))
/* 220 */         rep.generarReporteGuia(rutaJasper + jasperFile, rutaReporte + nameReporte);
/* 221 */       if (rep.getCodDoc().equals("07"))
/* 222 */         reportePdf=rep.generarReporteRetencion(emite,rutaJasper + jasperFile, rutaReporte + nameReporte);
     }
     catch (SQLException e)
     {				
/* 226 */       e.printStackTrace();
				return "";
     }
     catch (ClassNotFoundException e) {
/* 229 */       e.printStackTrace();
				return "";
     }
 
/* 232 */     return reportePdf;
   }

   public String generarReporteFac(com.cimait.invoicec.bean.Emisor emite,
								   String urlReporte, 
								   String numfact)
     throws SQLException, ClassNotFoundException
   {
/*  72 */     FileInputStream is = null;
/*  73 */     JRDataSource dataSource = null;
/*  74 */     List detallesAdiciones = new ArrayList();
/*  75 */     List infoAdicional = new ArrayList();
/*  76 */     List detDocumento = new ArrayList();
/*  77 */     List detAdicional = new ArrayList();
     try
     {
				detDocumento = emite.getInfEmisor().getListDetDocumentos();/*
				if (emite.getInfEmisor().getListInfAdicional()!= null){
		       		if (emite.getInfEmisor().getListInfAdicional().size()>0) {
			       		  for (int i = 0; i < emite.getInfEmisor().getListInfAdicional().size(); i++) {
			       		    InformacionAdicional infoAd = new InformacionAdicional();
			       		    infoAd.setNombre(emite.getInfEmisor().getListInfAdicional().get(i).getName());
			       		    infoAd.setValor(emite.getInfEmisor().getListInfAdicional().get(i).getValue());
			       		    infoAdicional.add(i, infoAd);
			       		  }
			       		}
				}*/
				if (detDocumento!= null){
/*  90 */        if (!detDocumento.isEmpty()){
/*  91 */         for (int i = 0; i < emite.getInfEmisor().getListDetDocumentos().size(); i++) {
/*  93 */           DetallesAdicionalesReporte detAd = new DetallesAdicionalesReporte();
					detAd.setCodigoPrincipal(emite.getInfEmisor().getListDetDocumentos().get(i).getCodigoPrincipal());
///*  95 */         
					//detAd = emite.getInfEmisor().getListDetDocumentos().get(i).getListDetAdicionalesDocumentos();
/*  99 */           detAd.setDescuento(String.valueOf((emite.getInfEmisor().getListDetDocumentos().get(i).getDescuento())));
/* 100 */           detAd.setCodigoAuxiliar(emite.getInfEmisor().getListDetDocumentos().get(i).getCodigoAuxiliar());
/* 101 */           detAd.setDescripcion(emite.getInfEmisor().getListDetDocumentos().get(i).getDescripcion());
/* 102 */           detAd.setCantidad(String.valueOf(emite.getInfEmisor().getListDetDocumentos().get(i).getCantidad()));
/* 103 */           detAd.setPrecioTotalSinImpuesto(String.valueOf(emite.getInfEmisor().getListDetDocumentos().get(i).getPrecioTotalSinImpuesto()));
/* 104 */           detAd.setPrecioUnitario(new Double(emite.getInfEmisor().getListDetDocumentos().get(i).getPrecioUnitario()).toString());
/* 105 */           detAd.setInfoAdicional(infoAdicional.isEmpty() ? null : infoAdicional);
/* 106 */           detallesAdiciones.add(i, detAd);
         }
			    }
     }
			  }
     catch (Exception e) {
/* 110 */       e.printStackTrace();
     }
     try {
/* 113 */       dataSource = new JRBeanCollectionDataSource(detallesAdiciones);
			    //urlReporte="/usr/facElectronica/Procesos/FacturadorElectronico/resources/reportes/factura.jasper";
				System.out.println("urlReporte::"+urlReporte);
/* 114 */       is = new FileInputStream(urlReporte);
			    JasperPrint reporte_view = JasperFillManager.fillReport(is, obtenerMapaParametrosReportes(obtenerParametrosInfoTriobutaria(emite), obtenerInfoFactura(emite)), dataSource);
				System.out.println("numfact::"+numfact);
				//System.out.println("reporte_view::"+reporte_view);
/* 116 */       JasperExportManager.exportReportToPdfFile(reporte_view, numfact);
      
			  }
     catch (FileNotFoundException ex) {
/* 119 */       Logger.getLogger(ReporteUtil.class.getName()).log(Level.SEVERE, null, ex);
       try
       {
/* 124 */         if (is != null)
/* 125 */           is.close();
       }
       catch (IOException ex1) {
/* 128 */         Logger.getLogger(ReporteUtil.class.getName()).log(Level.SEVERE, null, ex1);
       }
     }
     catch (JRException e)
     {
/* 121 */       Logger.getLogger(ReporteUtil.class.getName()).log(Level.SEVERE, null, e);
       try
       {
/* 124 */         if (is != null)
/* 125 */           is.close();
       }
       catch (IOException ex) {
/* 128 */         Logger.getLogger(ReporteUtil.class.getName()).log(Level.SEVERE, null, ex);
       }
     }
     finally
     {
       try
       {
/* 124 */         if (is != null)
/* 125 */           is.close();
       }
       catch (IOException ex) {
/* 128 */         Logger.getLogger(ReporteUtil.class.getName()).log(Level.SEVERE, null, ex);
       }
     }
				return numfact;
   }

   
  public String generarReporteCred(com.cimait.invoicec.bean.Emisor emite,
		   String urlReporte, 
		   String numcred)
throws SQLException, ClassNotFoundException
{
/*  72 */     FileInputStream is = null;
/*  73 */     JRDataSource dataSource = null;
/*  74 */     List detallesAdiciones = new ArrayList();
/*  75 */     List infoAdicional = new ArrayList();
/*  76 */     List detDocumento = new ArrayList();
/*  77 */     List detAdicional = new ArrayList();
try
{
detDocumento = emite.getInfEmisor().getListDetDocumentos();/*
if (emite.getInfEmisor().getListInfAdicional()!= null){
if (emite.getInfEmisor().getListInfAdicional().size()>0) {
	  for (int i = 0; i < emite.getInfEmisor().getListInfAdicional().size(); i++) {
	    InformacionAdicional infoAd = new InformacionAdicional();
	    infoAd.setNombre(emite.getInfEmisor().getListInfAdicional().get(i).getName());
	    infoAd.setValor(emite.getInfEmisor().getListInfAdicional().get(i).getValue());
	    infoAdicional.add(i, infoAd);
	  }
	}
}*/
if (detDocumento!= null){
/*  90 */        if (!detDocumento.isEmpty()){
/*  91 */         for (int i = 0; i < emite.getInfEmisor().getListDetDocumentos().size(); i++) {
/*  93 */           DetallesAdicionalesReporte detAd = new DetallesAdicionalesReporte();
					detAd.setCodigoPrincipal(emite.getInfEmisor().getListDetDocumentos().get(i).getCodigoPrincipal());

//detAd = emite.getInfEmisor().getListDetDocumentos().get(i).getListDetAdicionalesDocumentos();
/*  99 */           detAd.setDescuento(String.valueOf((emite.getInfEmisor().getListDetDocumentos().get(i).getDescuento())));
/* 100 */           detAd.setCodigoAuxiliar(emite.getInfEmisor().getListDetDocumentos().get(i).getCodigoAuxiliar());
/* 101 */           detAd.setDescripcion(emite.getInfEmisor().getListDetDocumentos().get(i).getDescripcion());
/* 102 */           detAd.setCantidad(String.valueOf(emite.getInfEmisor().getListDetDocumentos().get(i).getCantidad()));
/* 103 */           detAd.setPrecioTotalSinImpuesto(String.valueOf(emite.getInfEmisor().getListDetDocumentos().get(i).getPrecioTotalSinImpuesto()));
/* 104 */           detAd.setPrecioUnitario(new Double(emite.getInfEmisor().getListDetDocumentos().get(i).getPrecioUnitario()).toString());
/* 105 */           detAd.setInfoAdicional(infoAdicional.isEmpty() ? null : infoAdicional);
/* 106 */           detallesAdiciones.add(i, detAd);
}
}
}
}
catch (Exception e) {
/* 110 */       e.printStackTrace();
}
try {
/* 113 */       dataSource = new JRBeanCollectionDataSource(detallesAdiciones);
//urlReporte="/usr/facElectronica/Procesos/FacturadorElectronico/resources/reportes/factura.jasper";
System.out.println("urlReporte::"+urlReporte);
/* 114 */       is = new FileInputStream(urlReporte);
JasperPrint reporte_view = JasperFillManager.fillReport(is, obtenerMapaParametrosReportes(obtenerParametrosInfoTriobutaria(emite), obtenerInfoCredito(emite)), dataSource);
System.out.println("numcred::"+numcred);
//System.out.println("reporte_view::"+reporte_view);
/* 116 */       JasperExportManager.exportReportToPdfFile(reporte_view, numcred);

}
catch (FileNotFoundException ex) {
/* 119 */       Logger.getLogger(ReporteUtil.class.getName()).log(Level.SEVERE, null, ex);
try
{
/* 124 */         if (is != null)
/* 125 */           is.close();
}
catch (IOException ex1) {
/* 128 */         Logger.getLogger(ReporteUtil.class.getName()).log(Level.SEVERE, null, ex1);
}
}
catch (JRException e)
{
/* 121 */       Logger.getLogger(ReporteUtil.class.getName()).log(Level.SEVERE, null, e);
try
{
/* 124 */         if (is != null)
/* 125 */           is.close();
}
catch (IOException ex) {
/* 128 */         Logger.getLogger(ReporteUtil.class.getName()).log(Level.SEVERE, null, ex);
}
}
finally
{
try
{
/* 124 */         if (is != null)
/* 125 */           is.close();
}
catch (IOException ex) {
/* 128 */         Logger.getLogger(ReporteUtil.class.getName()).log(Level.SEVERE, null, ex);
}
}
return numcred;
}


	public static boolean generaPdfDocumentos(com.cimait.invoicec.bean.Emisor emite, 
											  String rutaJasper, 
											  String rutaReporte, 
											  String nameReporte)
     throws Exception
   {
/* 172 */     ReporteUtil rep = new ReporteUtil();
			  
/* 174 */     rep.setRuc(emite.getInfEmisor().getRuc());
/* 175 */     rep.setCodEst(emite.getInfEmisor().getCodEstablecimiento());
/* 176 */     rep.setCodPuntEm(emite.getInfEmisor().getCodPuntoEmision());
/* 177 */     rep.setCodDoc(emite.getInfEmisor().getCodDocumento());
/* 178 */     rep.setSecuencial(emite.getInfEmisor().getSecuencial());
/* 179 */     classReference = "ReporteUtil";
/* 180 */     String name_xml = "facturacion.xml";
 	
			   
/* 189 */     String jasperFile = "";
     try {
/* 191 */       jasperFile = Environment.c.getString("facElectronica.pdf.jasper.doc" + rep.getRuc() + "_" + rep.getCodDoc()) == null ? "" : Environment.c.getString("facElectronica.pdf.jasper.doc" + rep.getRuc() + "_" + rep.getCodDoc());
     }
     catch (Exception e) {
/* 194 */       jasperFile = Environment.c.getString("facElectronica.pdf.jasper.doc" + rep.getRuc() + "_" + rep.getCodDoc()) == null ? "" : Environment.c.getString("facElectronica.pdf.jasper.doc" + rep.getRuc() + "_" + rep.getCodDoc());
     }
/* 196 */     if ((jasperFile.equals("")) || (jasperFile == null)) {
/* 197 */       jasperFile = Environment.c.getString("facElectronica.pdf.jasper.doc" + rep.getRuc() + "_" + rep.getCodDoc()) == null ? "" : Environment.c.getString("facElectronica.pdf.jasper.doc" + rep.getRuc() + "_" + rep.getCodDoc());
     }
/* 199 */     if ((jasperFile.equals("")) || (jasperFile == null)) {
/* 200 */       if (rep.getCodDoc().equals("01"))
/* 201 */         jasperFile = "factura.jasper";
/* 202 */       if (rep.getCodDoc().equals("04"))
/* 203 */         jasperFile = "notaCreditoFinal.jasper";
/* 204 */       if (rep.getCodDoc().equals("05"))
/* 205 */         jasperFile = "notaDebitoFinal.jasper";
/* 206 */       if (rep.getCodDoc().equals("06"))
/* 207 */         jasperFile = "guiaRemisionFinal.jasper";
/* 208 */       if (rep.getCodDoc().equals("07"))
/* 209 */         jasperFile = "comprobanteRetencion.jasper";
     }
     try
     {
/* 213 */       if (rep.getCodDoc().equals("01"))
/* 214 */         rep.generarReporteFac(rutaJasper + jasperFile, rutaReporte + nameReporte);
/* 215 */       if (rep.getCodDoc().equals("04"))
/* 216 */         rep.generarReporteNotaCredito(rutaJasper + jasperFile, rutaReporte + nameReporte);
/* 217 */       if (rep.getCodDoc().equals("05"))
/* 218 */         rep.generarReporteNotaDebito(rutaJasper + jasperFile, rutaReporte + nameReporte);
/* 219 */       if (rep.getCodDoc().equals("06"))
/* 220 */         rep.generarReporteGuia(rutaJasper + jasperFile, rutaReporte + nameReporte);
/* 221 */       //if (rep.getCodDoc().equals("07"))
/* 222 */         //rep.generarReporteRetencion(rutaJasper + jasperFile, rutaReporte + nameReporte);
     }
     catch (SQLException e)
     {
/* 226 */       e.printStackTrace();
     }
     catch (ClassNotFoundException e) {
/* 229 */       e.printStackTrace();
     }
 
/* 232 */     return true;
   }
 
   public void generarReporteNotaDebito(String urlReporte, String numrep)
     throws SQLException, ClassNotFoundException
   {
/* 240 */     FileInputStream is = null;
     try
     {
/* 243 */       List debito = new ArrayList();
/* 244 */       List adicional = new ArrayList();
/* 245 */       List detAdicional = new ArrayList();
/* 246 */       List infoAdicional = new ArrayList();
       try
       {
/* 249 */         adicional = this.servicio.buscarDetAdicional(this.Ruc, this.codEst, this.codPuntEm, this.codDoc, this.secuencial);
/* 250 */         debito = this.servicio.buscarMotivosDebito(this.Ruc, this.codEst, this.codPuntEm, this.codDoc, this.secuencial);
/* 251 */         if (!adicional.isEmpty()) {
/* 252 */           for (int i = 0; i < adicional.size(); i++) {
/* 253 */             InformacionAdicional info = new InformacionAdicional();
/* 254 */             info.setNombre(((FacDetAdicional)adicional.get(i)).getNombre());
/* 255 */             info.setValor(((FacDetAdicional)adicional.get(i)).getValor());
/* 256 */             infoAdicional.add(i, info);
           }
         }
/* 259 */         if (!debito.isEmpty())
/* 260 */           for (int i = 0; i < debito.size(); i++) {
/* 261 */             DetallesAdicionales detAdi = new DetallesAdicionales();
/* 262 */             detAdi.setRazonModificacion(((FacDetMotivosdebito)debito.get(i)).getRazon());
/* 263 */             detAdi.setValorModificacion(String.valueOf(((FacDetMotivosdebito)debito.get(i)).getBaseImponible()));
/* 264 */             detAdi.setInfoAdicional(infoAdicional.isEmpty() ? null : infoAdicional);
/* 265 */             detAdicional.add(i, detAdi);
           }
       }
       catch (Exception e)
       {
/* 270 */         e.printStackTrace();
       }
/* 272 */       JRDataSource dataSource = new JRBeanCollectionDataSource(detAdicional);
/* 273 */       is = new FileInputStream(urlReporte);
/* 274 */       JasperPrint reporte_view = JasperFillManager.fillReport(is, obtenerMapaParametrosReportes(obtenerParametrosInfoTriobutaria(), obtenerInfoND()), dataSource);
/* 275 */       JasperExportManager.exportReportToPdfFile(reporte_view, numrep);
     }
     catch (FileNotFoundException ex) {
/* 278 */       Logger.getLogger(ReporteUtil.class.getName()).log(Level.SEVERE, null, ex);
/* 279 */       ex.printStackTrace();
       try
       {
/* 285 */         if (is != null)
/* 286 */           is.close();
       }
       catch (IOException ex1) {
/* 289 */         Logger.getLogger(ReporteUtil.class.getName()).log(Level.SEVERE, null, ex1);
       }
     }
     catch (JRException e)
     {
/* 281 */       Logger.getLogger(ReporteUtil.class.getName()).log(Level.SEVERE, null, e);
/* 282 */       e.printStackTrace();
       try
       {
/* 285 */         if (is != null)
/* 286 */           is.close();
       }
       catch (IOException ex) {
/* 289 */         Logger.getLogger(ReporteUtil.class.getName()).log(Level.SEVERE, null, ex);
       }
     }
     finally
     {
       try
       {
/* 285 */         if (is != null)
/* 286 */           is.close();
       }
       catch (IOException ex) {
/* 289 */         Logger.getLogger(ReporteUtil.class.getName()).log(Level.SEVERE, null, ex);
       }
     }
   }

   public void generarReporteNotaCredito(com.cimait.invoicec.bean.Emisor emite,
		   								 String urlReporte, 
		   								 String numrep)
		     throws SQLException, ClassNotFoundException
		   {
		/* 299 */     FileInputStream is = null;
		     try {
		/* 301 */       List detallesAdiciones = new ArrayList();
		/* 302 */       List infoAdicional = new ArrayList();
		/* 303 */       List detDocumento = new ArrayList();
		/* 304 */       List detAdicional = new ArrayList();
		 
		/* 306 */       JRDataSource dataSource = null;
		       try {
		    	   detDocumento = emite.getInfEmisor().getListDetDocumentos();
		/* 309 */         detAdicional = emite.getInfEmisor().getListInfAdicional();
		/* 310 */         if (!detAdicional.isEmpty()) {
		/* 311 */           for (int i = 0; i < detAdicional.size(); i++) {
		/* 312 */             InformacionAdicional infoAd = new InformacionAdicional();
		/* 313 */             infoAd.setNombre(((FacDetAdicional)detAdicional.get(i)).getNombre());
		/* 314 */             infoAd.setValor(((FacDetAdicional)detAdicional.get(i)).getValor());
		/* 315 */             infoAdicional.add(i, infoAd);
		           }
		         }
		 
		/* 319 */         if (!detDocumento.isEmpty()) {
		/* 320 */           for (int i = 0; i < detDocumento.size(); i++) {
		/* 321 */             //FacProducto producto = new FacProducto();
		/* 322 */             DetallesAdicionalesReporte detAd = new DetallesAdicionalesReporte();
		/* 323 */             detAd.setCodigoPrincipal(((FacDetDocumento)detDocumento.get(i)).getCodPrincipal());
		/* 324 */             //producto = this.servicio.buscarProductos(Integer.valueOf(((FacDetDocumento)detDocumento.get(i)).getCodPrincipal().trim()).intValue());
		/* 325 */             //detAd.setDetalle1(producto.getAtributo1());
		/* 326 */             //detAd.setDetalle2(producto.getAtributo2());
		/* 327 */             //detAd.setDetalle3(producto.getAtributo3());
		/* 328 */             detAd.setDescuento(String.valueOf(((FacDetDocumento)detDocumento.get(i)).getDescuento()));
		/* 329 */             detAd.setCodigoAuxiliar(((FacDetDocumento)detDocumento.get(i)).getCodAuxiliar());
		/* 330 */             detAd.setDescripcion(((FacDetDocumento)detDocumento.get(i)).getDescripcion());
		/* 331 */             detAd.setCantidad(((FacDetDocumento)detDocumento.get(i)).getCantidad().toString());
		/* 332 */             detAd.setPrecioTotalSinImpuesto(String.valueOf(((FacDetDocumento)detDocumento.get(i)).getPrecioTotalSinImpuesto()));
		/* 333 */             detAd.setPrecioUnitario(String.valueOf(((FacDetDocumento)detDocumento.get(i)).getPrecioUnitario()));
		/* 334 */             detAd.setInfoAdicional(infoAdicional.isEmpty() ? null : infoAdicional);
		/* 335 */             detallesAdiciones.add(i, detAd);
		           }
		         }
		 
		       }
		       catch (Exception e)
		       {
		/* 342 */         e.printStackTrace();
		       }
		/* 344 */       dataSource = new JRBeanCollectionDataSource(detallesAdiciones);
		 
		/* 346 */       is = new FileInputStream(urlReporte);
		/* 347 */       JasperPrint reporte_view = JasperFillManager.fillReport(is, obtenerMapaParametrosReportes(obtenerParametrosInfoTriobutaria(emite), obtenerInfoNC()), dataSource);
		/* 348 */       JasperExportManager.exportReportToPdfFile(reporte_view, numrep);
		     }
		     catch (FileNotFoundException ex) {
		/* 351 */       Logger.getLogger(ReporteUtil.class.getName()).log(Level.SEVERE, null, ex);
		       try
		       {
		/* 356 */         if (is != null)
		/* 357 */           is.close();
		       }
		       catch (IOException ex1) {
		/* 360 */         Logger.getLogger(ReporteUtil.class.getName()).log(Level.SEVERE, null, ex1);
		       }
		     }
		     catch (JRException e)
		     {
		/* 353 */       Logger.getLogger(ReporteUtil.class.getName()).log(Level.SEVERE, null, e);
		       try
		       {
		/* 356 */         if (is != null)
		/* 357 */           is.close();
		       }
		       catch (IOException ex) {
		/* 360 */         Logger.getLogger(ReporteUtil.class.getName()).log(Level.SEVERE, null, ex);
		       }
		     }
		     finally
		     {
		       try
		       {
		/* 356 */         if (is != null)
		/* 357 */           is.close();
		       }
		       catch (IOException ex) {
		/* 360 */         Logger.getLogger(ReporteUtil.class.getName()).log(Level.SEVERE, null, ex);
		       }
		     }
		   }
   
   public void generarReporteNotaCredito(String urlReporte, String numrep)
     throws SQLException, ClassNotFoundException
   {
/* 299 */     FileInputStream is = null;
     try {
/* 301 */       List detallesAdiciones = new ArrayList();
/* 302 */       List infoAdicional = new ArrayList();
/* 303 */       List detDocumento = new ArrayList();
/* 304 */       List detAdicional = new ArrayList();
 
/* 306 */       JRDataSource dataSource = null;
       try {
/* 308 */        
    	   		  detDocumento = this.servicio.buscarDatosDetallesDocumentos(this.Ruc, this.codEst, this.codPuntEm, this.codDoc, this.secuencial);
/* 309 */         detAdicional = this.servicio.buscarDetAdicional(this.Ruc, this.codEst, this.codPuntEm, this.codDoc, this.secuencial);
/* 310 */         if (!detAdicional.isEmpty()) {
/* 311 */           for (int i = 0; i < detAdicional.size(); i++) {
/* 312 */             InformacionAdicional infoAd = new InformacionAdicional();
/* 313 */             infoAd.setNombre(((FacDetAdicional)detAdicional.get(i)).getNombre());
/* 314 */             infoAd.setValor(((FacDetAdicional)detAdicional.get(i)).getValor());
/* 315 */             infoAdicional.add(i, infoAd);
           }
         }
 
/* 319 */         if (!detDocumento.isEmpty()) {
/* 320 */           for (int i = 0; i < detDocumento.size(); i++) {
/* 321 */             FacProducto producto = new FacProducto();
/* 322 */             DetallesAdicionalesReporte detAd = new DetallesAdicionalesReporte();
/* 323 */             detAd.setCodigoPrincipal(((FacDetDocumento)detDocumento.get(i)).getCodPrincipal());
/* 324 */             producto = this.servicio.buscarProductos(Integer.valueOf(((FacDetDocumento)detDocumento.get(i)).getCodPrincipal().trim()).intValue());
/* 325 */             detAd.setDetalle1(producto.getAtributo1());
/* 326 */             detAd.setDetalle2(producto.getAtributo2());
/* 327 */             detAd.setDetalle3(producto.getAtributo3());
/* 328 */             detAd.setDescuento(String.valueOf(((FacDetDocumento)detDocumento.get(i)).getDescuento()));
/* 329 */             detAd.setCodigoAuxiliar(((FacDetDocumento)detDocumento.get(i)).getCodAuxiliar());
/* 330 */             detAd.setDescripcion(((FacDetDocumento)detDocumento.get(i)).getDescripcion());
/* 331 */             detAd.setCantidad(((FacDetDocumento)detDocumento.get(i)).getCantidad().toString());
/* 332 */             detAd.setPrecioTotalSinImpuesto(String.valueOf(((FacDetDocumento)detDocumento.get(i)).getPrecioTotalSinImpuesto()));
/* 333 */             detAd.setPrecioUnitario(String.valueOf(((FacDetDocumento)detDocumento.get(i)).getPrecioUnitario()));
/* 334 */             detAd.setInfoAdicional(infoAdicional.isEmpty() ? null : infoAdicional);
/* 335 */             detallesAdiciones.add(i, detAd);
           }
         }
 
       }
       catch (Exception e)
       {
/* 342 */         e.printStackTrace();
       }
/* 344 */       dataSource = new JRBeanCollectionDataSource(detallesAdiciones);
 
/* 346 */       is = new FileInputStream(urlReporte);
/* 347 */       JasperPrint reporte_view = JasperFillManager.fillReport(is, obtenerMapaParametrosReportes(obtenerParametrosInfoTriobutaria(), obtenerInfoNC()), dataSource);
/* 348 */       JasperExportManager.exportReportToPdfFile(reporte_view, numrep);
     }
     catch (FileNotFoundException ex) {
/* 351 */       Logger.getLogger(ReporteUtil.class.getName()).log(Level.SEVERE, null, ex);
       try
       {
/* 356 */         if (is != null)
/* 357 */           is.close();
       }
       catch (IOException ex1) {
/* 360 */         Logger.getLogger(ReporteUtil.class.getName()).log(Level.SEVERE, null, ex1);
       }
     }
     catch (JRException e)
     {
/* 353 */       Logger.getLogger(ReporteUtil.class.getName()).log(Level.SEVERE, null, e);
       try
       {
/* 356 */         if (is != null)
/* 357 */           is.close();
       }
       catch (IOException ex) {
/* 360 */         Logger.getLogger(ReporteUtil.class.getName()).log(Level.SEVERE, null, ex);
       }
     }
     finally
     {
       try
       {
/* 356 */         if (is != null)
/* 357 */           is.close();
       }
       catch (IOException ex) {
/* 360 */         Logger.getLogger(ReporteUtil.class.getName()).log(Level.SEVERE, null, ex);
       }
     }
   }
 
   public void generarReporteGuia(String urlReporte, String numrep)
     throws SQLException, ClassNotFoundException
   {
/* 368 */     FileInputStream is = null;
     try
     {
/* 371 */       List detDocumento = new ArrayList();
/* 372 */       List detGuia = new ArrayList();
 
/* 374 */       FacCabDocumento cabDoc = new FacCabDocumento();
/* 375 */       List guiaLista = new ArrayList();
       try {
/* 377 */         cabDoc = this.servicio.buscarDatosCabDocumentos(this.Ruc, this.codEst, this.codPuntEm, this.codDoc, this.secuencial);
/* 378 */         detDocumento = this.servicio.buscarDatosDetallesDocumentos(this.Ruc, this.codEst, this.codPuntEm, this.codDoc, this.secuencial);
/* 379 */         if (!detDocumento.isEmpty())
         {
/* 381 */           for (int i = 0; i < detDocumento.size(); i++) {
/* 382 */             DetalleGuiaReporte guiaRem = new DetalleGuiaReporte();
/* 383 */             guiaRem.setCantidad(String.valueOf(((FacDetDocumento)detDocumento.get(i)).getCantidad()));
/* 384 */             guiaRem.setCodigoAuxiliar(((FacDetDocumento)detDocumento.get(i)).getCodAuxiliar());
/* 385 */             guiaRem.setCodigoPrincipal(((FacDetDocumento)detDocumento.get(i)).getCodPrincipal());
/* 386 */             guiaRem.setDescripcion(((FacDetDocumento)detDocumento.get(i)).getDescripcion());
/* 387 */             detGuia.add(i, guiaRem);
           }
 
/* 390 */           GuiaRemisionReporte rep = new GuiaRemisionReporte();
/* 391 */           rep.setCodigoEstab(cabDoc.getCodEstablecimientoDest());
/* 392 */           rep.setDestino(cabDoc.getRuta());
/* 393 */           rep.setDetalles(detGuia);
/* 394 */           rep.setDocAduanero(cabDoc.getDocAduaneroUnico());
/* 395 */           rep.setFechaEmisionSustento(cabDoc.getFechaEmisionDocSustento() == null ? null : new SimpleDateFormat("dd/MM/yyyy hh:mm:ss").format(cabDoc.getFechaEmisionDocSustento()));
/* 396 */           rep.setMotivoTraslado(cabDoc.getMotivoTraslado());
/* 397 */           rep.setNombreComprobante(cabDoc.getCodigoDocumento());
/* 398 */           rep.setNumDocSustento(cabDoc.getNumDocSustento());
/* 399 */           rep.setNumeroAutorizacion(String.valueOf(cabDoc.getNumAutDocSustento()));
/* 400 */           rep.setRazonSocial(cabDoc.getRazonSocialDestinatario());
/* 401 */           rep.setRucDestinatario(cabDoc.getIdentificacionDestinatario());
/* 402 */           rep.setRuta(cabDoc.getRuta());
/* 403 */           guiaLista.add(rep);
         }
       }
       catch (Exception e)
       {
/* 408 */         e.printStackTrace();
       }
/* 410 */       JRDataSource dataSource = new JRBeanCollectionDataSource(guiaLista);
/* 411 */       is = new FileInputStream(urlReporte);
/* 412 */       JasperPrint reporte_view = JasperFillManager.fillReport(is, obtenerMapaParametrosReportes(obtenerParametrosInfoTriobutaria(), obtenerInfoGR()), dataSource);
/* 413 */       JasperExportManager.exportReportToPdfFile(reporte_view, numrep);
     }
     catch (FileNotFoundException ex) {
/* 416 */       ex.printStackTrace();
/* 417 */       Logger.getLogger(ReporteUtil.class.getName()).log(Level.SEVERE, null, ex);
       try
       {
/* 423 */         if (is != null)
/* 424 */           is.close();
       }
       catch (IOException ex1) {
/* 427 */         Logger.getLogger(ReporteUtil.class.getName()).log(Level.SEVERE, null, ex1);
       }
     }
     catch (JRException e)
     {
/* 419 */       e.printStackTrace();
/* 420 */       Logger.getLogger(ReporteUtil.class.getName()).log(Level.SEVERE, null, e);
       try
       {
/* 423 */         if (is != null)
/* 424 */           is.close();
       }
       catch (IOException ex) {
/* 427 */         Logger.getLogger(ReporteUtil.class.getName()).log(Level.SEVERE, null, ex);
       }
     }
     finally
     {
       try
       {
/* 423 */         if (is != null)
/* 424 */           is.close();
       }
       catch (IOException ex) {
/* 427 */         Logger.getLogger(ReporteUtil.class.getName()).log(Level.SEVERE, null, ex);
       }
     }
   }
 

    public String generarReporteRetencion(com.cimait.invoicec.bean.Emisor emite,
											  String urlReporte, 
											  String numret)
     throws SQLException, ClassNotFoundException
   {
	 FileInputStream is = null;
     try {
       List detRetencion = new ArrayList(); 
       FacCabDocumento cabDoc = new FacCabDocumento();
       InformacionAdicional info = null;
       List infoAdicional = new ArrayList();
       DetallesAdicionales detalles = null;
       List detallesAdicional = new ArrayList();
       List detAdicionals = new ArrayList();
       try {
    	   
           if (emite.getInfEmisor().getListDetImpuestosRetenciones().size()>0){
						for (int i = 0; i < emite.getInfEmisor().getListDetImpuestosRetenciones().size(); i++) {
							 String comprobante = "";
							 
							 if (emite.getInfEmisor().getListDetImpuestosRetenciones().get(i).getCodDocSustento().trim().equals("01")) comprobante = "FACTURA";
							 if (emite.getInfEmisor().getListDetImpuestosRetenciones().get(i).getCodDocSustento().trim().equals("04")) comprobante = "NOTA DE CREDITO";
							 if (emite.getInfEmisor().getListDetImpuestosRetenciones().get(i).getCodDocSustento().trim().equals("05")) comprobante = "NOTA DE DEBITO";
							 if (emite.getInfEmisor().getListDetImpuestosRetenciones().get(i).getCodDocSustento().trim().equals("06")) comprobante = "GUIA DE REMISION";
							 detalles = new DetallesAdicionales();
							 detalles.setBaseImponible(String.valueOf(emite.getInfEmisor().getListDetImpuestosRetenciones().get(i).getBaseImponible()));
							 detalles.setComprobante(comprobante);
							 detalles.setNombreComprobante(comprobante);
							 detalles.setFechaEmisionCcompModificado(emite.getInfEmisor().getListDetImpuestosRetenciones().get(i).getFechaEmisionDocSustento() == null ? null : (emite.getInfEmisor().getListDetImpuestosRetenciones().get(i).getFechaEmisionDocSustento()));
							 detalles.setNumeroComprobante(String.valueOf(emite.getInfEmisor().getListDetImpuestosRetenciones().get(i).getNumDocSustento()));
							 
							 detalles.setPorcentajeRetencion(String.valueOf(emite.getInfEmisor().getListDetImpuestosRetenciones().get(i).getCodigoRetencion()));
							 detalles.setPorcentajeRetener(String.valueOf((emite.getInfEmisor().getListDetImpuestosRetenciones().get(i).getPorcentajeRetener())));
							 detalles.setValorRetenido(String.valueOf((emite.getInfEmisor().getListDetImpuestosRetenciones().get(i).getValorRetenido())));
							 //detalles.setInfoAdicional(infoAdicional);
							 //FacGeneral general = new FacGeneral();
							 
							 String descripcion = "";
							 if (emite.getInfEmisor().getListDetImpuestosRetenciones().get(i).getCodigo()==1){
								 descripcion = "RENTA";
							 }
							 if (emite.getInfEmisor().getListDetImpuestosRetenciones().get(i).getCodigo()==2){
								 descripcion = "IVA";
							 }
							 if (emite.getInfEmisor().getListDetImpuestosRetenciones().get(i).getCodigo()==6){
								 descripcion = "ISD";
							 }
							 //general = this.servicio.buscarNombreCodigo(String.valueOf(((FacDetRetencione)detRetencion.get(i)).getCodImpuesto()), "29");							 
							 detalles.setNombreImpuesto(descripcion);							 
							 detallesAdicional.add(i, detalles);														
						}
				}/*
						detAdicionals = emite.getInfEmisor().getListInfAdicional();
						if (!detAdicionals.isEmpty()) {
						  for (int i = 0; i < detAdicionals.size(); i++) {
						    InformacionAdicional infoAd = new InformacionAdicional();
						    infoAd.setNombre(((FacDetAdicional)detAdicionals.get(i)).getNombre());
						    infoAd.setValor(((FacDetAdicional)detAdicionals.get(i)).getValor());
						    infoAdicional.add(i, infoAd);
						  }
						}*/
         		//JZU RETENCION
			    JRDataSource dataSource = new JRBeanCollectionDataSource(detallesAdicional);
				is = new FileInputStream(urlReporte);
				//JasperPrint reporte_view=JasperFillManager.fillReport(is, obtenerMapaParametrosReportes(obtenerParametrosInfoTriobutaria(), obtenerInfoCompRetencion()), dataSource);
				JasperPrint reporte_view = JasperFillManager.fillReport(is, obtenerMapaParametrosReportes(obtenerParametrosInfoTriobutaria(emite), obtenerInfoCompRetencion(emite)), dataSource);
				JasperExportManager.exportReportToPdfFile(reporte_view, numret);
     }
     catch (FileNotFoundException ex) {
    	 ex.printStackTrace();
       Logger.getLogger(ReporteUtil.class.getName()).log(Level.SEVERE, null, ex);
       try
       {
         if (is != null)
           is.close();
       }
       catch (IOException ex1) {
         Logger.getLogger(ReporteUtil.class.getName()).log(Level.SEVERE, null, ex1);
       }
     }
     catch (JRException e)
     {
    	 
       Logger.getLogger(ReporteUtil.class.getName()).log(Level.SEVERE, null, e);
       try
       {
         if (is != null)
           is.close();
       }
       catch (IOException ex) {
         Logger.getLogger(ReporteUtil.class.getName()).log(Level.SEVERE, null, ex);
       }
     }
     finally
     {
       try
       {
         if (is != null)
           is.close();
       }
       catch (IOException ex) {
         Logger.getLogger(ReporteUtil.class.getName()).log(Level.SEVERE, null, ex);
       }
     }
   }catch (Exception ex) {
	   
	         Logger.getLogger(ReporteUtil.class.getName()).log(Level.SEVERE, null, ex);
}
     return numret;
}
 
   private Map<String, Object> obtenerMapaParametrosReportes(Map<String, Object> mapa1, Map<String, Object> mapa2)
   {
/* 511 */     mapa1.putAll(mapa2);
/* 512 */     return mapa1;
   }
 


   private Map<String, Object> obtenerParametrosInfoTriobutaria(com.cimait.invoicec.bean.Emisor emite) throws SQLException, ClassNotFoundException, FileNotFoundException
   {
/* 517 */     Map param = new HashMap(); 	  
/* 519 */     //FacCabDocumento cabDoc = new FacCabDocumento();
/* 520 */     FacEmpresa empresa = new FacEmpresa();
     try {
/* 522 */       /*cabDoc = this.servicio.buscarDatosCabDocumentos(this.Ruc, this.codEst, this.codPuntEm, this.codDoc, this.secuencial);
       			  if (cabDoc != null) {*/
/* 524 */         empresa = this.servicio.buscarEmpresa(emite.getInfEmisor().getRuc());
/* 525 */         param.put("RUC", emite.getInfEmisor().getRuc());
/* 526 */         System.out.println("GBM: Ubicando la clave de acceso en el PDF: " + emite.getInfEmisor().getClaveAcceso());
				  param.put("CLAVE_ACC", (emite.getInfEmisor().getClaveAcceso().trim().equals("")) || (emite.getInfEmisor().getClaveAcceso() == null) ? "" : emite.getInfEmisor().getClaveAcceso());
/* 527 */         param.put("RAZON_SOCIAL", emite.getInfEmisor().getRazonSocial());
/* 528 */         param.put("NOM_COMERCIAL", emite.getInfEmisor().getRazonSocial());
/* 529 */         param.put("DIR_MATRIZ", emite.getInfEmisor().getDireccionMatriz());
/* 530 */         param.put("SUBREPORT_DIR", "C://resources//reportes//");
/* 531 */         param.put("TIPO_EMISION", emite.getInfEmisor().getTipoEmision().equals("1") ? "NORMAL" : "CONTINGENCIA");
/* 532 */         param.put("NUM_AUT", (emite.getInfEmisor().getNumeroAutorizacion() == null) || (emite.getInfEmisor().getNumeroAutorizacion().equals("")) ? null : emite.getInfEmisor().getNumeroAutorizacion());
/* 533 */         param.put("FECHA_AUT", emite.getInfEmisor().getFechaAutorizacion() == null ? null : emite.getInfEmisor().getFechaAutorizacion());
/* 534 */         param.put("NUM_FACT", emite.getInfEmisor().getCodEstablecimiento() + "-" + emite.getInfEmisor().getCodPuntoEmision() + "-" + emite.getInfEmisor().getSecuencial());
/* 535 */         param.put("AMBIENTE", emite.getInfEmisor().getAmbiente() == 1 ? "PRUEBA" : "PRODUCCION");
/* 536 */         param.put("DIR_SUCURSAL", emite.getInfEmisor().getDireccionMatriz());
/* 537 */         param.put("CONT_ESPECIAL", emite.getInfEmisor().getContribEspecial());
/* 538 */         param.put("LLEVA_CONTABILIDAD", (emite.getInfEmisor().getObligContabilidad().trim().equals("S")||emite.getInfEmisor().getObligContabilidad().trim().equals("SI")) ? "SI" : "NO");

				  if (emite.getInfEmisor().getListInfAdicional()!= null){
						if (emite.getInfEmisor().getListInfAdicional().size()>0) {
				   		  for (int i = 0; i < emite.getInfEmisor().getListInfAdicional().size(); i++) {
				   		    //InformacionAdicional infoAd = new InformacionAdicional();
				   		    
				   		    /*GBM: Comentado el sig put porque se lo hace al final luego de procesar la trama*/
				   		    System.out.println("Info. Adicional: " + emite.getInfEmisor().getListInfAdicional().get(i).getName()+"-"+ capitalizeString(emite.getInfEmisor().getListInfAdicional().get(i).getValue()));
				   		    //param.put(emite.getInfEmisor().getListInfAdicional().get(i).getName(), capitalizeString(emite.getInfEmisor().getListInfAdicional().get(i).getValue()));
				   		    
				   		    /*GBM NUEVO BLOQUE PARA PROCESAMIENTO DE INFORMACIÓN ADICIONAL EN LAS FACTURAS*/
                            String value[] = null;
                            String name = null;
                            String info=emite.getInfEmisor().getListInfAdicional().get(i).getName();
                            String valores=emite.getInfEmisor().getListInfAdicional().get(i).getValue();
                            valores = valores.replace(",", " ");
                            valores = valores.replace("|", ",");
                            valores = valores.replace("NOVALUE", " ");
                            System.out.println("NAME::"+info);
                            if (info.equals("INFO1"))
                               {                                                               
                                       value = valores.split(",");
                                       System.out.println("TRAMA::"+valores);
                                       for (int j=0;j<value.length;j++){
                                               if (j == 0){        name = "HUESPED";  }
                                               if (j == 1){        name = "DIRECCION"; }
                                               if (j == 2){        name = "REFERENCIA"; }
                                               if (j == 3){        name = "CORTE"; }
                                               if (j == 4){        name = "EMAIL"; }
                                               System.out.println(name+"-"+ value[j]);
                                               param.put(name, value[j]);
                                       }
                               }else if (info.equals("INFO2"))
                               {
                                       value = valores.split(",");
                                       System.out.println("TRAMA::"+valores);
                                       for (int j=0;j<value.length;j++){                                
                                               if (j == 0){        name = "CIUDAD";  }
                                               if (j == 1){        name = "TELEFONO"; }
                                               if (j == 2){        name = "GRUPO"; }
                                               if (j == 3){        name = "CREDITO"; }
                                               System.out.println(name+"-"+ value[j]);
                                               param.put(name, value[j]);
                                       }
                               }else if (info.equals("INFO3")){
                               value = valores.split(",");
                               System.out.println("TRAMA::"+valores);
                               for (int j=0;j<value.length;j++){                                
                                       if (j == 0){        name = "CONFIRMACION"; }
                                       if (j == 1){        name = "HABITACION"; }
                                       if (j == 2){        name = "CAJERO"; }
                                       if (j == 3){        name = "USER"; }
                                       System.out.println(name+"-"+ value[j]);
                                       param.put(name, value[j]);
                               }
                               }else if (info.equals("INFO4")){
                            	   value = valores.split(",");
                            	   System.out.println("TRAMA::"+valores);
                            	   for (int j=0;j<value.length;j++){                                
                                       if (j == 0){        name = "LLEGADA"; }
                                       if (j == 1){        name = "SALIDA"; }
                                       if (j == 2){        name = "TEXT1"; }
                                       if (j == 3){        name = "TEXT2"; }
                                       System.out.println(name+"-"+ value[j]);
                                       param.put(name, value[j]);
                               }
                       }
                               else{
                                       System.out.println(info+"-"+ valores);
                                       param.put(info, valores);
                               }
                             }
                           }
				   		  }
				  File f = new File(empresa.getPathLogoEmpresa());
				  System.out.println("LOGO::"+empresa.getPathLogoEmpresa());
				  //if (((this.codDoc.equals("04")) || (this.codDoc.equals("05")) || (this.codDoc.equals("06")) || (this.codDoc.equals("07"))) && 
				  // (f.exists())) param.put("LOGO", new FileInputStream(empresa.getPathLogoEmpresa()));
				  //if ((this.codDoc.equals("01")) && 
				   //(f.exists())) 
				  param.put("LOGO", empresa.getPathLogoEmpresa());
				  
				  //String file = (emite.getInfEmisor().getAmbientePuntoEmision().equals("1") ? "produccion.jpeg" : "pruebas.jpeg");
				  //String ruta = (empresa.getPathMarcaAgua() == null) || (empresa.getPathMarcaAgua().trim().equals("")) ? "C://resources//images//" : empresa.getPathMarcaAgua();
				  /*System.out.println("MARCA_AGUA::" + ruta + file);
			         String marca = empresa.getMarcaAgua().trim().equals("S") ? ruta + file : "C://resources//images//produccion.jpeg";
			         f = new File(marca);
			         if (f.exists()) param.put("MARCA_AGUA", marca);
				  */
       //}
     }
/* 553 */     catch (Exception e) { e.printStackTrace(); }
 
/* 555 */     return param;
   }

   private Map<String, Object> obtenerParametrosInfoTriobutaria() throws SQLException, ClassNotFoundException, FileNotFoundException
   {
/* 517 */     Map param = new HashMap();
 
/* 519 */     FacCabDocumento cabDoc = new FacCabDocumento();
/* 520 */     FacEmpresa empresa = new FacEmpresa();
     try {
/* 522 */       cabDoc = this.servicio.buscarDatosCabDocumentos(this.Ruc, this.codEst, this.codPuntEm, this.codDoc, this.secuencial);
/* 523 */       if (cabDoc != null) {
/* 524 */         empresa = this.servicio.buscarEmpresa(cabDoc.getRuc());
/* 525 */         param.put("RUC", cabDoc.getRuc());
/* 526 */         param.put("CLAVE_ACC", (cabDoc.getClaveAcceso().trim().equals("")) || (cabDoc.getClaveAcceso() == null) ? "1111111" : cabDoc.getClaveAcceso());
/* 527 */         param.put("RAZON_SOCIAL", empresa.getRazonSocial());
/* 528 */         param.put("NOM_COMERCIAL", empresa.getRazonComercial());
/* 529 */         param.put("DIR_MATRIZ", empresa.getDireccionMatriz());
/* 530 */         param.put("SUBREPORT_DIR", "C://resources//reportes//");
/* 531 */         param.put("TIPO_EMISION", cabDoc.getTipoEmision().trim().equals("1") ? "NORMAL" : "CONTINGENCIA");
/* 532 */         param.put("NUM_AUT", (cabDoc.getNumAutDocSustento() == null) || (cabDoc.getNumAutDocSustento().equals("")) ? null : cabDoc.getNumAutDocSustento());
/* 533 */         param.put("FECHA_AUT", cabDoc.getFechaEmisionDocSustento() == null ? null : new SimpleDateFormat("dd/MM/yyyy").format(cabDoc.getFechaEmisionDocSustento()));
/* 534 */         param.put("NUM_FACT", cabDoc.getCodEstablecimiento() + "-" + cabDoc.getCodPuntEmision() + "-" + cabDoc.getSecuencial());
/* 535 */         param.put("AMBIENTE", cabDoc.getAmbiente().intValue() == 1 ? "PRUEBA" : "PRODUCCION");
/* 536 */         param.put("DIR_SUCURSAL", cabDoc.getDirEstablecimiento());
/* 537 */         param.put("CONT_ESPECIAL", empresa.getContribEspecial());
/* 538 */         param.put("LLEVA_CONTABILIDAD", cabDoc.getObligadoContabilidad());
/* 539 */         File f = new File(empresa.getPathLogoEmpresa());
/* 540 */         if (((this.codDoc.equals("04")) || (this.codDoc.equals("05")) || (this.codDoc.equals("06")) || (this.codDoc.equals("07"))) && 
/* 541 */           (f.exists())) param.put("LOGO", new FileInputStream(empresa.getPathLogoEmpresa()));
/* 542 */         if ((this.codDoc.equals("01")) && 
/* 543 */           (f.exists())) param.put("LOGO", empresa.getPathLogoEmpresa());
 
/* 545 */         String file = cabDoc.getAmbiente().intValue() == 1 ? "produccion.jpeg" : "pruebas.jpeg";
/* 546 */         String ruta = (empresa.getPathMarcaAgua() == null) || (empresa.getPathMarcaAgua().trim().equals("")) ? "C://resources//images//" : empresa.getPathMarcaAgua();
/* 547 */         /*System.out.println("MARCA_AGUA::" + ruta + file);
			         String marca = empresa.getMarcaAgua().trim().equals("S") ? ruta + file : "C://resources//images//produccion.jpeg";
			         f = new File(marca);
			         if (f.exists()) param.put("MARCA_AGUA", marca);
				  */
       }
     }
/* 553 */     catch (Exception e) { e.printStackTrace(); }
 
/* 555 */     return param;
   }
 
   private Map<String, Object> obtenerInfoFactura(com.cimait.invoicec.bean.Emisor emite)
   {
/* 560 */     Map param = new HashMap();
			  //GBM: Cambios para agregar base imponible al reporte
			  double baseImponible = 0;
 
/* 562 */     FacCabDocumento cabDoc = new FacCabDocumento();
     try {
/* 564 */       /*cabDoc = this.servicio.buscarDatosCabDocumentos(this.Ruc, this.codEst, this.codPuntEm, this.codDoc, this.secuencial);
	       		  if (cabDoc != null) {*/
/* 566 */         param.put("RS_COMPRADOR", capitalizeString(emite.getInfEmisor().getRazonSocialComp()));
/* 567 */         param.put("RUC_COMPRADOR", emite.getInfEmisor().getIdentificacionComp());
/* 568 */         param.put("FECHA_EMISION", emite.getInfEmisor().getFecEmision());
/* 569 */         param.put("GUIA", emite.getInfEmisor().getGuiaRemision());
/* 570 */         param.put("VALOR_TOTAL", Double.valueOf(emite.getInfEmisor().getImporteTotal()));
/* 571 */         param.put("IVA", Double.valueOf(emite.getInfEmisor().getTotalIva12()));
				  ArrayList<DetalleTotalImpuestos> lisDetImp = emite.getInfEmisor().getListDetDetImpuestos();
					for ( DetalleTotalImpuestos det : lisDetImp){
						System.out.println("codTotalImpuestos::"+det.getCodTotalImpuestos());
						System.out.println("codPorcentImpuestos::"+det.getCodPorcentImp());
						System.out.println("baseImponible::"+det.getBaseImponibleImp());
						//GBM: Obtener la base imponible... en la lógica actual se obtiene por detalle de impuesto
						baseImponible = baseImponible + det.getBaseImponibleImp();
						
						if ((det.getCodTotalImpuestos() == 2)&&(det.getCodPorcentImp() == 2)){
							cabDoc.setSubtotal12(det.getValorImp());
							System.out.println("Valor::getSubtotal12::"+cabDoc.getSubtotal12());
							
						}
						if ((det.getCodTotalImpuestos() == 2)&&(det.getCodPorcentImp() == 0)){
							cabDoc.setSubtotal0(det.getValorImp());
							System.out.println("Valor::getSubTotal0::"+cabDoc.getSubtotal0());
						}	
						
						if ((det.getCodTotalImpuestos() == 2)&&(det.getCodPorcentImp() == 2)){
							cabDoc.setSubtotal12(det.getValorImp());
							System.out.println("Valor::getSubtotal12::"+cabDoc.getSubtotal12());
							
						}
						if ((det.getCodTotalImpuestos() == 2)&&(det.getCodPorcentImp() == 0)){
							cabDoc.setSubtotal0(det.getValorImp());
							System.out.println("Valor::getSubTotal0::"+cabDoc.getSubtotal0());
						}
					}
				  System.out.println("obtenerInfoFactura::getSubtotal0::"+cabDoc.getSubtotal0());
				  System.out.println("obtenerInfoFactura::getSubtotal12::"+cabDoc.getSubtotal12());
/* 572 */         param.put("IVA_0", Double.valueOf(cabDoc.getSubtotal0()));
/* 573 */         param.put("IVA_12", Double.valueOf(cabDoc.getSubtotal12()));
/* 574 */         param.put("ICE", Double.valueOf(emite.getInfEmisor().getTotalICE()));
/* 575 */         param.put("NO_OBJETO_IVA", Double.valueOf(emite.getInfEmisor().getTotalSinImpuestos()));
/* 576 */         param.put("SUBTOTAL", Double.valueOf(emite.getInfEmisor().getTotalSinImpuestos()));
/* 577 */         param.put("PROPINA", Double.valueOf(emite.getInfEmisor().getPropina()));
/* 578 */         param.put("TOTAL_DESCUENTO", Double.valueOf(emite.getInfEmisor().getTotalDescuento()));
				  //GBM: Agregar la base a los parámetros que se envían al reporte
				  param.put("BASEIMPONIBLE", baseImponible);
        //}
     } catch (Exception e) {
/* 581 */       e.printStackTrace();
     }
/* 583 */     return param;
   }

   private Map<String, Object> obtenerInfoCredito(com.cimait.invoicec.bean.Emisor emite)
   {
/* 560 */     Map param = new HashMap();
 
/* 562 */     FacCabDocumento cabDoc = new FacCabDocumento();
     try {
/* 564 */       /*cabDoc = this.servicio.buscarDatosCabDocumentos(this.Ruc, this.codEst, this.codPuntEm, this.codDoc, this.secuencial);
	       		  if (cabDoc != null) {*/
/* 566 */         param.put("RS_COMPRADOR", capitalizeString(emite.getInfEmisor().getRazonSocialComp()));
/* 567 */         param.put("RUC_COMPRADOR", emite.getInfEmisor().getIdentificacionComp());
/* 568 */         param.put("FECHA_EMISION", emite.getInfEmisor().getFecEmision());
/* 569 */         param.put("GUIA", emite.getInfEmisor().getGuiaRemision());
/* 570 */         param.put("VALOR_TOTAL", Double.valueOf(emite.getInfEmisor().getImporteTotal()));
/* 571 */         param.put("IVA", Double.valueOf(emite.getInfEmisor().getTotalIva12()));

/* 572 */         param.put("IVA_0", Double.valueOf(emite.getInfEmisor().getTotalSinImpuestos()));
/* 573 */         param.put("IVA_12", Double.valueOf(emite.getInfEmisor().getTotalIva12()));
/* 574 */         param.put("ICE", Double.valueOf(emite.getInfEmisor().getTotalICE()));
/* 575 */         param.put("NO_OBJETO_IVA", Double.valueOf(emite.getInfEmisor().getTotalSinImpuestos()));
/* 576 */         param.put("SUBTOTAL", Double.valueOf(emite.getInfEmisor().getTotalSinImpuestos()));
/* 577 */         param.put("PROPINA", Double.valueOf(emite.getInfEmisor().getPropina()));
/* 578 */         param.put("TOTAL_DESCUENTO", Double.valueOf(emite.getInfEmisor().getTotalDescuento()));
        //}
     } catch (Exception e) {
/* 581 */       e.printStackTrace();
     }
/* 583 */     return param;
   }
   
   
   private Map<String, Object> obtenerInfoFactura()
   {
/* 560 */     Map param = new HashMap();
 
/* 562 */     FacCabDocumento cabDoc = new FacCabDocumento();
     try {
/* 564 */       cabDoc = this.servicio.buscarDatosCabDocumentos(this.Ruc, this.codEst, this.codPuntEm, this.codDoc, this.secuencial);
/* 565 */       if (cabDoc != null) {
/* 566 */         param.put("RS_COMPRADOR", cabDoc.getRazonSocialComprador());
/* 567 */         param.put("RUC_COMPRADOR", cabDoc.getIdentificacionComprador());
/* 568 */         param.put("FECHA_EMISION", cabDoc.getFechaEmision());
/* 569 */         param.put("GUIA", cabDoc.getGuiaRemision());
/* 570 */         param.put("VALOR_TOTAL", Double.valueOf(cabDoc.getImporteTotal()));
/* 571 */         param.put("IVA", Double.valueOf(cabDoc.getIva12()));
//emite.getInfEmisor().getTotalSinImpuestos()
				  System.out.println("obtenerInfoFactura::getSubtotal0::"+cabDoc.getSubtotal0());
/* 572 */         param.put("IVA_0", Double.valueOf(cabDoc.getSubtotal0()));
				  System.out.println("obtenerInfoFactura::getSubtotal12::"+cabDoc.getSubtotal12());
/* 573 */         param.put("IVA_12", Double.valueOf(cabDoc.getSubtotal12()));
/* 574 */         param.put("ICE", Double.valueOf(cabDoc.getTotalvalorICE()));
/* 575 */         param.put("NO_OBJETO_IVA", Double.valueOf(cabDoc.getSubtotalNoIva()));
/* 576 */         param.put("SUBTOTAL", Double.valueOf(cabDoc.getTotalSinImpuesto()));
/* 577 */         param.put("PROPINA", Double.valueOf(cabDoc.getPropina()));
/* 578 */         param.put("TOTAL_DESCUENTO", Double.valueOf(cabDoc.getTotalDescuento()));
       }
     } catch (Exception e) {
/* 581 */       e.printStackTrace();
     }
/* 583 */     return param;
   }
   
   private Map<String, Object> obtenerInfoNC(com.cimait.invoicec.bean.Emisor emite)
   {
/* 589 */     Map param = new HashMap();
 
/* 591 */     FacCabDocumento cabDoc = new FacCabDocumento();
     try {
/* 593 */       //cabDoc = this.servicio.buscarDatosCabDocumentos(this.Ruc, this.codEst, this.codPuntEm, this.codDoc, this.secuencial);
/* 594 */       //if (cabDoc != null) {
/* 595 */         String comprobante = "";
/* 596 */         if (cabDoc.getCodDocModificado().trim().equals("01")) comprobante = "FACTURA";
/* 597 */         if (cabDoc.getCodDocModificado().trim().equals("04")) comprobante = "NOTA DE CREDITO";
/* 598 */         if (cabDoc.getCodDocModificado().trim().equals("05")) comprobante = "NOTA DE DEBITO";
/* 599 */         if (cabDoc.getCodDocModificado().trim().equals("06")) comprobante = "GUIA DE REMISION";

/* 566 */         param.put("RS_COMPRADOR", capitalizeString(emite.getInfEmisor().getRazonSocialComp()));
/* 567 */         param.put("RUC_COMPRADOR", emite.getInfEmisor().getIdentificacionComp());
/* 568 */         param.put("FECHA_EMISION", emite.getInfEmisor().getFecEmision());
/* 570 */         param.put("VALOR_TOTAL", Double.valueOf(emite.getInfEmisor().getImporteTotal()));
/* 571 */         param.put("IVA", Double.valueOf(emite.getInfEmisor().getTotalIva12()));

/* 572 */         param.put("IVA_0", Double.valueOf(emite.getInfEmisor().getTotalSinImpuestos()));
/* 573 */         param.put("IVA_12", Double.valueOf(emite.getInfEmisor().getTotalIva12()));
/* 574 */         param.put("ICE", Double.valueOf(emite.getInfEmisor().getTotalICE()));
/* 575 */         param.put("NO_OBJETO_IVA", Double.valueOf(emite.getInfEmisor().getTotalSinImpuestos()));
/* 576 */         param.put("SUBTOTAL", Double.valueOf(emite.getInfEmisor().getTotalSinImpuestos()));
/* 577 */         param.put("PROPINA", Double.valueOf(emite.getInfEmisor().getPropina()));
/* 578 */         param.put("TOTAL_DESCUENTO", Double.valueOf(emite.getInfEmisor().getTotalDescuento()));

/* 612 */         param.put("NUM_DOC_MODIFICADO", emite.getInfEmisor().getNumDocModificado());
/* 613 */         param.put("FECHA_EMISION_DOC_SUSTENTO", emite.getInfEmisor().getFecEmisionDoc());
/* 615 */         param.put("DOC_MODIFICADO", emite.getInfEmisor().getCodDocModificado());
/* 616 */         param.put("RAZON_MODIF", emite.getInfEmisor().getMotivo());
       //}
     } catch (Exception e) {
/* 619 */       e.printStackTrace();
     }
/* 621 */     return param;
   }
 
   private Map<String, Object> obtenerInfoNC()
   {
/* 589 */     Map param = new HashMap();
 
/* 591 */     FacCabDocumento cabDoc = new FacCabDocumento();
     try {
/* 593 */       cabDoc = this.servicio.buscarDatosCabDocumentos(this.Ruc, this.codEst, this.codPuntEm, this.codDoc, this.secuencial);
/* 594 */       if (cabDoc != null) {
/* 595 */         String comprobante = "";
/* 596 */         if (cabDoc.getCodDocModificado().trim().equals("01")) comprobante = "FACTURA";
/* 597 */         if (cabDoc.getCodDocModificado().trim().equals("04")) comprobante = "NOTA DE CREDITO";
/* 598 */         if (cabDoc.getCodDocModificado().trim().equals("05")) comprobante = "NOTA DE DEBITO";
/* 599 */         if (cabDoc.getCodDocModificado().trim().equals("06")) comprobante = "GUIA DE REMISION";
/* 600 */         param.put("RS_COMPRADOR", cabDoc.getRazonSocialComprador());
/* 601 */         param.put("RUC_COMPRADOR", cabDoc.getIdentificacionComprador());
/* 602 */         param.put("FECHA_EMISION", cabDoc.getFechaEmision());
/* 603 */         param.put("VALOR_TOTAL", Double.valueOf(cabDoc.getImporteTotal()));
/* 604 */         param.put("IVA", Double.valueOf(cabDoc.getIva12()));
/* 605 */         param.put("IVA_0", Double.valueOf(cabDoc.getSubtotal0()));
/* 606 */         param.put("IVA_12", Double.valueOf(cabDoc.getSubtotal12()));
/* 607 */         param.put("ICE", Double.valueOf(cabDoc.getTotalvalorICE()));
/* 608 */         param.put("NO_OBJETO_IVA", Double.valueOf(cabDoc.getSubtotalNoIva()));
/* 609 */         param.put("SUBTOTAL", Double.valueOf(cabDoc.getTotalSinImpuesto()));
/* 610 */         param.put("PROPINA", Double.valueOf(cabDoc.getPropina()));
/* 611 */         param.put("TOTAL_DESCUENTO", Double.valueOf(cabDoc.getTotalDescuento()));
/* 612 */         param.put("NUM_DOC_MODIFICADO", cabDoc.getNumDocModificado());
/* 613 */         param.put("FECHA_EMISION_DOC_SUSTENTO", cabDoc.getFecEmisionDocSustento() == null ? "" : new SimpleDateFormat("dd/MM/yyyy").format(cabDoc.getFecEmisionDocSustento()));
/* 614 */         param.put("FECHA_EMISION", cabDoc.getFechaEmision() == null ? null : new SimpleDateFormat("dd/MM/yyyy").format(cabDoc.getFechaEmision()));
/* 615 */         param.put("DOC_MODIFICADO", comprobante);
/* 616 */         param.put("RAZON_MODIF", cabDoc.getMotivoRazon());
       }
     } catch (Exception e) {
/* 619 */       e.printStackTrace();
     }
/* 621 */     return param;
   }
 
   private Map<String, Object> obtenerInfoGR()
   {
/* 626 */     Map param = new HashMap();
 
/* 628 */     FacCabDocumento cabDoc = new FacCabDocumento();
/* 629 */     List infoAdicional = new ArrayList();
/* 630 */     List detAdicional = new ArrayList();
     try {
/* 632 */       cabDoc = this.servicio.buscarDatosCabDocumentos(this.Ruc, this.codEst, this.codPuntEm, this.codDoc, this.secuencial);
/* 633 */       detAdicional = this.servicio.buscarDetAdicional(this.Ruc, this.codEst, this.codPuntEm, this.codDoc, this.secuencial);
/* 634 */       if (cabDoc != null) {
/* 635 */         param.put("RS_TRANSPORTISTA", cabDoc.getRazonSocialComprador());
/* 636 */         param.put("RUC_TRANSPORTISTA", cabDoc.getIdentificacionComprador());
/* 637 */         param.put("FECHA_EMISION", cabDoc.getFechaEmision());
/* 638 */         param.put("GUIA", cabDoc.getGuiaRemision());
/* 639 */         param.put("VALOR_TOTAL", Double.valueOf(cabDoc.getImporteTotal()));
/* 640 */         param.put("IVA", Double.valueOf(cabDoc.getIva12()));
/* 641 */         param.put("IVA_0", Double.valueOf(cabDoc.getSubtotal0()));
/* 642 */         param.put("IVA_12", Double.valueOf(cabDoc.getSubtotal12()));
/* 643 */         param.put("ICE", Double.valueOf(cabDoc.getTotalvalorICE()));
/* 644 */         param.put("NO_OBJETO_IVA", Double.valueOf(cabDoc.getSubtotalNoIva()));
/* 645 */         param.put("SUBTOTAL", Double.valueOf(cabDoc.getTotalSinImpuesto()));
/* 646 */         param.put("PROPINA", Double.valueOf(cabDoc.getPropina()));
/* 647 */         param.put("TOTAL_DESCUENTO", Double.valueOf(cabDoc.getTotalDescuento()));
/* 648 */         param.put("PLACA", cabDoc.getPlaca());
/* 649 */         param.put("PUNTO_PARTIDA", cabDoc.getPartida());
/* 650 */         param.put("FECHA_INI_TRANSPORTE", cabDoc.getFechaInicioTransporte());
/* 651 */         param.put("FECHA_FIN_TRANSPORTE", cabDoc.getFechaFinTransporte());
/* 652 */         if (!detAdicional.isEmpty()) {
/* 653 */           for (int i = 0; i < detAdicional.size(); i++) {
/* 654 */             InformacionAdicional infoAdic = new InformacionAdicional();
/* 655 */             infoAdic.setNombre(((FacDetAdicional)detAdicional.get(i)).getNombre());
/* 656 */             infoAdic.setValor(((FacDetAdicional)detAdicional.get(i)).getValor());
/* 657 */             infoAdicional.add(i, infoAdic);
           }
/* 659 */           param.put("INFO_ADICIONAL", infoAdicional);
         }
       }
     }
     catch (Exception e)
     {
/* 665 */       e.printStackTrace();
     }
/* 667 */     return param;
   }
 
   private Map<String, Object> obtenerInfoND()
   {
/* 672 */     Map param = new HashMap();
 
/* 674 */     FacCabDocumento cabDoc = new FacCabDocumento();
     try {
/* 676 */       cabDoc = this.servicio.buscarDatosCabDocumentos(this.Ruc, this.codEst, this.codPuntEm, this.codDoc, this.secuencial);
/* 677 */       if (cabDoc != null) {
/* 678 */         String comprobante = "";
/* 679 */         if (cabDoc.getCodDocModificado().trim().equals("01")) comprobante = "FACTURA";
/* 680 */         if (cabDoc.getCodDocModificado().trim().equals("04")) comprobante = "NOTA DE CREDITO";
/* 681 */         if (cabDoc.getCodDocModificado().trim().equals("05")) comprobante = "NOTA DE DEBITO";
/* 682 */         if (cabDoc.getCodDocModificado().trim().equals("06")) comprobante = "GUIA DE REMISION";
/* 683 */         param.put("RS_COMPRADOR", cabDoc.getRazonSocialComprador());
/* 684 */         param.put("RUC_COMPRADOR", cabDoc.getIdentificacionComprador());
/* 685 */         param.put("FECHA_EMISION", cabDoc.getFechaEmision());
/* 686 */         param.put("GUIA", cabDoc.getGuiaRemision());
/* 687 */         param.put("TOTAL", Double.valueOf(cabDoc.getImporteTotal()));
/* 688 */         param.put("IVA", Double.valueOf(cabDoc.getIva12()));
/* 689 */         param.put("IVA_0", Double.valueOf(cabDoc.getSubtotal0()));
/* 690 */         param.put("IVA_12", Double.valueOf(cabDoc.getSubtotal12()));
/* 691 */         param.put("ICE", Double.valueOf(cabDoc.getTotalvalorICE()));
/* 692 */         param.put("NO_OBJETO_IVA", Double.valueOf(cabDoc.getSubtotalNoIva()));
/* 693 */         param.put("SUBTOTAL", Double.valueOf(cabDoc.getTotalSinImpuesto()));
/* 694 */         param.put("PROPINA", Double.valueOf(cabDoc.getPropina()));
/* 695 */         param.put("TOTAL_SIN_IMP", Double.valueOf(cabDoc.getTotalSinImpuesto()));
/* 696 */         param.put("NUM_DOC_MODIFICADO", cabDoc.getNumDocModificado());
/* 697 */         param.put("DOC_MODIFICADO", comprobante);
/* 698 */         param.put("FECHA_EMISION_DOC_SUSTENTO", cabDoc.getFecEmisionDocSustento() == null ? "NO ENVIADO" : new SimpleDateFormat("dd/MM/yyyy").format(cabDoc.getFecEmisionDocSustento()));
       }
     } catch (Exception e) {
/* 701 */       e.printStackTrace();
     }
/* 703 */     return param;
   }
 
   private Map<String, Object> obtenerInfoCompRetencion()
   {
     Map param = new HashMap(); 
     FacCabDocumento cabDoc = new FacCabDocumento();
     try
     {
       cabDoc = this.servicio.buscarDatosCabDocumentos(this.Ruc, this.codEst, this.codPuntEm, this.codDoc, this.secuencial);
       if (cabDoc != null) {
         param.put("RS_COMPRADOR", cabDoc.getRazonSocialComprador());
         param.put("RUC_COMPRADOR", cabDoc.getIdentificacionComprador());
         param.put("FECHA_EMISION", cabDoc.getFechaEmision());
         param.put("EJERCICIO_FISCAL", cabDoc.getPeriodoFiscal());
       }
     } catch (Exception e) {
       e.printStackTrace();
     }
     return param;
   }
   private Map<String, Object> obtenerInfoCompRetencion(com.cimait.invoicec.bean.Emisor emite)
   {
	 Map param = new HashMap(); 	     	     
     param.put("RS_COMPRADOR", emite.getInfEmisor().getRazonSocialComp());
     param.put("RUC_COMPRADOR", emite.getInfEmisor().getIdentificacionComp());
     param.put("FECHA_EMISION", emite.getInfEmisor().getFecEmision());
     param.put("EJERCICIO_FISCAL", emite.getInfEmisor().getPeriodoFiscal());
	 return param;
   }
   
   public void showReport(JasperPrint jp)
   {
/* 729 */     JasperViwerSRI jv = new JasperViwerSRI(jp, Locale.getDefault());
/* 730 */     List newSaveContributors = new LinkedList();
/* 731 */     JRSaveContributor[] saveContributors = jv.getSaveContributors();
/* 732 */     for (int i = 0; i < saveContributors.length; i++) {
/* 733 */       if ((saveContributors[i] instanceof JRPdfSaveContributor)) {
/* 734 */         newSaveContributors.add(saveContributors[i]);
       }
     }
/* 737 */     jv.setSaveContributors((JRSaveContributor[])newSaveContributors.toArray(new JRSaveContributor[0]));
 
/* 739 */     JFrame jf = new JFrame();
/* 740 */     jf.setTitle("Generador de RIDE");
/* 741 */     jf.getContentPane().add(jv);
/* 742 */     jf.validate();
/* 743 */     jf.setVisible(true);
/* 744 */     jf.setSize(new Dimension(800, 650));
/* 745 */     jf.setLocation(300, 100);
/* 746 */     jf.setDefaultCloseOperation(1);
   }
 
   public String getRuc()
   {
/* 751 */     return this.Ruc;
   }
   public void setRuc(String ruc) {
/* 754 */     this.Ruc = ruc;
   }
   public String getCodEst() {
/* 757 */     return this.codEst;
   }
   public void setCodEst(String codEst) {
/* 760 */     this.codEst = codEst;
   }
   public String getCodPuntEm() {
/* 763 */     return this.codPuntEm;
   }
   public void setCodPuntEm(String codPuntEm) {
/* 766 */     this.codPuntEm = codPuntEm;
   }
   public String getCodDoc() {
/* 769 */     return this.codDoc;
   }
   public void setCodDoc(String codDoc) {
/* 772 */     this.codDoc = codDoc;
   }
   public String getSecuencial() {
/* 775 */     return this.secuencial;
   }
   public void setSecuencial(String secuencial) {
/* 778 */     this.secuencial = secuencial;
   }

public static String capitalizeString(String string) {
	  char[] chars = string.toLowerCase().toCharArray();
	  boolean found = false;
	  for (int i = 0; i < chars.length; i++) {
	    if (!found && Character.isLetter(chars[i])) {
	      chars[i] = Character.toUpperCase(chars[i]);
	      found = true;
	    } else if (Character.isWhitespace(chars[i]) || chars[i]=='.' || chars[i]=='\'') { // You can add other chars here
	      found = false;
	    }
	  }
	  return String.valueOf(chars);
	}
}