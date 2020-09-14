#
# Copyright (C) Grzegorz Skorupa 2018.
# Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
#
# This is Dockerfile template for Cricket based services
# 
# The file is used by Ant build to cereate Dockerfile.
# 1.2.4 parameter will be replaced automatically with the actual project version configured in build.xml
#

// THIS DOCKERFILE IS OUTDATED

FROM azul/zulu-openjdk-alpine:13.0.1

WORKDIR /usr/signomix
RUN mkdir /usr/signomix/data
RUN mkdir /usr/signomix/log

#VOLUME /usr/signomix/data
#VOLUME /usr/signomix/www
#VOLUME /usr/signomix/config
VOLUME /dbdata

COPY dist/data/cricket_publickeystore.jks /usr/signomix/data
COPY dist/signomix-ce-1.2.4.jar /usr/signomix/
COPY dist/config/cricket.json /usr/signomix/config/
COPY src/js/device-script-template.js /usr/signomix/config/
COPY src/js/payload-decoder-envelope.js /usr/signomix/config/
COPY www /usr/signomix/www

CMD ["java", "-Xms50m", "-Xmx100m", "-jar", "signomix-ce-1.2.4.jar", "-r", "-c", "config/cricket.json"]
