package com.sun.directory.examples;


import com.thoughtworks.xstream.XStream;
import ec.gob.sri.comprobantes.util.xml.XStreamUtil;
import ec.gob.sri.comprobantes.ws.aut.Autorizacion;
import ec.gob.sri.comprobantes.ws.aut.AutorizacionComprobantes;
import ec.gob.sri.comprobantes.ws.aut.AutorizacionComprobantesService;
import ec.gob.sri.comprobantes.ws.aut.Mensaje;
import ec.gob.sri.comprobantes.ws.aut.RespuestaComprobante;
import ec.gob.sri.comprobantes.ws.aut.RespuestaLote;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.net.URLConnection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.namespace.QName;

public class AutorizacionComprobantesWs
{
  private AutorizacionComprobantesService service;
  public static final String ESTADO_AUTORIZADO = "AUTORIZADO";
  public static final String ESTADO_NO_AUTORIZADO = "NO AUTORIZADO";
  public static String xmlAutorizacionSri = ""; 

  public AutorizacionComprobantesWs(String wsdlLocation)
  {
    try
    {
    	URL url = new URL(wsdlLocation);
    	URLConnection con = url.openConnection();
        con.setConnectTimeout(30000);
        con.setReadTimeout(30000);
      this.service = new AutorizacionComprobantesService(con.getURL(), new QName("http://ec.gob.sri.ws.autorizacion", "AutorizacionComprobantesService"));
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  public RespuestaComprobante llamadaWSAutorizacionInd(String claveDeAcceso)
  {
    RespuestaComprobante response = null;
    try
    {
      AutorizacionComprobantes port = this.service.getAutorizacionComprobantesPort();
      response = port.autorizacionComprobante(claveDeAcceso);      
    }
    catch (Exception e) {
      e.printStackTrace();
      return response;
    }

    return response;
  }

  public RespuestaLote llamadaWsAutorizacionLote(String claveDeAcceso)
  {
    RespuestaLote response = null;
    try {
      AutorizacionComprobantes port = this.service.getAutorizacionComprobantesPort();
      response = port.autorizacionComprobanteLote(claveDeAcceso);
    }
    catch (Exception e) {
      Logger.getLogger(AutorizacionComprobantesWs.class.getName()).log(Level.SEVERE, null, e);
      return response;
    }
    return response;
  }

  public static String autorizarComprobanteIndividual(String claveDeAcceso, String nombreArchivo, String tipoAmbiente, String dirAutorizados, String dirNoAutorizados, String dirFirmados)
  {
    StringBuilder mensaje = new StringBuilder();
    try {
      RespuestaComprobante respuesta = null;

      for (int i = 0; i < 20; i++) {
    	System.out.println("Intento:"+i);
        respuesta = new AutorizacionComprobantesWs(com.sun.comprobantes.util.FormGenerales.devuelveUrlWs(tipoAmbiente, "AutorizacionComprobantes")).llamadaWSAutorizacionInd(claveDeAcceso);

        if (!respuesta.getAutorizaciones().getAutorizacion().isEmpty()) {
          break;
        }
        Thread.currentThread(); Thread.sleep(3000);
      }
      int i;
      if (respuesta != null) {
        i = 0;
        if(respuesta.getAutorizaciones().getAutorizacion().size()>0){
        	System.out.println("respuesta::"+respuesta.getNumeroComprobantes()+"::Estado::"+respuesta.getAutorizaciones().getAutorizacion().get(0).getEstado());
        }
        for (Autorizacion item : respuesta.getAutorizaciones().getAutorizacion()) {
          mensaje.append(item.getEstado());

          item.setComprobante("<![CDATA[" + item.getComprobante() + "]]>");

          XStream xstream = XStreamUtil.getRespuestaXStream();
          Writer writer = null;
          ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
          writer = new OutputStreamWriter(outputStream, "UTF-8");
          writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
          xstream.toXML(item, writer);
          //xmlAutorizacionSri = xstream.
          String xmlAutorizacion = outputStream.toString("UTF-8");
          if ((i == 0) && (item.getEstado().equals("AUTORIZADO"))) {
            ArchivoUtils.stringToArchivo(dirAutorizados + nombreArchivo + ".xml", xmlAutorizacion);
            //item.getNumeroAutorizacion();
            //item.getFechaAutorizacion();            
            mensaje.append("|" +item.getNumeroAutorizacion()+"|"+item.getFechaAutorizacion()+"|");
            //System.out.println("Xml"+xmlAutorizacion);
            //VisualizacionRideUtil.decodeArchivoBase64(dirAutorizados + File.separator + nombreArchivo, item.getNumeroAutorizacion(), item.getFechaAutorizacion().toString());
            break;
          }
          if (item.getEstado().equals("NO AUTORIZADO")) {
        	  //ERROR                        
            if (verificarOCSP(item)){            	
            	mensaje.append("SIN-CERTIFICADORA|" + "No se puede validar el certificado digital.|" +obtieneMensajesAutorizacion(item) );
            	System.out.println("No se puede validar el certificado digital.");
            }else{
            	ArchivoUtils.stringToArchivo(dirNoAutorizados + nombreArchivo + ".xml" , xmlAutorizacion);
                mensaje.append("NO AUTORIZADO|" + obtieneMensajesAutorizacion(item));                
            }
            break;
          }
          i++;
        }
      }

      if ((respuesta == null) || (respuesta.getAutorizaciones().getAutorizacion().isEmpty() == true)) {
        mensaje.append("TRANSMITIDO SIN RESPUESTA|Ha ocurrido un error en el proceso de la Autorización, por lo que se traslado el archivo a la carpeta de: transmitidosSinRespuesta");

        //String dirFirmados = new ConfiguracionDirectorioSQL().obtenerDirectorio(DirectorioEnum.FIRMADOS.getCode()).getPath();
        String dirTransmitidos = dirFirmados + File.separator + "transmitidosSinRespuesta";

        File transmitidos = new File(dirTransmitidos);
        if (!transmitidos.exists()) {
          new File(dirTransmitidos).mkdir();
        }

        File archivoFirmado = new File(new File(dirFirmados), nombreArchivo + ".xml");
        if (!ArchivoUtils.copiarArchivo(archivoFirmado, transmitidos.getPath() + File.separator + nombreArchivo + ".xml"))
          mensaje.append("\nError al mover archivo a carpeta de Transmitidos sin Respuesta");
        else
          archivoFirmado.delete();
      }
    }
    catch (Exception ex)
    {
      Logger.getLogger(AutorizacionComprobantesWs.class.getName()).log(Level.SEVERE, null, ex);
    }
    return mensaje.toString();
  }

  

  public static String obtieneMensajesAutorizacion(Autorizacion autorizacion)
  {
    StringBuilder mensaje = new StringBuilder();
    for (Mensaje m : autorizacion.getMensajes().getMensaje()) {
      if (m.getInformacionAdicional() != null)
        mensaje.append("\n" + m.getMensaje() + ": " + m.getInformacionAdicional());
      else {
        mensaje.append("\n" + m.getMensaje());
      }
    }

    return mensaje.toString();
  }

  public static boolean verificarOCSP(Autorizacion autorizacion)
    throws Exception
  {
    boolean respuesta = false;

    for (Mensaje m : autorizacion.getMensajes().getMensaje()) {
      if (m.getIdentificador().equals("61")) {    	
        respuesta = true;
      }
    }
    return respuesta;
  }
}