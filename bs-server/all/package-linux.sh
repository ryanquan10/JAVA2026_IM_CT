#!/bin/bash
echo `pwd`

cd ../parent
mvn clean install -P linux
cd ../all


mvn clean install -P linux