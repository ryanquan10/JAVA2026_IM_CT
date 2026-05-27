@echo off
setlocal & pushd

set APP_ENTRY=org.tio.sitexxx.all.Starter

set BASE=%~dp0
set CP=%BASE%\config;%BASE%\lib\*
java -Xverify:none -Xms64m -Xmx2046m -Djava.util.Arrays.useLegacyMergeSort=true -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/java-tio-pid.hprof -cp "%CP%" %APP_ENTRY%