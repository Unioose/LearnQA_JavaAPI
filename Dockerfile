FROM maven:3.6.3-openjdk-14

WORKDIR /tests

# Копируем файлы проекта
COPY . .

# Команда для запуска тестов
CMD ["mvn", "clean", "test"]