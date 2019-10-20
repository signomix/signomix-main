#
# Copyright (C) Grzegorz Skorupa 2018.
# Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
#
# This is Dockerfile template for Cricket based services
# 
# The file is used by Ant build to cereate Dockerfile.
# 1.2.3 parameter will be replaced automatically with the actual project version configured in build.xml
#
FROM gskorupa/jelastic-java10:latest

WORKDIR /usr/signomix
RUN mkdir /usr/signomix/data

#VOLUME /usr/signomix/data
#VOLUME /usr/signomix/www
#VOLUME /usr/signomix/config
VOLUME /dbdata

COPY dist/data/cricket_publickeystore.jks /usr/signomix/data
COPY dist/signomix-1.2.3.jar /usr/signomix/
COPY dist/config/cricket.json /usr/signomix/config/
COPY src/js/device-script-template.js /usr/signomix/config/
COPY src/js/payload-decoder-envelope.js /usr/signomix/config/
COPY www /usr/signomix/www

CMD ["java", "--illegal-access=deny", "--add-modules","java.xml.bind", "--add-modules", "java.activation", "-Xms50m",  "-Xmx100m", "-jar", "./signomix-1.2.3.jar", "-r", "-c", "config/cricket.json"]