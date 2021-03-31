#!/bin/bash

# env

# replace
echo 'DEFAULT=/user-data' > data.properties

# start

./application \
 logger.path=/application-logger-data/application.log \
 data.path=/application-data