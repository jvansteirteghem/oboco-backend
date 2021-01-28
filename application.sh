#!/bin/bash

# env

# replace

# start

./application -Dquarkus.http.host=0.0.0.0 \
 application.logger.path=/application-logger-data/application.log \
 application.data.path=/application-data \
 user.data.path=/user-data