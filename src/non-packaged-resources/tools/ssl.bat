@echo off

set /p DNS=Enter DNS: 
set /p IP=Enter IP: 
set /p PASSWORD=Enter password: 

if exist server-root.jks if exist server-root.pem if exist server-ca.jks if exist server-ca.pem goto part2

echo  ====================================================================
echo  Creating server-root.jks, server-root.pem, server-ca.jks and server-ca.pem ...
echo  ====================================================================

del server-root.jks 2>nul
del server-root.pem 2>nul
del server-ca.jks 2>nul
del server-ca.pem 2>nul

rem generate private keys (for root and ca)

keytool -genkeypair -alias root -dname "cn=oboco-root" -validity 10000 -keyalg RSA -keysize 2048 -ext "bc:c" -keystore server-root.jks -keypass "%PASSWORD%" -storepass "%PASSWORD%"
keytool -genkeypair -alias ca -dname "cn=oboco-ca" -validity 10000 -keyalg RSA -keysize 2048 -ext "bc:c" -keystore server-ca.jks -keypass "%PASSWORD%" -storepass "%PASSWORD%"

rem generate root certificate

keytool -exportcert -rfc -keystore server-root.jks -alias root -storepass "%PASSWORD%" > server-root.pem

rem generate a certificate for ca signed by root (root -> ca)

keytool -keystore server-ca.jks -storepass "%PASSWORD%" -certreq -alias ca | keytool -keystore server-root.jks -storepass "%PASSWORD%" -gencert -alias root -ext "bc=0" -ext "san=dns:ca" -rfc > server-ca.pem

rem import ca cert chain into server-ca.jks

keytool -keystore server-ca.jks -storepass "%PASSWORD%" -importcert -trustcacerts -noprompt -alias root -file server-root.pem
keytool -keystore server-ca.jks -storepass "%PASSWORD%" -importcert -alias ca -file server-ca.pem

:part2

echo  ====================================================================
echo  Creating server.jks and server.pem ...
echo  ====================================================================

del server.jks 2>nul
del server.pem 2>nul

rem generate private keys (for server)

keytool -genkeypair -alias server -dname "cn=oboco-server" -validity 10000 -keyalg RSA -keysize 2048 -keystore server.jks -keypass "%PASSWORD%" -storepass "%PASSWORD%"

rem generate a certificate for server signed by ca (root -> ca -> server)

keytool -keystore server.jks -storepass "%PASSWORD%" -certreq -alias server | keytool -keystore server-ca.jks -storepass "%PASSWORD%" -gencert -alias ca -ext "ku:c=dig,keyEnc" -ext "san=dns:%DNS%,ip:%IP%" -ext "eku=sa,ca" -rfc > server.pem

rem import server cert chain into server.jks

keytool -keystore server.jks -storepass "%PASSWORD%" -importcert -trustcacerts -noprompt -alias root -file server-root.pem
keytool -keystore server.jks -storepass "%PASSWORD%" -importcert -alias ca -file server-ca.pem
keytool -keystore server.jks -storepass "%PASSWORD%" -importcert -alias server -file server.pem

pause
