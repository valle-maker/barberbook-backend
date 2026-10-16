FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build
COPY pom.xml .
COPY src ./src
RUN mvn -q -B -DskipTests package

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /build/target/*.jar app.jar
# Tuned for the free plan (512 MB, a fraction of a CPU): a smaller heap leaves room for the JVM's
# own memory, single-threaded GC, small thread stacks, and the light JIT compiler (C1 only) so
# startup doesn't starve the CPU.
ENV JAVA_OPTS="-XX:MaxRAMPercentage=60 -XX:MaxMetaspaceSize=128m -XX:+UseSerialGC -Xss512k -XX:TieredStopAtLevel=1"
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
