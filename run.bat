@echo off
setlocal enabledelayedexpansion

set BASEDIR=%~dp0
set JAVA_BIN=%BASEDIR%java\jdk-24\OpenJDK24U-jdk_x64_windows_hotspot_24.0.1_9\jdk-24.0.1+9\bin\java.exe
set JAVAFX_LIB=%BASEDIR%java\javafx\javafx-sdk-24.0.1\lib
set CLASSPATH=%BASEDIR%out\production\MusicSoftware2
set CLASSPATH=%CLASSPATH%;%BASEDIR%java\mysql-connector-j-9.3.0\mysql-connector-j-9.3.0\mysql-connector-j-9.3.0.jar
set CLASSPATH=%CLASSPATH%;%BASEDIR%java\jbcrypt-0.4.jar

REM Debug per conferma:
echo Verifica classpath:
dir /s /b "%CLASSPATH%\com\dashapp\Main.class"
echo.

"%JAVA_BIN%" ^
  --module-path "%JAVAFX_LIB%" ^
  --add-modules javafx.controls,javafx.fxml ^
  --enable-native-access=ALL-UNNAMED ^
  -cp "%CLASSPATH%" ^
  com.dashapp.Main

pause
