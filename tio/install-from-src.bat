@echo off
rem %~dp0 当前盘符和路径
set target_dir=%~dp0libs
echo %target_dir%
set tio_version=3.8.3.v20220902-RELEASE


rem 安装
cd .\src\parent
call mvn clean install
cd ..\..\

rem copy
cd .\src\parent
set project_name=tio-parent
call copy .\pom.xml %target_dir%\%project_name%.pom
cd ..\..\

cd .\src\utils
set project_name=tio-utils
call copy .\target\%project_name%-%tio_version%.jar %target_dir%\%project_name%.jar
call copy .\pom.xml %target_dir%\%project_name%.pom
cd ..\..\

cd .\src\core
set project_name=tio-core
call copy .\target\%project_name%-%tio_version%.jar %target_dir%\%project_name%.jar
call copy .\pom.xml %target_dir%\%project_name%.pom
cd ..\..\



cd .\src\zoo\parent
set project_name=tio-zoo-parent
call copy .\pom.xml %target_dir%\%project_name%.pom
cd ..\..\..\


cd .\src\zoo\flash-policy-server
set project_name=tio-flash-policy-server
call copy .\target\%project_name%-%tio_version%.jar %target_dir%\%project_name%.jar
call copy .\pom.xml %target_dir%\%project_name%.pom
cd ..\..\..\



rem http
cd .\src\zoo\http\parent
set project_name=tio-http-parent
call copy .\pom.xml %target_dir%\%project_name%.pom
cd ..\..\..\..\

cd .\src\zoo\http\common
set project_name=tio-http-common
call copy .\target\%project_name%-%tio_version%.jar %target_dir%\%project_name%.jar
call copy .\pom.xml %target_dir%\%project_name%.pom
cd ..\..\..\..\

cd .\src\zoo\http\server
set project_name=tio-http-server
call copy .\target\%project_name%-%tio_version%.jar %target_dir%\%project_name%.jar
call copy .\pom.xml %target_dir%\%project_name%.pom
cd ..\..\..\..\




rem websocket
cd .\src\zoo\websocket\parent
set project_name=tio-websocket-parent
call copy .\pom.xml %target_dir%\%project_name%.pom
cd ..\..\..\..\

cd .\src\zoo\websocket\common
set project_name=tio-websocket-common
call copy .\target\%project_name%-%tio_version%.jar %target_dir%\%project_name%.jar
call copy .\pom.xml %target_dir%\%project_name%.pom
cd ..\..\..\..\

cd .\src\zoo\websocket\server
set project_name=tio-websocket-server
call copy .\target\%project_name%-%tio_version%.jar %target_dir%\%project_name%.jar
call copy .\pom.xml %target_dir%\%project_name%.pom
cd ..\..\..\..\




rem webpack
cd .\src\zoo\webpack\parent
set project_name=tio-webpack-parent
call copy .\pom.xml %target_dir%\%project_name%.pom
cd ..\..\..\..\

cd .\src\zoo\webpack\core
set project_name=tio-webpack-core
call copy .\target\%project_name%-%tio_version%.jar %target_dir%\%project_name%.jar
call copy .\pom.xml %target_dir%\%project_name%.pom
cd ..\..\..\..\

pause