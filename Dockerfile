FROM openjdk:8-jre-alpine
EXPOSE 8080
ADD JavaOpenChess/target/jChess-1.5.1.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]