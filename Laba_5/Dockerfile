FROM eclipse-temurin:17-jdk AS build
WORKDIR /build

# 1) compile compute interfaces, package into compute.jar
COPY interface/compute/ ./compute/
RUN javac compute/*.java \
 && jar cvf /build/compute.jar compute/*.class

# 2) compile server (engine.ComputeEngine) using compute.jar
COPY server/engine/ ./server-src/engine/
RUN mkdir -p /out/server \
 && javac -cp /build/compute.jar -d /out/server server-src/engine/*.java

# 3) compile client (client.ClientMain, client.SplitTask) using compute.jar
COPY client/client/ ./client-src/client/
RUN mkdir -p /out/client \
 && javac -cp /build/compute.jar -d /out/client client-src/client/*.java

FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY --from=build /build/compute.jar /app/compute.jar
COPY --from=build /out/server /app/server
COPY --from=build /out/client /app/client
COPY client/client.policy /app/client.policy
