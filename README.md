# oboco-backend

http://127.0.0.1:8080/swagger-ui/
http://127.0.0.1:8080/openapi

mvn compile quarkus:dev
mvn compile quarkus:dev -Ddebug

debug on port 5005

in mysql:
C:\Program Files\MariaDB 10.4\data\my.ini
add parameter "bind-address = 0.0.0.0" to "[mysqld]"

in Dockerfile:
add parameter "-Dquarkus.datasource.jdbc.url=jdbc:mysql://192.168.0.124:3306/oboco"

docker build -f Dockerfile -t oboco/2.0.0 .
docker run -i --rm -p 8080:8080 -v c:/data1:/data1 -v c:/data2:/data2 --name oboco oboco/2.0.0
docker stop oboco
docker ps -a