## https://quarkus.io/guides/building-native-image

## Stage 1 : build with maven builder image with native capabilities
FROM quay.io/quarkus/centos-quarkus-maven:20.2.0-java11 AS build
COPY pom.xml /usr/src/app/
RUN mvn -f /usr/src/app/pom.xml -B de.qaware.maven:go-offline-maven-plugin:1.2.5:resolve-dependencies
COPY src /usr/src/app/src
USER root
RUN chown -R quarkus /usr/src/app
USER quarkus
COPY src/non-packaged-resources/lib-native/turbojpeg/linux/amd64/libturbojpeg.so /usr/java/packages/lib/libturbojpeg.so
RUN mvn -f /usr/src/app/pom.xml -Pnative clean package

## Stage 2 : create the docker final image
FROM registry.access.redhat.com/ubi8/ubi-minimal
WORKDIR /work/
COPY --from=build /usr/src/app/target/*-runner /work/application
COPY --from=build /usr/src/app/target/application.properties /work/application.properties
COPY --from=build /usr/src/app/target/user.properties /work/user.properties
COPY --from=build /usr/src/app/target/data /work/data
COPY --from=build /usr/src/app/target/data.csv /work/data.csv
#COPY --from=build /usr/src/app/target/database.ddl /work/database.ddl
#COPY --from=build /usr/src/app/target/database.sql /work/database.sql
COPY --from=build /usr/src/app/target/logs /work/logs
COPY --from=build /usr/src/app/target/lib-native /work/lib-native

RUN sed -i 's/data\.path\=C\:\/data1\,C\:\/data2/data\.path\=\/data1,\/data2/' user.properties

# set up permissions for user `1001`
RUN chmod 775 /work /work/application \
  && chown -R 1001 /work \
  && chmod -R "g+rwX" /work \
  && chown -R 1001:root /work

EXPOSE 8080
USER 1001

CMD ["./application", "-Dquarkus.http.host=0.0.0.0", "-Dquarkus.datasource.jdbc.url=jdbc:mysql://192.168.0.124:3306/oboco"]