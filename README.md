# oboco-backend

the backend of [oboco](https://gitlab.com/jeeto/oboco) as a quarkus and quarkus-native application (work in progress).

## quarkus

### requirements

- java 8 or 11
- maven >= 3.6.2

### configuration

- application
	- configure src\non-packaged-resources\user.properties
		- data.path: the data (books, book collections)
	- configure src\main\resources\application.properties
		- quarkus.datasource.db-kind: h2
		- quarkus.datasource.jdbc.url=jdbc:h2:file:./database
		- quarkus.datasource.username=
		- quarkus.datasource.password=

### build

- mvn compile quarkus:dev
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

- docker build -f Dockerfile-mandrel -t oboco/2.0.0 .

### run

- start
	- docker run -e TZ=Europe/Brussels -e QUARKUS_DATASOURCE_JDBC_URL=jdbc:mysql://192.168.0.124:3306/oboco -e QUARKUS_DATASOURCE_USERNAME=root -e QUARKUS_DATASOURCE_PASSWORD=toor -i --rm -p 8080:8080 -v c:/data:/data --name oboco oboco/2.0.0
		- "-e TZ=Europe/Brussels": the timezone
		- "-e QUARKUS_DATASOURCE_JDBC_URL=jdbc:mysql://192.168.0.124:3306/oboco": the database url
		- "-e QUARKUS_DATASOURCE_USERNAME=root": the database user name
		- "-e QUARKUS_DATASOURCE_PASSWORD=toor": the database user password
		- "-v c:/data:/data": the data (books, book collections)
- stop
	- docker stop oboco

### test

- http://127.0.0.1:8080/swagger-ui/

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