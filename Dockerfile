# Java version
FROM ghcr.io/graalvm/jdk-community:17

# Metadata and labels
LABEL maintainer="puumInc@outlook.com"
LABEL description="Jangopham E-commerce backend"

# Set the working directory
WORKDIR /jungophram

# Copy the JAR file to the working directory
ARG JAR_FILE=build/libs/ecommerce-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} ./ecommerce.jar

# Expose the application port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java",\
"-Dcom.sun.management.jmxremote=true", \
"-Dcom.sun.management.jmxremote.port=8085", \
"-Dcom.sun.management.jmxremote.local.only=false", \
"-Dcom.sun.management.jmxremote.authenticate=false", \
"-Dcom.sun.management.jmxremote.ssl=false", \
"-Dcom.sun.management.jmxremote.rmi.port=8085", \
"-Djava.rmi.server.hostname=localhost", \
 "-Xmx700M", \
 "-Xms150M", \
 "-jar", \
 "ecommerce.jar"]