#!/bin/bash
export PORT=8080
export DOMAIN=http://localhost:8080
export ENABLE_HTTPS=off
mvn clean
mvn package && java -jar target/todoapi-1.0-SNAPSHOT-fatjar.jar
