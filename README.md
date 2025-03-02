# AI-Mammoth Spring Service

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.8-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](https://opensource.org/licenses/MIT)
[![Sber](https://img.shields.io/badge/Sber-Internal-green.svg)](https://www.sber.ru)

## 📋 Обзор

AI-Mammoth Spring Service является ключевым компонентом экосистемы [AI-Mammoth Agent](https://github.com/SaltyFrappuccino/AI-Mammoth-agent). Этот сервис специализируется на сборе, обработке и агрегации данных из различных корпоративных систем в инфраструктуре Сбера, что позволяет ИИ-ассистентам получать доступ к этой информации и использовать её.

**⚠️ Важное примечание:** Данный сервис предназначен для работы исключительно в инфраструктуре и сетевой среде Сбера.

## 🌟 Возможности

- **Интеграция с множеством источников данных:** Сбор данных из:
  - 📝 Confluence (документация и база знаний)
  - 🎫 Jira (отслеживание задач и проектов)
  - ✅ Zephyr (управление тестовыми сценариями)
  - 🧩 Bitbucket (репозитории исходного кода)
  - 🏭 SberWorks (внутренняя платформа Сбера)

- **Запланированный сбор данных:** Автоматизированный сбор информации с настраиваемым расписанием
- **RESTful API:** Удобные конечные точки для получения данных
- **Документация OpenAPI:** Встроенный Swagger UI для изучения API
- **Безопасная аутентификация:** Интеграция с инфраструктурой безопасности Сбера

## 🔌 Точки интеграции

Сервис подключается к нескольким источникам данных для сбора необходимой информации:

### Confluence
- Доступ к пространствам, страницам и документам
- Извлечение контента с правильным форматированием
- Обработка вложений

### Jira
- Получение данных о проектах и задачах
- Отслеживание статусов и информации о приоритетах
- Поддержка пользовательских полей

### Zephyr
- Извлечение тест-планов и тест-кейсов
- Данные о результатах тестирования и выполнении
- Сбор метрик тестирования

### Bitbucket
- Получение списка и доступ к репозиториям
- Получение истории коммитов
- Информация о запросах на слияние (pull request)
- Извлечение фрагментов кода

### SberWorks
- Интеграция с внутренней платформой разработки Сбера
- Доступ к ресурсам, специфичным для платформы

## 🛠️ Технологический стек

- **Spring Boot:** Основной фреймворк приложения
- **Java 17:** Новейшие возможности Java для надежной разработки
- **PostgreSQL:** Реляционная база данных для постоянного хранения
- **Spring JDBC:** Подключение к базе данных и операции
- **Spring MVC:** Разработка RESTful API
- **Springdoc OpenAPI:** Документация API
- **Maven:** Управление зависимостями и сборка
- **Lombok:** Уменьшение шаблонного кода
- **Spring Actuator:** Мониторинг приложения и метрики

## 🚀 Начало работы

### Предварительные требования

- Java Development Kit (JDK) 17
- Maven 3.6+
- Доступ к внутренней сети Сбера
- Учетные данные для аутентификации во всех интегрированных системах
- Экземпляр базы данных PostgreSQL

### Конфигурация

Приложение можно настроить с помощью файла `application.yaml`. Ключевые области конфигурации включают:

```yaml
# Конфигурация базы данных
spring:
  datasource:
    url: jdbc:postgresql://[DB_HOST]:[DB_PORT]/[DB_NAME]
    username: [USERNAME]
    password: [PASSWORD]

# Точки интеграции
integration:
  confluence:
    base-url: [CONFLUENCE_URL]
    username: [USERNAME]
    api-token: [API_TOKEN]
  
  jira:
    base-url: [JIRA_URL]
    username: [USERNAME]
    api-token: [API_TOKEN]
    
  bitbucket:
    base-url: [BITBUCKET_URL]
    username: [USERNAME]
    api-token: [API_TOKEN]
    
  zephyr:
    base-url: [ZEPHYR_URL]
    username: [USERNAME]
    api-token: [API_TOKEN]
    
  sberworks:
    base-url: [SBERWORKS_URL]
    token: [TOKEN]

# Настройка планировщика
scheduler:
  enabled: true
  cron:
    confluence: "0 0 * * * *"  # Ежечасно
    jira: "0 */30 * * * *"     # Каждые 30 минут
    bitbucket: "0 15 * * * *"  # 15 минут каждого часа
    zephyr: "0 45 * * * *"     # 45 минут каждого часа
```

### Запуск приложения

1. Клонируйте репозиторий:
   ```bash
   git clone [repository-url]
   cd AI-Mammoth-spring
   ```

2. Соберите приложение:
   ```bash
   ./mvnw clean package
   ```

3. Запустите приложение:
   ```bash
   ./mvnw spring-boot:run
   ```

4. Получите доступ к документации API:
   ```
   http://localhost:8080/swagger-ui.html
   ```

## 📊 Документация API

Сервис предоставляет комплексный RESTful API для взаимодействия с собранными данными. API документирован с использованием OpenAPI и доступен через Swagger UI при запущенном приложении.

Основные конечные точки включают:

- `/api/v1/confluence` - Доступ к данным Confluence
- `/api/v1/jira` - Доступ к данным задач Jira
- `/api/v1/bitbucket` - Доступ к данным репозиториев Bitbucket
- `/api/v1/zephyr` - Доступ к данным тест-кейсов Zephyr
- `/api/v1/search` - Унифицированный поиск по всем источникам данных

## 🔄 Интеграция с AI-Mammoth Agent

Этот сервис функционирует как поставщик данных для [AI-Mammoth Agent](https://github.com/SaltyFrappuccino/AI-Mammoth-agent), предоставляя ему структурированную и обработанную информацию из различных корпоративных систем. Агент использует эти данные для предоставления интеллектуальных ответов и помощи пользователям.