#!/bin/bash
mvn clean
mvn package && java -jar target/todoapi-1.0-SNAPSHOT-fatjar.jar
