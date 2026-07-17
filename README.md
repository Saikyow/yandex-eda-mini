# order-platform

Учебная микросервисная платформа обработки заказов на **Java 21 + Spring Boot 3.5**. Демонстрирует связку синхронного (HTTP) и асинхронного (Kafka) взаимодействия между сервисами на примере полного жизненного цикла заказа: создание → оплата → назначение доставки.

## Архитектура

Проект — мультимодульный Gradle-проект из трёх сервисов и общей библиотеки контрактов.

```
                 HTTP POST /api/orders/{id}/pay
   ┌─────────────┐         (sync HTTP)          ┌──────────────────┐
   │   Клиент    │ ───────────────────────────▶ │  order-service   │
   └─────────────┘                              │      :8080       │
                                                └────────┬─────────┘
                                                         │ HTTP (sync)
                                                         ▼
                                                ┌──────────────────┐
                                                │ payment-service  │
                                                │      :8081       │
                                                └──────────────────┘
                          OrderPaidEvent                 │
                       topic: orders.events              │ order-service публикует
                                ┌────────────────────────┘  после успешной оплаты
                                ▼
                       ┌──────────────────┐
                       │ delivery-service │
                       │      :8082       │
                       └────────┬─────────┘
                                │ DeliveryAssignedEvent
                                │ topic: delivery.events
                                ▼
                       возвращается в order-service
                       (статус → DELIVERY_ASSIGNED)
```

### Модули

| Модуль | Порт | Назначение |
|--------|------|-----------|
| **order-service** | 8080 | REST API заказов, оркестрация оплаты, публикация `OrderPaidEvent`, приём `DeliveryAssignedEvent` |
| **payment-service** | 8081 | Обработка платежей (идемпотентно по `orderId`), симуляция успеха/отказа |
| **delivery-service** | 8082 | Назначение курьера по `OrderPaidEvent`, публикация `DeliveryAssignedEvent` |
| **common-libs** | — | Общие контракты: HTTP DTO, Kafka-события, enum'ы (`OrderStatus`, `PaymentStatus`, `PaymentMethod`) |

## Технологии

- **Java 21**, **Spring Boot 3.5.7**
- **Spring Web** — REST-контроллеры, декларативный HTTP-клиент (`@HttpExchange`)
- **Spring Kafka** — асинхронный обмен событиями
- **Spring Data JPA** + **PostgreSQL 16** — хранение
- **MapStruct 1.6** — маппинг entity ↔ DTO
- **Lombok** — сокращение boilerplate
- **springdoc-openapi** — Swagger UI
- **Gradle (Kotlin DSL)**, wrapper в комплекте

## Поток обработки заказа

1. **Создание** — `POST /api/orders`. Заказ сохраняется со статусом `PENDING_PAYMENT`, для позиций рассчитывается цена и `totalAmount`.
2. **Оплата** — `POST /api/orders/{id}/pay`. `order-service` синхронно вызывает `payment-service`. По результату статус становится `PAID` или `PAYMENT_FAILED`, после чего в топик `orders.events` публикуется `OrderPaidEvent`.
3. **Доставка** — `delivery-service` слушает `orders.events`, назначает курьера и ETA, публикует `DeliveryAssignedEvent` в `delivery.events`.
4. **Завершение** — `order-service` слушает `delivery.events` и переводит заказ в `DELIVERY_ASSIGNED`, сохраняя имя курьера и ETA.

> **Примечание (симуляция):** цены позиций генерируются случайно; оплата методом `QR` всегда завершается отказом (`PAYMENT_FAILED`), остальные методы — успехом. `payment-service` идемпотентен по `orderId`.

### Статусы

- `OrderStatus`: `PENDING_PAYMENT` → `PAID` / `PAYMENT_FAILED` → `DELIVERY_ASSIGNED` → `DELIVERED`
- `PaymentStatus`: `PAYMENT_SUCCEEDED`, `PAYMENT_FAILED`, `REFUND`
- `PaymentMethod`: `CARD`, `QR`, `YANDEX_SPLIT`

## Запуск

### Требования

- JDK 21
- Docker + Docker Compose

### 1. Поднять инфраструктуру

```bash
docker compose up -d
```

Поднимает PostgreSQL (порт `5433`, БД `orders`, `postgres/postgres`) и Kafka (порт `9092`).

### 2. Запустить сервисы

Каждый сервис — отдельное Spring Boot приложение. В трёх терминалах:

```bash
./gradlew :order-service:bootRun
./gradlew :payment-service:bootRun
./gradlew :delivery-service:bootRun
```

Или собрать всё сразу:

```bash
./gradlew build
```

## API

Swagger UI доступен у каждого сервиса по адресу `http://localhost:<port>/swagger-ui.html`.

### order-service (`:8080`)

| Метод | Путь | Описание |
|-------|------|----------|
| `POST` | `/api/orders` | Создать заказ |
| `POST` | `/api/orders/{id}/pay` | Оплатить заказ |
| `GET` | `/api/orders/{id}` | Получить заказ |

### payment-service (`:8081`)

| Метод | Путь | Описание |
|-------|------|----------|
| `POST` | `/api/payments` | Создать платёж (вызывается order-service) |

### Пример

```bash
# Создать заказ
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": 1,
    "address": "ул. Пушкина, д. 1",
    "items": [{ "productId": 42, "quantity": 2 }]
  }'

# Оплатить (id из ответа выше)
curl -X POST http://localhost:8080/api/orders/1/pay \
  -H "Content-Type: application/json" \
  -d '{ "paymentMethod": "CARD" }'

# Проверить статус — после обработки событий станет DELIVERY_ASSIGNED
curl http://localhost:8080/api/orders/1
```

## Kafka-топики

| Топик | Продюсер | Консьюмер | Событие |
|-------|----------|-----------|---------|
| `orders.events` | order-service | delivery-service | `OrderPaidEvent` |
| `delivery.events` | delivery-service | order-service | `DeliveryAssignedEvent` |

## Структура проекта

```
order-platform/
├── common-libs/        # общие DTO, события, enum'ы (fedor.dev.api.*)
├── order-service/      # api / domain / external (HTTP) / kafka
├── payment-service/    # api / domain
├── delivery-service/   # domain / kafka
├── docker-compose.yaml # PostgreSQL + Kafka
└── settings.gradle.kts
```

Внутри сервисов используется послойная организация: `api` (контроллеры) → `domain` (бизнес-логика, JPA) → `external`/`kafka` (интеграции).

## Конфигурация

Основные настройки в `application.yml` каждого сервиса:

- **БД:** `jdbc:postgresql://localhost:5433/orders`, `ddl-auto: update`
- **Kafka:** `localhost:9092`
- **order → payment:** `payment-service.base-url: http://localhost:8081`

`payment-service` читает креды БД из переменных окружения `POSTGRES_USERNAME` / `POSTGRES_PASSWORD` (по умолчанию `postgres`).
