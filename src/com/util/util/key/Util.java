/**
 * 
 */
package com.util.util.key;

import java.util.Date;

public class Util {
	public static final String FORMATO_FECHA_DB = "yyyy-MM-dd";
	public static final int EDITAR = 1;
	
	//#######- Nombres de Arhivos Logs, Crtl -#######
	public static final String log_control=Environment.c.getString("facElectronica.log.control");
	public static final String file_control=Environment.c.getString("facElectronica.ctrl-on-off.file");
	public static final String name_proyect="FACT_ELECTRONICA";
	
	//#######- Parametros de Conexion Database -#######
	public static final String driverConection=Environment.c.getString("facElectronica.database.driver");
	public static final String urlConection=Environment.c.getString("facElectronica.database.facturacion.url");
	public static final String userConection=Environment.c.getString("facElectronica.database.facturacion.user");
	public static final String passwordConection=Environment.c.getString("facElectronica.database.facturacion.password");
	
	//#######- Parametros de Mail-#######
	public static final String host=Environment.c.getString("facElectronica.alarm.email.host");
	public static final String from=Environment.c.getString("facElectronica.alarm.email.sender");
	public static final String list_email=Environment.c.getString("facElectronica.alarm.email.receivers-list");
	public static final String subject=Environment.c.getString("facElectronica.alarm.email.subject");
	public static final String pieMensaje=Environment.c.getString("facElectronica.alarm.email.final-message");
	public static final String enablemail= Environment.c.getString("facElectronica.alarm.email.enable");
	public static final int time_mail= Environment.c.getInt("facElectronica.alarm.email.time-mail");
	
	public static final int timeWait= Environment.c.getInt("facElectronica.general.time-wait");
	
	public static final String keystore_type = Environment.c.getString("facElectronica.security.keystore_type");
	public static final String keystore_file = Environment.c.getString("facElectronica.security.keystore_file");
	public static final String keystore_password = Environment.c.getString("facElectronica.security.keystore_password");
	public static final String private_key_password = Environment.c.getString("facElectronica.security.private_key_password");
	public static final String private_key_alias = Environment.c.getString("facElectronica.security.private_key_alias");
	
	public static final int min_pool =Environment.c.getInt("facElectronica.database.facturacion.min_pool");
	public static final int max_pool =Environment.c.getInt("facElectronica.database.facturacion.max_pool");
	
