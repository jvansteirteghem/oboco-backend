#!/bin/bash

# env

# replace

# start

./application -Dquarkus.http.host=0.0.0.0 \
 application.security.authentication.secret=${SECRET} \
 application.server.port=8080 \
 application.database.url=${DATABASE_URL} \
 application.database.user.name=${DATABASE_USER_NAME} \
 application.database.user.password=${DATABASE_USER_PASSWORD} \
 application.data.path=/data-application \
 user.data.path=/data-user