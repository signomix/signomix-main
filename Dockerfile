#
# Copyright (C) Grzegorz Skorupa 2018.
# Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
#
# This is Dockerfile template for Cricket based services
# 
# The file is used by Ant build to cereate Dockerfile.
# 1.2.4 parameter will be replaced automatically with the actual project version configured in build.xml
#

FROM azul/zulu-openjdk-alpine:13
#FROM azul/zulu-openjdk:13

WORKDIR /usr/signomix

RUN mkdir /usr/signomix/dbdata
RUN mkdir /usr/signomix/config
RUN mkdir /usr/signomix/dbdata/db
RUN mkdir /usr/signomix/dbdata/logs
RUN mkdir /usr/signomix/dbdata/files
RUN mkdir /usr/signomix/dbdata/assets
RUN mkdir /usr/signomix/dbdata/backup

COPY target/signomix.jar /usr/signomix/
COPY src/main/resources/settings.json /usr/signomix/config/
COPY src/main/resources/logback.xml /usr/signomix/
COPY src/main/resources/device-script-template.js /usr/signomix/config/
COPY src/main/resources/payload-decoder-envelope.js /usr/signomix/config/
COPY src/main/www /usr/signomix/www
COPY src/main/resources/run.sh /usr/signomix/
RUN chmod +x /usr/signomix/run.sh

#COPY src/main/resources/profiler-server-linuxamd64.zip /usr/signomix/
#RUN apt-get update -y
#RUN apt-get install -y unzip
#RUN unzip /usr/signomix/profiler-server-linuxamd64.zip -d profiler

VOLUME /usr/signomix/dbdata

#CMD ["java", "-Xms100m",  "-Xmx1g", "-cp", ".:signomix.jar", "org.cricketmsf.Runner", "-r", "-c", "config/settings.json"]
#CMD ["java", "-Xms100m",  "-Xmx1g", "-jar", "./signomix.jar", "-r", "-c", "config/settings.json"]

CMD ./run.sh
