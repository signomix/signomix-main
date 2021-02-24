#!/bin/sh
#
# To run Signomiks with your extension, comment out the last line and uncomment the penultimate line. 
# Change extension.jar to the name of your library.
#
#java -cp signomix.jar:ext-lib/extension.jar org.cricketmsf.Runner -r -c config/settings.json

#
# environment variables used by the service
#
export SIGNOMIX_INITIAL_ADMIN_SECRET=test123
export SIGNOMIX_INITIAL_ADMIN_EMAIL=admin@myhost
export SIGNOMIX_ADMIN_NOTIFICATION_EMAIL=admin@myhost
export SIGNOMIX_URL=http://localhost:8080
export SIGNOMIX_TITLE="Signomix DEV"
export SIGNOMIX_SMTP_FROM=signomix@myhost
export SIGNOMIX_SMTP_HOST=smtp.myhost
export SIGNOMIX_SMTP_USER=signomix@myhost
export SIGNOMIX_SMTP_PASSWORD=mypassword
export SIGNOMIX_PUSHOVER_TOKEN=myPushoverToken
export SIGNOMIX_TELEGRAM_TOKEN=myTelegramToken
export SIGNOMIX_SMS_LOGIN=smsLogin
export SIGNOMIX_SMS_PASSWORD=smsPassword
export SIGNOMIX_EMAIL_FROM=signomix@myhost
export SIGNOMIX_EMAIL_HOST=smtp.myhost
export SIGNOMIX_EMAIL_USER=signomix@myhost
export SIGNOMIX_EMAIL_PASSWORD=mypassword

java -Xdebug -agentlib:jdwp=transport=dt_socket,address=9876,server=y,suspend=n -jar signomix.jar -r -c config/settings.json