FROM openjdk:17-jdk
LABEL authors="MrSinkaaa"

WORKDIR /app

COPY ./build/libs/cloud-file-storage-0.0.1-SNAPSHOT.jar /app/cloud-file-storage.jar

EXPOSE 8080 5005

# Добавление параметров для отладки
ENTRYPOINT ["java", "-agentlib:jdwp=transport=dt_socket,address=*:5005,server=y,suspend=n", "-jar", "/app/cloud-file-storage.jar"]

CMD ["java", "-jar", "cloud-file-storage.jar"]
