#!/bin/bash
echo 安装tio-ee
echo $(pwd) 当前盘符和路径
gid=org.t-io
basepath=$(pwd)/libs
tio_versioin=3.8.3.v20220902-RELEASE


aid=tio-core
mvn install:install-file  -Dfile=${basepath}/${aid}.jar  -DgroupId=${gid} -DartifactId=${aid} -Dversion=${tio_versioin} -Dpackaging=jar -DpomFile=${basepath}/${aid}.pom

aid=tio-flash-policy-server
mvn install:install-file  -Dfile=${basepath}/${aid}.jar  -DgroupId=${gid} -DartifactId=${aid} -Dversion=${tio_versioin} -Dpackaging=jar -DpomFile=${basepath}/${aid}.pom

aid=tio-http-common
mvn install:install-file  -Dfile=${basepath}/${aid}.jar  -DgroupId=${gid} -DartifactId=${aid} -Dversion=${tio_versioin} -Dpackaging=jar -DpomFile=${basepath}/${aid}.pom

aid=tio-http-parent
mvn install:install-file  -Dfile=${basepath}/${aid}.pom  -DgroupId=${gid} -DartifactId=${aid} -Dversion=${tio_versioin} -Dpackaging=pom 


aid=tio-http-server
mvn install:install-file  -Dfile=${basepath}/${aid}.jar  -DgroupId=${gid} -DartifactId=${aid} -Dversion=${tio_versioin} -Dpackaging=jar -DpomFile=${basepath}/${aid}.pom

aid=tio-parent
mvn install:install-file  -Dfile=${basepath}/${aid}.pom  -DgroupId=${gid} -DartifactId=${aid} -Dversion=${tio_versioin} -Dpackaging=pom 

aid=tio-utils
mvn install:install-file  -Dfile=${basepath}/${aid}.jar  -DgroupId=${gid} -DartifactId=${aid} -Dversion=${tio_versioin} -Dpackaging=jar -DpomFile=${basepath}/${aid}.pom

aid=tio-webpack-core
mvn install:install-file  -Dfile=${basepath}/${aid}.jar  -DgroupId=${gid} -DartifactId=${aid} -Dversion=${tio_versioin} -Dpackaging=jar -DpomFile=${basepath}/${aid}.pom

aid=tio-webpack-parent
mvn install:install-file  -Dfile=${basepath}/${aid}.pom  -DgroupId=${gid} -DartifactId=${aid} -Dversion=${tio_versioin} -Dpackaging=pom 


aid=tio-websocket-common
mvn install:install-file  -Dfile=${basepath}/${aid}.jar  -DgroupId=${gid} -DartifactId=${aid} -Dversion=${tio_versioin} -Dpackaging=jar -DpomFile=${basepath}/${aid}.pom

aid=tio-websocket-parent
mvn install:install-file  -Dfile=${basepath}/${aid}.pom  -DgroupId=${gid} -DartifactId=${aid} -Dversion=${tio_versioin} -Dpackaging=pom 


aid=tio-websocket-server
mvn install:install-file  -Dfile=${basepath}/${aid}.jar  -DgroupId=${gid} -DartifactId=${aid} -Dversion=${tio_versioin} -Dpackaging=jar -DpomFile=${basepath}/${aid}.pom

aid=tio-zoo-parent
mvn install:install-file  -Dfile=${basepath}/${aid}.pom  -DgroupId=${gid} -DartifactId=${aid} -Dversion=${tio_versioin} -Dpackaging=pom 
