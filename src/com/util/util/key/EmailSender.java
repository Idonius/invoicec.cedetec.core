/**
 * 
 */
package com.util.util.key;

//import java.sql.Connection;
import java.util.Date;
import java.util.Properties;

import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import org.apache.log4j.Logger;

/**
 * @author Johnny Zurita Medranda
 *05/03/2012
 */
public class EmailSender {
	private String from = null;
	private String host = null;
	private Logger log;
	//private BitacoraScp scp;
	//private Connection axis;
	private int firstmail;
	public EmailSender (String host, String from) {
		this.host = host;
		this.from = from;
	}
	
	public EmailSender (Logger log) {
		this.log=log;
	}
	
	
	public String send (String to, String subject, String message) {
		String result = "Enviado";
		try {
			Properties prop = System.getProperties();
			prop.put("mail.smtp.host",host);
			Session sesion = Session.getDefaultInstance(prop,null);
			MimeMessage msg = new MimeMessage(sesion);
			msg.setFrom(new InternetAddress(from));
			String[] receivers = to.split(";");
			for (int i=0; i < receivers.length; i++){
				msg.addRecipient(Message.RecipientType.TO, new InternetAddress(receivers[i]));
			}
			msg.setSubject(subject);
			BodyPart messageBodyPart = new MimeBodyPart();
			messageBodyPart.setText(message);
			Multipart multipart = new MimeMultipart();
			multipart.addBodyPart(messageBodyPart);
			msg.setContent(multipart);
			Transport.send(msg);
			
		} catch (javax.mail.MessagingException me) {
			result = me.toString();
		}
		return result;
	}
	
	/**
	 * Funcion que envia  mensaje de correo  
	 * */
	public String Envia_Mensaje(String cuerpo){
		String result=""; 
		if (Util.enablemail.equals("Y")) {
			EmailSender e = new EmailSender(Util.host, Util.from);
		    result = e.send(Util.list_email, Util.subject, cuerpo+Util.pieMensaje);
		    if(result.equals("Enviado"))
		    {
		    	System.out.println("Estado del correo: "+result);
		    }else{
		    	System.err.println("No se procedio a enviar el correo "+result);
		    }
		    
		    
	     }
	
	
	return result;
	}
	
	
	/**
	 * Funcion que permite enviar mensaje cada cierto tiempo
	 * configurado en el xml
	 * */
	public Date Verifica_Tiempo_Mensaje(String cuerpo,Date ld_fecha,int firstmail){
		String result="";
		double fecha_i;
		int li_time_minutes_alarm = 0;
		String mensaje="";
		try{
			li_time_minutes_alarm = Util.time_mail;
		}catch(Exception e){
			li_time_minutes_alarm = 10; //Valor por default
		}
		
		fecha_i=Util.calcTimeMin(ld_fecha,new Date());
		if (fecha_i>li_time_minutes_alarm || firstmail==1){
        	try{        		
                //safirCli.sendAlarm("DESGSM: **ERROR DE CONEXION ", "Supero el numero de reintentos de conexion ");
                EmailSender e = new EmailSender(Util.host, Util.from);
                //mensaje="**Error de Conexion a B.D. Supero el numero de reintentos "+Util.reintentos_base+" de conexion ";
                result=e.Envia_Mensaje(cuerpo);
                if(result.equals("Enviado")){
                	log.info("**Se procedio a enviar el correo a los siguientes destinatarios: "+Util.list_email);
                	ld_fecha = new Date();
                	//firstmail=0;
                	setFirstmail(0);
                }else{
                	log.info("**No se puede enviar correos a los destinatarios: "+Util.list_email);
                }
                //ld_fecha = new Date();
        	}catch(Exception e){
                System.err.println("microcontainer..GenericTransaction::**Error de conexion-->"+e.getMessage());
                log.error("microcontainer..GenericTransaction::**Error de conexion-->"+e.getMessage());
        	} 
    	}else{
    		//log.warn(new StringBuffer().append("**El tiempo  minimo de envio entre correo no ha sido superado"));
    		System.out.println("**El tiempo  minimo de envio entre correo no ha sido superado");
    	}
	
	
	return ld_fecha;
	}

	public int getFirstmail() {
		return firstmail;
	}

	public void setFirstmail(int firstmail) {
		this.firstmail = firstmail;
	}
	
}

