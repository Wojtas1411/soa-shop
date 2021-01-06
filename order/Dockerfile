FROM maven:3-openjdk-11-slim as builder

COPY src /build/src
COPY pom.xml /build
WORKDIR /build/
RUN mvn package -DskipTests

FROM openjdk:8-jre-slim

COPY --from=builder /build/target/shop-0.0.1-SNAPSHOT.jar /usr/local/lib/app.jar

EXPOSE 9000
ENTRYPOINT ["java","-jar","-Dspring.profiles.active=docker","/usr/local/lib/app.jar"]