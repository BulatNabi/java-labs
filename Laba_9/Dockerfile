FROM eclipse-temurin:17-jdk

ENV DERBY_VERSION=10.16.1.1
ENV DERBY_HOME=/derby

RUN apt-get update && apt-get install -y wget && rm -rf /var/lib/apt/lists/*

RUN wget -q https://archive.apache.org/dist/db/derby/db-derby-${DERBY_VERSION}/db-derby-${DERBY_VERSION}-bin.tar.gz \
    && tar -xzf db-derby-${DERBY_VERSION}-bin.tar.gz \
    && mv db-derby-${DERBY_VERSION}-bin ${DERBY_HOME} \
    && rm db-derby-${DERBY_VERSION}-bin.tar.gz \
    && mkdir -p ${DERBY_HOME}/data

WORKDIR ${DERBY_HOME}/data

EXPOSE 1527

CMD ["java", "-jar", "/derby/lib/derbyrun.jar", "server", "start", "-h", "0.0.0.0", "-p", "1527"]
