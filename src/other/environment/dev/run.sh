#!/bin/sh
#java --add-modules java.activation -jar {{distribution}}.jar -r -c config/cricket.json
#-Djavax.net.ssl.trustStore=$JAVA_HOME/jre/lib/security/cacerts -Djavax.net.ssl.trustStorePassword=changeit 
java -jar {{distribution}}.jar -r -c config/settings.json