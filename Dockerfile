# Используем официальный образ OpenJDK с JDK 18
FROM eclipse-temurin:18-jdk-alpine

# Устанавливаем рабочую директорию внутри контейнера
WORKDIR /app

# Копируем файл с билдом приложения (JAR-файл) в контейнер
COPY target/Bets-1.0-SNAPSHOT.jar bets-app.jar

# Открываем порт, который будет использовать Spring Boot
EXPOSE 8084

# Команда для запуска приложения
ENTRYPOINT ["java", "-jar", "bets-app.jar"]
