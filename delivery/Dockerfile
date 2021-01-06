FROM maven:3-openjdk-11-slim as builder

COPY src /build/src
COPY pom.xml /build
WORKDIR /build/
RUN mvn package

FROM openjdk:11-jre-slim

COPY --from=builder /build/target/delivery-0.0.1-SNAPSHOT.jar /usr/local/lib/app.jar

EXPOSE 9901
ENTRYPOINT ["java","-jar","/usr/local/lib/app.jar"]