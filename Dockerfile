## https://quarkus.io/guides/building-native-image

## Stage 1 : build with maven builder image with native capabilities
FROM quay.io/quarkus/centos-quarkus-maven:21.3.0-java11 AS build

ARG OBOCO_DATABASE_NAME

COPY pom.xml /usr/src/app/
RUN mvn -f /usr/src/app/pom.xml -B de.qaware.maven:go-offline-maven-plugin:1.2.5:resolve-dependencies
COPY src /usr/src/app/src
USER root
RUN chown -R quarkus /usr/src/app
USER quarkus
RUN sed -i "s/quarkus\.datasource\.db\-kind\=.*/quarkus\.datasource\.db\-kind\=${OBOCO_DATABASE_NAME}/" /usr/src/app/src/main/resources/application.properties \
 && sed -i "s/quarkus\.datasource\.username\=.*/quarkus\.datasource\.username\=/" /usr/src/app/src/main/resources/application.properties \
 && sed -i "s/quarkus\.datasource\.password\=.*/quarkus\.datasource\.password\=/" /usr/src/app/src/main/resources/application.properties \
 && sed -i "s/quarkus\.datasource\.jdbc\.url\=.*/quarkus\.datasource\.jdbc\.url\=/" /usr/src/app/src/main/resources/application.properties
RUN mvn -f /usr/src/app/pom.xml -Pnative clean package

## Stage 2 : build dependencies
FROM registry.access.redhat.com/ubi8/ubi-minimal as build-dependencies

RUN microdnf update
RUN microdnf install freetype fontconfig

## Stage 3 : create the docker final image
FROM quay.io/quarkus/quarkus-micro-image:1.0

COPY --from=build-dependencies \
   /lib64/libfreetype.so.6 \
   /lib64/libgcc_s.so.1 \
   /lib64/libbz2.so.1 \
   /lib64/libpng16.so.16 \
   /lib64/libm.so.6 \
   /lib64/libbz2.so.1 \
   /lib64/libexpat.so.1 \
   /lib64/libuuid.so.1 \
   /lib64/

COPY --from=build-dependencies \
   /usr/lib64/libfontconfig.so.1 \
   /usr/lib64/

COPY --from=build-dependencies \
    /usr/share/fonts /usr/share/fonts

COPY --from=build-dependencies \
    /usr/share/fontconfig /usr/share/fontconfig

COPY --from=build-dependencies \
    /usr/lib/fontconfig /usr/lib/fontconfig

COPY --from=build-dependencies \
     /etc/fonts /etc/fonts

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