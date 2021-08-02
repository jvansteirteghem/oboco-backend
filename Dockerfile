## https://quarkus.io/guides/building-native-image

## Stage 1 : build with maven builder image with native capabilities
FROM quay.io/quarkus/centos-quarkus-maven:21.2.0-java11 AS build

ARG OBOCO_DATABASE_NAME

COPY pom.xml /usr/src/app/
RUN mvn -f /usr/src/app/pom.xml -B de.qaware.maven:go-offline-maven-plugin:1.2.5:resolve-dependencies
COPY src /usr/src/app/src
USER root
RUN chown -R quarkus /usr/src/app
USER quarkus
COPY src/non-packaged-resources/lib-native/turbojpeg/linux/amd64/libturbojpeg.so /usr/java/packages/lib/libturbojpeg.so
RUN sed -i "s/quarkus\.datasource\.db\-kind\=.*/quarkus\.datasource\.db\-kind\=${OBOCO_DATABASE_NAME}/" /usr/src/app/src/main/resources/application.properties \
 && sed -i "s/quarkus\.datasource\.username\=.*/quarkus\.datasource\.username\=/" /usr/src/app/src/main/resources/application.properties \
 && sed -i "s/quarkus\.datasource\.password\=.*/quarkus\.datasource\.password\=/" /usr/src/app/src/main/resources/application.properties \
 && sed -i "s/quarkus\.datasource\.jdbc\.url\=.*/quarkus\.datasource\.jdbc\.url\=/" /usr/src/app/src/main/resources/application.properties
RUN mvn -f /usr/src/app/pom.xml -Pnative clean package

## Stage 2 : create the docker final image
FROM registry.access.redhat.com/ubi8/ubi-minimal

RUN microdnf update

WORKDIR /work/
COPY --from=build /usr/src/app/target/*-runner /work/application
COPY --from=build /usr/src/app/target/application.properties /work/application.properties
COPY --from=build /usr/src/app/target/data.properties /work/data.properties
COPY --from=build /usr/src/app/target/data /work/data
COPY --from=build /usr/src/app/target/data.csv /work/data.csv
#COPY --from=build /usr/src/app/target/database.ddl /work/database.ddl
#COPY --from=build /usr/src/app/target/database.sql /work/database.sql
COPY --from=build /usr/src/app/target/logs /work/logs
COPY --from=build /usr/src/app/target/lib-native /work/lib-native

COPY application.sh /usr/local/bin/
RUN chmod 777 /usr/local/bin/application.sh

# set up permissions for user `1001`
RUN chmod 775 /work /work/application \
  && chown -R 1001 /work \
  && chmod -R "g+rwX" /work \
  && chown -R 1001:root /work

EXPOSE 8080
USER 1001

CMD ["bash", "/usr/local/bin/application.sh"]