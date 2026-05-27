cd ..\parent\
call mvn clean install -P linux
cd ..\all

call mvn clean install -P linux

pause