	public static final String ws_service1_name = Environment.c.getString("facElectronica.WS.service1.service_name");
	public static final String ws_service1_endpoint = Environment.c.getString("facElectronica.WS.service1.endpoint");
	public static final String ws_service1_qname = Environment.c.getString("facElectronica.WS.service1.qname");
	public static final String ws_service1_user = Environment.c.getString("facElectronica.WS.service1.user");
	public static final String ws_service1_password = Environment.c.getString("facElectronica.WS.service1.password");
	
	
	/*
	//#######- Niveles de Error - SCP -#######
	//NIVEL_ERROR - 0: Informativo, 1: Warning 2: Error leve 3: Error Critico
	public static final int nivel_error_inf=0;//NIVEL_ERROR - 0: Informativo, 
	public static final int nivel_error_war=1;//NIVEL_ERROR - 1: Warning 
	public static final int nivel_error_lev=2;//NIVEL_ERROR - 2: Error leve
	public static final int nivel_error_cri=3;//NIVEL_ERROR - 3: Error Critico
	
	//#######- Cabecera SCP -#######
	
	public static final String ls_unidad_registro_scp = "DESPACHADOR_COMANDOS";//Unidad de registro por ejemplo CDRS, VOUCHERS, SERVICIOS, CUENTAS. El programador debe especificar que es lo que se esta procesando. - scp_bitacora_procesos
	public static final double li_total_registros_scp = 0;					   //Registros totales a ser procesados. - scp_bitacora_procesos
	public static final String ls_referencia_scp = "desGSM.jar";		   	   //Nombre de programa invocado, sirve como ayuda al operador para identificar el programa responsable del proceso. - scp_bitacora_procesos
	//public static final String ls_error_scp = null; 						   //error del procedimiento del scp - scp_bitacora_procesos_ins
	public static final String ls_id_proceso_scp =Environment.c.getString("desGSM.oracle.scp.id_proceso_scp"); //"DESPACHADOR DESGSM";	   //Identificador del proceso. - scp_procesos - Configurado en el globito
	//#######- SCP -  Paquetes y procedimientos #######
	public static final String procesos_ins=Environment.c.getString("desGSM.oracle.scp.procesos_ins");
	public static final String detalles_bitacora_ins=Environment.c.getString("desGSM.oracle.scp.detalles_bitacora_ins");
	public static final String procesos_act=Environment.c.getString("desGSM.oracle.scp.procesos_act");
	public static final String bitacora_procesos_fin=Environment.c.getString("desGSM.oracle.scp.bitacora_procesos_fin");
	
	//***********************************************************************
	
	//Estados
	public static final String status_reproceso=Environment.c.getString("desGSM.oracle.axis.request.ds_servicios_cab.estado_registro.reproceso");
	public static final String status_procesado=Environment.c.getString("desGSM.oracle.axis.request.ds_servicios_cab.estado_registro.procesado");
	public static final String status_tomado=Environment.c.getString("desGSM.oracle.axis.request.ds_servicios_cab.estado_registro.tomado");
	public static final int status_procesado_resp=Environment.c.getInt("desGSM.oracle.axis.request.ds_sas_response.estado.procesado");
	public static final int status_respo_error=Environment.c.getInt("desGSM.oracle.axis.request.ds_sas_response.estado.error");
	public static final int status_det_error=Environment.c.getInt("desGSM.oracle.axis.request.ds_servicios_det.estado_respuesta.error");
	public static final int status_cab_inicial=Environment.c.getInt("desGSM.oracle.axis.request.ds_servicios_cab.estado_registro.inicial");
	
	
	//#######- Hilos -#######
	public static final String query=Environment.c.getString("desGSM.oracle.axis.request.ds_servicios_cab.query-principal");
	public static final int max_row=Environment.c.getInt("desGSM.oracle.axis.request.max-row");
	public static final int hilos_ejecucion=Environment.c.getInt("desGSM.oracle.axis.request.hilos.cantidad_ejecucion");
	
	
	public static final String QUEUE_ID="desGSM.oracle.axis.request.hilos.thread.QUEUE_ID";
	public static final String TYPE_THREAD="desGSM.oracle.axis.request.hilos.thread.type-thread";
	public static String QUEUE_IH="";
	public static String TYPE_THREADH="";
		
	public static final int waiting_time=Environment.c.getInt("desGSM.oracle.axis.request.hilos.sleep");
	

	
	//#######- Sockets -#######
	public static final String send_hlr=Environment.c.getString("desGSM.hlr.send-hlr");
	public static final String delete_cola=Environment.c.getString("desGSM.hlr.delete-cola");
	public static final String ls_safirPtoRes = Environment.c.getString("desGSM.hlr.SAFIR_PTO_REQ");
	public static final String ip=Environment.c.getString("desGSM.hlr.SAFIR_IP");
	public static final int port=Environment.c.getInt("desGSM.hlr.puerto_socket");
	public static final String ls_usrSafir = Environment.c.getString("desGSM.hlr.USR_SAFIR");
	public static final int reintentos_socket = Environment.c.getInt("desGSM.hlr.reintentos_socket");
	
	
	//#######- Nombres de Paquetes -#######
	public static final String arma_comando=Environment.c.getString("desGSM.oracle.axis.pck.arma_comando");
	
	
	
	//#######- Reintentos para reproceso -#######
	public static final int max_reint=Environment.c.getInt("desGSM.oracle.axis.request.ds_servicios_cab.reintentos.numero");
	public static final int reintentos_base=Environment.c.getInt("desGSM.oracle.axis.reintentos_base");
	
	
	//#######- Comandos -#######
	public static final String logout=Environment.c.getString("desGSM.hlr.ACK_RESP_LOGOUT");
	public static final String comando_login=Environment.c.getString("desGSM.hlr.trama-login");
	
	
	
	
	
	
	//#######- CRUD DESGSM-#######
	public static final String update_response=Environment.c.getString("desGSM.oracle.axis.request.ds_sas_response.update-status");
	public static final String update_dsServiciosCab=Environment.c.getString("desGSM.oracle.axis.request.ds_servicios_cab.update-status");
	public static final String update_ReintentosdsServiciosCab=Environment.c.getString("desGSM.oracle.axis.request.ds_servicios_cab.reintentos.update");
	public static final String consulta_reintentos=Environment.c.getString("desGSM.oracle.axis.request.ds_servicios_cab.reintentos.consulta_reintentos");
	public static final String update_reproc_response=Environment.c.getString("desGSM.oracle.axis.request.ds_sas_response.update-reproc-response");
	public static final String estado_reproc_response=Environment.c.getString("desGSM.oracle.axis.request.ds_sas_response.estado.error");
	*/
	
	
	//#######- Calculo de tiempo entre dos fechas -#######
	public static double calcTimeMin(Date ld_fechaInicial, Date ld_fechaFinal){
		 try{
				  long fechaIni = ld_fechaInicial.getTime();
				  long fechaFin = ld_fechaFinal.getTime();  
				  double minutes = fechaFin - fechaIni;	 
				  return (minutes/(1000*60)); 
		 }catch(Exception e){
			 e.printStackTrace();
			 return -1;
		 }
	 }
	
	/*
	
	
	
	//#######- Funcion que permite relacionar el queid y el type  -#######
	public static void Asigna_queid_cola(int hilo){
		String que="";
		String type="";
		que=QUEUE_ID+hilo;
		type=TYPE_THREAD+hilo;
		QUEUE_IH=Environment.c.getString(que);
		TYPE_THREADH=Environment.c.getString(type);
		 
	 }
	
	//#######- DSK_SAFIR -  Paquetes y procedimientos #######
	public static final String fail_comand=Environment.c.getString("desGSM.oracle.dsk_safir.funciones.send_failcomandossafir");	
	public static final String consulta_DserviciosBloqueo=Environment.c.getString("desGSM.oracle.dsk_safir.funciones.consulta_dsServiciosBloqueo");
	*/
}

