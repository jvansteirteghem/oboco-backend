#!/bin/bash

# env

# replace
echo 'DEFAULT=/user-data' > data.properties

# start

./application \
 application.logger.path=/application-logger-data/application.log \
 application.data.path=/application-data