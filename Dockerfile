#
# Build stage
#
FROM maven:3.6.0-jdk-11-slim AS build
COPY opening-hours-dto /home/opening-hours/opening-hours-dto
COPY opening-hours-ws /home/opening-hours/opening-hours-ws
COPY pom.xml /home/opening-hours
RUN mvn -f /home/opening-hours/pom.xml clean package

#
# Package stage
#
FROM openjdk:11-jre-slim
COPY --from=build /home/opening-hours/opening-hours-ws/target/opening-hours-ws-0.0.1-SNAPSHOT.jar /usr/local/lib/opening-hours.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/usr/local/lib/opening-hours.jar"]