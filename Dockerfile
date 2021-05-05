FROM maven:3.6.0-jdk-11-slim as build

RUN apt-get update && apt-get -y install git make

ENV MAVEN_OPTS="-Dhttps.protocols=TLSv1.2 -Dmaven.repo.local=~/.m2/repository -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=WARN -Dorg.slf4j.simpleLogger.showDateTime=true -Djava.awt.headless=true"
ENV MAVEN_CLI_OPTS="--batch-mode --errors --fail-at-end --show-version -DinstallAtEnd=true -DdeployAtEnd=true"
WORKDIR /app

COPY . .

RUN make build

FROM openjdk:11.0.1-jre as base
COPY --from=build /app/target/nsds.jar /app/nsds.jar
EXPOSE 8080

ENTRYPOINT ["java", "-Xms512m", "-Xmx2g", "-XX:+UseG1GC", "-jar", "/app/nsds.jar"]
