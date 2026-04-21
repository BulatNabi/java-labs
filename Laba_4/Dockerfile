FROM eclipse-temurin:21-jdk AS build
WORKDIR /build
COPY src/ ./src/
RUN mkdir -p out && javac -d out src/*.java

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /build/out /app/classes
ENTRYPOINT ["java", "-cp", "/app/classes"]
