ECHO HI ...

cd "C:\Users\GOXR3PLUS\Documents\OpenID-Connect-Java-Spring-Server\openid-connect-server-webapp\"
DEL /F /Q /A "C:\xampp\tomcat\webapps\openid-connect-server-webapp.war"
rmdir /s /q "C:\xampp\tomcat\webapps\openid-connect-server-webapp\"
ECHO Finishing....

mvn clean package

ECHO F|xcopy "C:\Users\GOXR3PLUS\Documents\OpenID-Connect-Java-Spring-Server\openid-connect-server-webapp\target\openid-connect-server-webapp.war" "C:\xampp\tomcat\webapps\openid-connect-server-webapp.war"
@pause

