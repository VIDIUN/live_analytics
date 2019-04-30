#!/bin/bash -
LOGSDIR=/var/log/nginx
LOGFILE=access_log.gz.1
CASSANDRA_NODE_NAME=%CASSANDRA_NODE_NAME%
UNIQLOGNAME=`hostname`-`date +%Y%m%d%H%M%S`.gz
cd $LOGSDIR
mv $LOGFILE $UNIQLOGNAME
/usr/bin/java -Dlog4j.configuration=file:/opt/vidiun/lib/log4j.properties -cp /opt/vidiun/lib/* com.vidiun.live.RegisterFile $CASSANDRA_NODE_NAME $LOGSDIR $UNIQLOGNAME