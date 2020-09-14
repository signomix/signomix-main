#!/bin/sh
# 
# To run Signomiks with your extension, comment out the last line and uncomment the penultimate line. 
# Change extension.jar to the name of your library.
#

#java -cp signomix.jar:ext-lib/extension.jar org.cricketmsf.Runner -r -c config/settings.json
java -jar signomix.jar -r -c config/settings.json