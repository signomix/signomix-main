#!/bin/sh

#java \
#-XX:+UseLinuxPosixThreadCPUClocks -agentpath:/usr/signomix/profiler/lib/deployed/jdk16/linux-amd64/libprofilerinterface.so=/usr/signomix/profiler/lib,5140 \
#-Xms100m -Xmx1g -cp .:signomix.jar org.cricketmsf.Runner -r -c config/settings.json

java \
-server -Djava.net.preferIPv4Stack=true -Xms100m -Xmx1g -XX:+UseParallelGC -cp .:signomix.jar org.cricketmsf.Runner -r -c config/settings.json

