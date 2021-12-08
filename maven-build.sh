#!/bin/bash
mvn clean install -Dconfig.location=/etc/mitreid \
                  -Dlog.to=FILE \
                  -Dlog.file=/usr/local/tomcat/logs/mitreid \
                  -Dlog.level=trace
