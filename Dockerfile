FROM eclipse-temurin:21-jre

WORKDIR /app

COPY leafCatcher-1.0-SNAPSHOT.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]


EXPOSE 8080
