#!/bin/sh
echo `pwd`


cd ../parent
mvn clean install -P windows
cd ../all

call mvn clean install -P windows
