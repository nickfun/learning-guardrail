#!/bin/bash
mvn clean
mvn package && java -jar target/birds-1.0-SNAPSHOT-fatjar.jar
