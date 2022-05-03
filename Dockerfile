FROM openjdk:11
EXPOSE  8085
WORKDIR /app
ADD   ./target/*.jar /app/movementCredit-service.jar
ENTRYPOINT ["java","-jar","/app/movementCredit-service.jar"] 