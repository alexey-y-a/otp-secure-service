## Backend-сервис для генерации и валидации временных одноразовых кодов подтверждения (One-Time Password) с поддержкой нескольких каналов доставки.

---

## Содержание

- [Технологии](#технологии)
- [Функциональность](#функциональность)
- [API эндпоинты](#api-эндпоинты)
- [Требования](#требования)
- [Установка и запуск](#установка-и-запуск)
- [Тестирование API](#тестирование-api)
- [Переменные окружения](#переменные-окружения)
- [Структура проекта](#структура-проекта)

---

## Технологии

| Технология | Версия | Назначение |
|------------|--------|------------|
| Java | 17 | Язык программирования |
| Spring Boot | 3.3.4 | Основной фреймворк |
| Spring Security | 3.3.4 | Аутентификация и авторизация |
| Spring Data JPA | 3.3.4 | ORM и работа с БД |
| PostgreSQL | 17 | База данных |
| H2 | - | База данных для тестов |
| JJWT | 0.12.6 | JWT токены |
| JSM PP | 3.0.1 | SMPP протокол для SMS |
| Logback | - | Логирование |
| Gradle | 8.14 | Сборка проекта |
| Lombok | - | Упрощение кода |
| Docker | - | Контейнеризация PostgreSQL и SMPP эмулятора |

---

## Функциональность

### Аутентификация и авторизация
- Регистрация пользователей
- Логин с выдачей JWT токена
- Разграничение ролей: **ADMIN** и **USER**
- Первый зарегистрированный пользователь становится ADMIN

### Управление OTP кодами
- Генерация OTP кодов с настраиваемой длиной (4-8 символов)
- Настраиваемое время жизни кода (1-30 минут)
- Три статуса кода: **ACTIVE**, **EXPIRED**, **USED**
- Автоматическое истечение просроченных кодов (каждую минуту)

### Каналы доставки OTP
| Канал | Формат destination |
|-------|--------------------|
| Email | `user@example.com` |
| SMS (SMPP) | `79123456789` |
| Telegram | `123456789` (chatId) |
| File | `имя_пользователя` |

---

## API эндпоинты

### Публичные (без токена)
| Метод | Эндпоинт | Описание |
|-------|----------|----------|
| POST | `/api/auth/register` | Регистрация пользователя |
| POST | `/api/auth/login` | Вход (возвращает JWT токен) |

### Пользовательские (требуют токен)
| Метод | Эндпоинт | Описание |
|-------|----------|----------|
| POST | `/api/otp/generate?channel={channel}&destination={dest}&operationId={id}` | Генерация и отправка OTP |
| POST | `/api/otp/validate?code={code}` | Проверка OTP кода |

### Административные (требуют роль ADMIN)
| Метод | Эндпоинт | Описание |
|-------|----------|----------|
| GET | `/api/admin/users` | Список всех пользователей |
| DELETE | `/api/admin/users/{userId}` | Удаление пользователя и его OTP кодов |
| GET | `/api/admin/config` | Получить конфигурацию OTP |
| PUT | `/api/admin/config?codeLength={len}&ttlMinutes={min}` | Обновить конфигурацию OTP |

---

## Требования

- Java 17+
- Docker Desktop (для PostgreSQL и SMPP эмулятора)
- Gradle 8.14+ (используется wrapper)

---

## Установка и запуск

### Клонирование репозитория

```
git clone https://github.com/alexey-y-a/otp-secure-service.git
cd otp-secure-service
```

### Настройка переменных окружения
- Создать файл .env в корне проекта:
```
MAIL_USERNAME=your_email@gmail.com
MAIL_PASSWORD=your_app_password
TELEGRAM_BOT_TOKEN=your_telegram_bot_token
```

### Запуск PostgreSQL в Docker
```
docker run --name otp-postgres \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -e POSTGRES_DB=otpdb \
  -p 5432:5432 \
  -d postgres:17
```

### Запуск SMPP эмулятора в Docker
```
docker run --name smppsim \
  -p 2775:2775 \
  -p 8088:8088 \
  -d \
  --restart unless-stopped \
  eagafonov/smppsim
```

### Запуск приложения
- Загрузить переменные окружения

`export $(grep -v '^#' .env | xargs)`

- Запустить приложение

`./gradlew bootRun`

- Приложение запустится на порту 8080

---

## Тестирование API
- Регистрация (первый пользователь → ADMIN)
```
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

- Логин
```
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

- Генерация OTP кода (Telegram)
```
вместо ChatID вставьте chat_id вашего телеграм бота,

curl -X POST "http://localhost:8080/api/otp/generate?channel=TELEGRAM&destination=ChatID&operationId=test" \
  -H "Authorization: Bearer <ваш_токен>"
```

- Генерация OTP кода (File)
```
curl -X POST "http://localhost:8080/api/otp/generate?channel=FILE&destination=admin&operationId=test" \
  -H "Authorization: Bearer <ваш_токен>"
```

- Проверить сохранённый код:

`cat otp_codes.log`

- Генерация OTP кода (SMS)
```
вместо phone_number укажите номер телефона,

curl -X POST "http://localhost:8080/api/otp/generate?channel=SMS&destination=phone_number&operationId=test" \
  -H "Authorization: Bearer <ваш_токен>"
```

- Генерация OTP кода (Email)
```
вместо your@eamil.com укажите ваш email адрес,

curl -X POST "http://localhost:8080/api/otp/generate?channel=EMAIL&destination=your@email.com&operationId=test" \
  -H "Authorization: Bearer <ваш_токен>"
```

- Валидация OTP кода
```
вместо 123456 укажите реальный полученный от сервера code из файла otp_codes.log,

curl -X POST "http://localhost:8080/api/otp/validate?code=123456" \
  -H "Authorization: Bearer <ваш_токен>"
```

- ADMIN API — получить всех пользователей
```
curl -X GET http://localhost:8080/api/admin/users \
  -H "Authorization: Bearer <admin_токен>"
```

- ADMIN API — получить конфигурацию OTP
```
curl -X GET http://localhost:8080/api/admin/config \
  -H "Authorization: Bearer <admin_токен>"
```

- ADMIN API — обновить конфигурацию OTP
```
curl -X PUT "http://localhost:8080/api/admin/config?codeLength=8&ttlMinutes=10" \
  -H "Authorization: Bearer <admin_токен>"
```

---

## Переменные окружения

Для работы приложения необходимы следующие переменные:

| Переменная | Описание | Пример |
|------------|----------|--------|
| `MAIL_USERNAME` | Email отправителя | `your@gmail.com` |
| `MAIL_PASSWORD` | Пароль приложения Gmail | `xxxx xxxx xxxx xxxx` |
| `TELEGRAM_BOT_TOKEN` | Токен Telegram бота | `123456:XXX-XXXxxx-xxxxx-xxxxx` |

**Как получить:**
- **Gmail пароль приложения:** Google Account → Безопасность → Пароли приложений
- **Telegram токен:** @BotFather в Telegram → `/newbot`

---

## Структура проекта

```
src/main/java/ru/alexey/otpsecureservice/
├── OtpSecureServiceApplication.java     # Точка входа
├── controller/                          # Слой API
│   ├── AuthController.java              # Регистрация и логин
│   ├── OtpController.java               # Генерация и валидация OTP
│   └── AdminController.java             # Административное API
├── dto/                                 # DTO для запросов/ответов
│   ├── AuthRequest.java
│   ├── AuthResponse.java
│   └── RegisterRequest.java
├── filter/                              # Фильтры
│   └── LoggingFilter.java               # Детальное логирование запросов
├── model/                               # Entity (JPA)
│   ├── User.java
│   ├── OtpCode.java
│   ├── OtpConfig.java
│   ├── OtpStatus.java                   # ACTIVE, EXPIRED, USED
│   └── Role.java                        # USER, ADMIN
├── repository/                          # DAO слой (Spring Data JPA)
│   ├── UserRepository.java
│   ├── OtpCodeRepository.java
│   └── OtpConfigRepository.java
├── security/                            # Безопасность
│   ├── JwtUtil.java                     # Генерация и валидация JWT
│   ├── JwtAuthenticationFilter.java     # Фильтр аутентификации
│   └── SecurityConfig.java              # Конфигурация Spring Security
├── service/                             # Бизнес-логика
│   ├── UserService.java                 # Регистрация, логин
│   ├── OtpService.java                  # Генерация, валидация
│   └── OtpConfigService.java            # Настройки и scheduled task
└── notification/                        # Каналы доставки
    ├── NotificationService.java         # Интерфейс
    ├── EmailNotificationService.java    # Email (SMTP)
    ├── SmsNotificationService.java      # SMS (SMPP эмулятор)
    ├── TelegramNotificationService.java # Telegram Bot API
    ├── FileNotificationService.java     # Сохранение в файл
    └── NotificationFacade.java          # Фасад для выбора канала

src/main/resources/
├── application.yml                      # Конфигурация (секреты через env)
└── logback-spring.xml                   # Настройка логирования
```