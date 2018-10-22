#!/bin/bash
mvn clean
mvn package && java -jar target/swag1-1.0-SNAPSHOT-fatjar.jar
