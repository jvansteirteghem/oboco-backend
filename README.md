# oboco-backend

the backend of [oboco](https://gitlab.com/jeeto/oboco) as a quarkus and quarkus-native application (work in progress).

## quarkus

### requirements

- java 11
- maven >= 3.6.2

### configuration

- application
	- configure src\non-packaged-resources\data.properties
		- %ROOT_BOOK_COLLECTION%=%DATA_PATH%: the data (books, book collections)
	- configure src\main\resources\application.properties
		- quarkus.datasource.db-kind: h2
		- quarkus.datasource.jdbc.url: jdbc:h2:file:./database
		- quarkus.datasource.username: 
		- quarkus.datasource.password: 

### build

- mvn compile quarkus:dev
- mvn compile quarkus:dev -Dquarkus.args="start=DEFAULT"
- mvn compile quarkus:dev -Ddebug (port: 5005)

### test

- http://127.0.0.1:8080/swagger-ui/

## quarkus-native

### requirements

- docker
- database: h2, mysql or postgresql

### configuration

- docker
	- select "settings"
	- select "resources"
	- set "memory" to "6.00 gb"
- database
	- create database
		- src\non-packaged-resources\database*.ddl
		- src\non-packaged-resources\database*.sql
- application
	- configure src\main\resources\application.properties
		- quarkus.datasource.db-kind: "h2", "mysql" or "postgresql"

### build

- docker build --build-arg OBOCO_DATABASE_NAME=mysql -f Dockerfile-mandrel -t oboco-backend/2.0.0 .
	- "--build-arg OBOCO_DATABASE_NAME=mysql": the database type ("h2", "mysql" or "postgresql").

### run

- start
	- docker run -e TZ=Europe/Brussels -e OBOCO_SERVER_AUTHENTICATION_SECRET=secret -e OBOCO_DATABASE_URL=jdbc:mysql://192.168.0.219:3306/oboco -e OBOCO_DATABASE_USER_NAME=root -e OBOCO_DATABASE_USER_PASSWORD=toor -i --rm -p 8080:8080 -v c:/oboco/application-logger-data:/application-logger-data -v c:/oboco/application-data:/application-data -v c:/oboco/user-data:/user-data --name oboco-backend oboco-backend/2.0.0
		- "-e TZ=Europe/Brussels": the timezone
		- "-e OBOCO_SERVER_AUTHENTICATION_SECRET=secret": the authentication secret
		- "-e OBOCO_DATABASE_URL=jdbc:mysql://192.168.0.124:3306/oboco": the database url
		- "-e OBOCO_DATABASE_USER_NAME=root": the database user name
		- "-e OBOCO_DATABASE_USER_PASSWORD=toor": the database user password
		- "-v c:/oboco/application-logger-data:/application-logger-data": the logger data
		- "-v c:/oboco/application-data:/application-data": the data (book pages)
		- "-v c:/oboco/user-data:/user-data": the data (books, book collections)
- stop
	- docker stop oboco-backend

### test

- http://127.0.0.1:8080/swagger-ui/

### registry

you can use the latest docker image:
- h2: registry.gitlab.com/jeeto/oboco-backend/oboco-backend-h2:latest
- mysql: registry.gitlab.com/jeeto/oboco-backend/oboco-backend-mysql:latest
- postgresql: registry.gitlab.com/jeeto/oboco-backend/oboco-backend-postgresql:latest

### development

- update test branch with master
	- git checkout master
	- git pull
	- git checkout test
	- git merge master
	- git push origin test
- update master with test branch
	- git checkout master
	- git merge test
	- git push origin master
- push docker image
	- docker login registry.gitlab.com -u jeeto -p %TOKEN%
	- docker build --build-arg OBOCO_DATABASE_NAME=%DATABASE_NAME% -f Dockerfile-mandrel -t registry.gitlab.com/jeeto/oboco-backend/oboco-backend-%DATABASE_NAME%:latest .
	- docker push registry.gitlab.com/jeeto/oboco-backend/oboco-backend-%DATABASE_NAME%:latest

## license

mit license