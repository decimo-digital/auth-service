FROM openjdk:15 as builder

COPY . /app 

WORKDIR /app

RUN ./mvnw package -DskipTests

RUN mv ./target/*.jar app.jar

FROM openjdk:15 as runner 

COPY --from=builder /app/app.jar app.jar

ENTRYPOINT [ "java", "-jar", "app.jar"]