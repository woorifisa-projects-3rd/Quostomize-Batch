FROM eclipse-temurin:17-jdk-alpine
COPY ./build/libs/*SNAPSHOT.jar batch.jar
ENTRYPOINT ["java", "-jar", "batch.jar"]