#!/bin/bash
echo $(pwd) 当前盘符和路径
target_dir=$(pwd)/libs
echo ${target_dir}
tio_version=3.8.3.v20220902-RELEASE


echo 安装
cd ./src/parent
mvn clean install
cd ../../

echo copy
cd ./src/parent
project_name=tio-parent
cp ./pom.xml ${target_dir}/${project_name}.pom
cd ../../

cd ./src/utils
project_name=tio-utils
cp ./target/${project_name}-${tio_version}.jar ${target_dir}/${project_name}.jar
cp ./pom.xml ${target_dir}/${project_name}.pom
cd ../../

cd ./src/core
project_name=tio-core
cp ./target/${project_name}-${tio_version}.jar ${target_dir}/${project_name}.jar
cp ./pom.xml ${target_dir}/${project_name}.pom
cd ../../



cd ./src/zoo/parent
project_name=tio-zoo-parent
cp ./pom.xml ${target_dir}/${project_name}.pom
cd ../../../


cd ./src/zoo/flash-policy-server
project_name=tio-flash-policy-server
cp ./target/${project_name}-${tio_version}.jar ${target_dir}/${project_name}.jar
cp ./pom.xml ${target_dir}/${project_name}.pom
cd ../../../



echo http
cd ./src/zoo/http/parent
project_name=tio-http-parent
cp ./pom.xml ${target_dir}/${project_name}.pom
cd ../../../../

cd ./src/zoo/http/common
project_name=tio-http-common
cp ./target/${project_name}-${tio_version}.jar ${target_dir}/${project_name}.jar
cp ./pom.xml ${target_dir}/${project_name}.pom
cd ../../../../

cd ./src/zoo/http/server
project_name=tio-http-server
cp ./target/${project_name}-${tio_version}.jar ${target_dir}/${project_name}.jar
cp ./pom.xml ${target_dir}/${project_name}.pom
cd ../../../../




echo websocket
cd ./src/zoo/websocket/parent
project_name=tio-websocket-parent
cp ./pom.xml ${target_dir}/${project_name}.pom
cd ../../../../

cd ./src/zoo/websocket/common
project_name=tio-websocket-common
cp ./target/${project_name}-${tio_version}.jar ${target_dir}/${project_name}.jar
cp ./pom.xml ${target_dir}/${project_name}.pom
cd ../../../../

cd ./src/zoo/websocket/server
project_name=tio-websocket-server
cp ./target/${project_name}-${tio_version}.jar ${target_dir}/${project_name}.jar
cp ./pom.xml ${target_dir}/${project_name}.pom
cd ../../../../




echo webpack
cd ./src/zoo/webpack/parent
project_name=tio-webpack-parent
cp ./pom.xml ${target_dir}/${project_name}.pom
cd ../../../../

cd ./src/zoo/webpack/core
project_name=tio-webpack-core
cp ./target/${project_name}-${tio_version}.jar ${target_dir}/${project_name}.jar
cp ./pom.xml ${target_dir}/${project_name}.pom
cd ../../../../
