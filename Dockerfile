FROM openjdk:17
COPY target/todoapi-1.0-SNAPSHOT-fatjar.jar /usr/src/myapp/app.jar
WORKDIR /usr/src/myapp
CMD ["java", "-jar", "app.jar"]
