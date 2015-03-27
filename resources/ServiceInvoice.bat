@echo off
rem ServiceInvoice.bat
if "%1" == "stop" goto stopservice
if "%1" == "start" goto startservice
if "%1" == "" goto noparms

:startservice
echo "Iniciando Servicio"
>nul find "N" facturacion.ctr && (
	if "%JAVA_HOME" == "" goto NOJVM
	if "%2" == "" goto NORUC
	echo S > facturacion.ctr
	%JAVA_HOME%/bin/java -Xmx1024m -cp "./*" com.cimait.invoicec.core.ServiceData %2
	goto end
) || (
	echo "Servicio iniciado con anterioridad"
)
goto end

:stopservice
echo "Finalizando Servicio"
>nul find "S" facturacion.ctr && (
	echo N > facturacion.ctr
) || (
	echo "Servicio detenido con anterioridad"
)

goto end

:noparms
echo "Para iniciar servicio : ServiceInvoice.bat start <RUC>"
echo "Para finalizar servicio : ServiceInvoice.bat stop"
goto end

:NOJVM
echo "Variable de ambiente JAVA_HOME no configurada"
goto end

:NORUC
echo "no ha proveido RUC emisor a usar en servicio"
echo "ServiINvoice.bat start <RUC>"
goto end

:end