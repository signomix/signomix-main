#!/bin/sh
java --add-modules java.activation -jar {{distribution}}.jar -r -c config/cricket.json
