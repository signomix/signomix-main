#!/bin/sh
#
# 
 
# To run Signomiks with your extension, comment out the last line and uncomment the penultimate line. 
# Change extension.jar to the name of your library.
#

#java -Xdebug -agentlib:jdwp=transport=dt_socket,address=9876,server=y,suspend=n -cp signomix.jar:ext-lib/extension.jar org.cricketmsf.Runner -r -c config/settings.json
java -Xdebug -agentlib:jdwp=transport=dt_socket,address=9876,server=y,suspend=n -jar signomix.jar -r -c config/settings.json