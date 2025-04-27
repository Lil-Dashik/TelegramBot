FROM openjdk:17-jdk-slim-buster
WORKDIR /app
COPY target/bot-0.0.1-SNAPSHOT.jar /app/bot.jar
ENTRYPOINT ["java","-jar", "bot.jar"]