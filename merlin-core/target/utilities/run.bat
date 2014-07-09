@echo off
	REM ###############################################################################################
	REM # 
	REM # run.bat - merlin version 0.0.1-SNAPSHOT for Win x86
	REM # 
	REM # CEB - Centre of Biological Engineering
	REM # University of Minho
	REM #
	REM # Created inside the BioSystem Research Group (http://sysbio.uminho.pt)
	REM # University of Minho
	REM #
	REM # Copyright (c) 2014.
	REM #
	REM # http://www.merlin-sysbio.org
	REM #
	REM ###############################################################################################
	
	SET APP_NAME="merlin - MEtabolic models Reconstruction using genome-scaLe INformation v0.0.1-SNAPSHOT"
	
	SET MERLIN_HOME=%~d0%~sp0../
	
	REM cd %merlin_HOME%
	SET %MERLIN_HOME%lib;path=%path%
	
	REM SET path=%path%;%merlin_HOME%lib;%merlin_HOME%libwin32
	
	SET CLASSPATH="./lib\*;"
	REM .\libwin32\*"
	SET JAR_ARGS=%MERLIN_HOME%"plugins_bin"
	
	SET MAIN_CLASS="es.uvigo.ei.aibench.Launcher"
	
	SET JAVA="%merlin_HOME%jre7\bin\java"
	
	SET LPATH="-Djava.library.path=%merlin_HOME%lib";%merlin_HOME%
	SET LAF="-Dswing.defaultlaf=com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel"
	SET	LEGACY="-Djava.util.Arrays.useLegacyMergeSort=true"
	SET LOCALE="-Duser.language=en"
	SET CHARSET="-Dfile.encoding=utf-8"
	SET MAXMEM="-Xmx1024M"
	SET MINMEM="-Xms512m"
	
	SET COMMAND=%JAVA% %LAF% %LEGACY% %LPATH% %LOCALE% %CHARSET% %MAXMEM% %MINMEM% -cp %CLASSPATH% %MAIN_CLASS% %JAR_ARGS%
	
	%COMMAND%