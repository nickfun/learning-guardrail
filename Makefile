
run: jar
	java -jar target/todoapi-1.0-SNAPSHOT-fatjar.jar

target/todoapi-1.0-SNAPSHOT-fatjar.jar:
	mvn package

clean:
	sbt clean
	rm PUSH_REMOTE.tar

jar:
	mvn package

e2e:
	java -cp target/todoapi-1.0-SNAPSHOT-fatjar.jar gs.nick.AppTester "http://localhost:8080"

deploy: PUSH_REMOTE.tar
	caprover deploy -t PUSH_REMOTE.tar


PUSH_REMOTE.tar: captain-definition Dockerfile target/todoapi-1.0-SNAPSHOT-fatjar.jar
	tar -cvf PUSH_REMOTE.tar captain-definition Dockerfile target/todoapi-1.0-SNAPSHOT-fatjar.jar

