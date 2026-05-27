@echo off
setlocal & pushd

set APP_ENTRY=org.tio.mg.all.Starter

set BASE=%~dp0
set CP=%BASE%\config;%BASE%\lib\*
java -Xverify:none -Xms64m -Xmx2046m -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/java-tio-mg-pid.hprof -cp "%CP%" %APP_ENTRY%