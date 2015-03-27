package com.cimait.invoicec.sri.xades;

import es.mityc.firmaJava.libreria.xades.DatosFirma;
import es.mityc.firmaJava.libreria.xades.ResultadoValidacion;
import es.mityc.firmaJava.libreria.xades.ValidarFirmaXML;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class ValidacionBasica
{
  private static final String ARCHIVO_XADES_VALIDO = "/repositorio/factura-XAdES-BES.xml";

  public static void main(String[] args)
  {
    ValidacionBasica validador = new ValidacionBasica();

    if (validador.validarFichero(ValidacionBasica.class.getResourceAsStream("/repositorio/factura-XAdES-BES.xml")))
      System.out.println("archivo valido");
  }

  public boolean validarArchivo(File archivo)
  {
    ValidacionBasica validador = new ValidacionBasica();
    boolean esValido = false;
    try
    {
      InputStream is = new FileInputStream(archivo);
      esValido = validador.validarFichero(is);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    return esValido;
  }

  public boolean validarFichero(InputStream archivo)
  {
    boolean esValido = true;

    ArrayList results = null;

    Document doc = parseaDoc(archivo);

    if (doc != null)
    {
      try
      {
        ValidarFirmaXML vXml = new ValidarFirmaXML();
        results = vXml.validar(doc, "./", null);
      } catch (Exception e) {
        e.printStackTrace();;
      }

      ResultadoValidacion result = null;
      Iterator it = results.iterator();
      while (it.hasNext()) {
        result = (ResultadoValidacion)it.next();
        esValido = result.isValidate();

        if (esValido) {
          System.out.println("La firma es valida = " + result.getNivelValido() + "\nFirmado el: " + result.getDatosFirma().getFechaFirma()); continue;
        }

        System.out.println("La firma NO es valida\n" + result.getLog());
      }

    }

    return esValido;
  }

  private Document parseaDoc(InputStream fichero)
  {
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    dbf.setNamespaceAware(true);

    DocumentBuilder db = null;
    try {
      db = dbf.newDocumentBuilder();
    } catch (ParserConfigurationException ex) {
      System.out.println("Error interno al parsear la firma" + ex.getMessage());
      return null;
    }

    Document doc = null;
    try {
      doc = db.parse(fichero);
      Document localDocument1 = doc;
      return localDocument1;
    }
    catch (SAXException ex)
    {
      doc = null;
    } catch (IOException ex) {
      System.out.println("Error interno al validar firma" + ex.getMessage());
    } finally {
      dbf = null;
      db = null;
    }

    return null;
  }
}