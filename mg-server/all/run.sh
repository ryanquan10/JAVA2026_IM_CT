APP_ENTRY=org.tio.mg.all.Starter
BASE=$(cd `dirname $0`; pwd)
echo BASE:${BASE}
CP=${BASE}/config:${BASE}/lib/*
java -Xverify:none -Xms64m -Xmx2046m -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/java-tio-mg-pid.hprof -cp ${CP} ${APP_ENTRY} &
