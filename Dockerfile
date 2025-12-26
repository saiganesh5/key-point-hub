FROM eclipse-temurin:21-jdk
COPY target/key-point-hub.jar key-point-hub.jar
ENTRYPOINT ["java","-jar","/key-point-hub.jar"]