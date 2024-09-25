FROM openjdk:17-jdk-alpine

WORKDIR /app
COPY . .
COPY ./docker/application.yml ./src/main/resources/application.yml

RUN ./mvnw clean package

EXPOSE 8080

CMD [ "java", "-jar", "target/drive-1.0.0.jar", "--seeder=role"]

