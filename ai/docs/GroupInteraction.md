## Base URL

```
/api/v1/alarms
```

---

## Содержание

- [Модель данных](#модель-данных)
- [Эндпоинты](#эндпоинты)
  - [Получить свои будильники](#получить-свои-будильники)
  - [Получить будильники пользователя](#получить-будильники-пользователя)
  - [Создать будильник](#создать-будильник)
  - [Обновить будильник](#обновить-будильник)
  - [Удалить будильник](#удалить-будильник)
  - [Получить выданные права](#получить-выданные-права)
  - [Выдать право пользователю](#выдать-право-пользователю)
  - [Удалённо запустить будильник](#удалённо-запустить-будильник)
  - [Удалённо включить будильник](#удалённо-включить-будильник)
  - [Удалённо выключить будильник](#удалённо-выключить-будильник)
  - [Получить историю входящих действий](#получить-историю-входящих-действий)
  - [Проверка состояния сервиса](#проверка-состояния-сервиса)
- [Справочник значений](#справочник-значений)

---

## Модель данных

### `Alarm` — Будильник

| Поле | Тип | Описание |
|---|---|---|
| `id` | Long | Идентификатор будильника |
| `ownerId` | Long | Идентификатор владельца |
| `title` | String | Название будильника |
| `alarmTime` | DateTime | Дата и время срабатывания |
| `enabled` | Boolean | Включён ли будильник |
| `repeatDays` | List\<Enum\> | Дни недели для повторения |
| `soundName` | String | Название звука |
| `difficultyLevel` | Int | Уровень сложности выключения |
| `isOverslept` | Boolean | Был ли будильник пропущен |
| `createdAt` | DateTime | Время создания |
| `updatedAt` | DateTime | Время последнего обновления |

**`repeatDays`** — допустимые значения: `MON`, `TUE`, `WED`, `THU`, `FRI`, `SAT`, `SUN`

```json
"repeatDays": ["MON", "WED", "FRI"]
```

> Если `repeatDays` пустой — будильник считается одноразовым.

---

## Эндпоинты

### Получить свои будильники

```http
GET /api/v1/alarms
```

Возвращает список будильников текущего пользователя.

**Ответ `200 OK`**
```json
[
  {
    "id": 1,
    "ownerId": 1,
    "title": "Подъем",
    "alarmTime": "2026-04-05T07:30:00",
    "enabled": true,
    "repeatDays": ["MON", "WED", "FRI"],
    "soundName": "classic",
    "difficultyLevel": 2,
    "isOverslept": false
  }
]
```

**Ошибки**

| Код | Описание |
|---|---|
| `401 Unauthorized` | Пользователь не определён |

---

### Получить будильники пользователя

```http
GET /api/v1/alarms/users/{userId}
```

Возвращает список будильников указанного пользователя.

**Ответ `200 OK`**
```json
[
  {
    "id": 4,
    "ownerId": 15,
    "title": "Подъем",
    "alarmTime": "2026-04-05T07:30:00",
    "enabled": true,
    "repeatDays": ["MON", "WED", "FRI"],
    "soundName": "classic",
    "difficultyLevel": 2,
    "isOverslept": false
  }
]
```

**Ошибки**

| Код | Описание |
|---|---|
| `401 Unauthorized` | Пользователь не определён |
| `404 Not Found` | Пользователь не найден |

---

### Создать будильник

```http
POST /api/v1/alarms
```

Создаёт новый будильник текущего пользователя.

**Тело запроса**
```json
{
  "title": "Подъем",
  "alarmTime": "2026-04-05T07:30:00",
  "enabled": true,
  "repeatDays": ["MON", "WED", "FRI"],
  "soundName": "classic",
  "difficultyLevel": 2
}
```

> **Обязательные поля:** `title`, `alarmTime`

**Ответ `200 OK`**
```json
{
  "id": 1,
  "ownerId": 1,
  "title": "Подъем",
  "alarmTime": "2026-04-05T07:30:00",
  "enabled": true,
  "repeatDays": ["MON", "WED", "FRI"],
  "soundName": "classic",
  "difficultyLevel": 2,
  "isOverslept": false
}
```

**Ошибки**

| Код | Описание |
|---|---|
| `400 Bad Request` | Отсутствует `title` или `alarmTime` |
| `401 Unauthorized` | Пользователь не определён |

---

### Обновить будильник

```http
PATCH /api/v1/alarms/{alarmId}
```

Обновляет существующий будильник текущего пользователя. Можно передавать только изменяемые поля.

**Тело запроса**
```json
{
  "title": "Подъем на работу",
  "enabled": false,
  "repeatDays": ["MON", "TUE", "WED", "THU", "FRI"],
  "isOverslept": false
}
```

**Ответ `200 OK`**
```json
{
  "id": 1,
  "ownerId": 1,
  "title": "Подъем на работу",
  "alarmTime": "2026-04-05T07:30:00",
  "enabled": false,
  "repeatDays": ["MON", "TUE", "WED", "THU", "FRI"],
  "soundName": "classic",
  "difficultyLevel": 2,
  "isOverslept": false
}
```

**Ошибки**

| Код | Описание |
|---|---|
| `401 Unauthorized` | Пользователь не определён |
| `403 Forbidden` | Будильник не принадлежит текущему пользователю |
| `404 Not Found` | Будильник не найден |

---

### Удалить будильник

```http
DELETE /api/v1/alarms/{alarmId}
```

Удаляет будильник текущего пользователя.

**Ответ `204 No Content`**

**Ошибки**

| Код | Описание |
|---|---|
| `401 Unauthorized` | Пользователь не определён |
| `403 Forbidden` | Будильник не принадлежит текущему пользователю |
| `404 Not Found` | Будильник не найден |

---

### Получить выданные права

```http
GET /api/v1/alarms/permissions
```

Возвращает список прав, которые текущий пользователь выдал другим пользователям.

**Ответ `200 OK`**
```json
[
  {
    "id": 1,
    "ownerId": 1,
    "targetUserId": 2,
    "permissionType": "TRIGGER",
    "active": true,
    "createdAt": "2026-04-04T20:00:00"
  }
]
```

**Ошибки**

| Код | Описание |
|---|---|
| `401 Unauthorized` | Пользователь не определён |

---

### Выдать право пользователю

```http
POST /api/v1/alarms/permissions
```

Выдаёт другому пользователю право на взаимодействие с будильниками владельца.

**Тело запроса**
```json
{
  "targetUserId": 2,
  "permissionType": "TRIGGER"
}
```

**Ответ `200 OK`**
```json
{
  "id": 1,
  "ownerId": 1,
  "targetUserId": 2,
  "permissionType": "TRIGGER",
  "active": true,
  "createdAt": "2026-04-04T20:00:00"
}
```

**Ошибки**

| Код | Описание |
|---|---|
| `400 Bad Request` | Отсутствует `targetUserId` или `permissionType` |
| `401 Unauthorized` | Пользователь не определён |

---

### Удалённо запустить будильник

```http
POST /api/v1/alarms/{alarmId}/trigger
```

Позволяет другому пользователю удалённо запустить чужой будильник. Требует право `TRIGGER`.

**Тело запроса**
```json
{
  "messageText": "Вставай, опоздаешь"
}
```

**Ответ `200 OK`**
```json
{
  "id": 1,
  "alarmId": 1,
  "actorUserId": 2,
  "targetUserId": 1,
  "actionType": "TRIGGER_NOW",
  "status": "EXECUTED",
  "executedAt": "2026-04-04T20:10:00",
  "messageText": "Вставай, опоздаешь"
}
```

**Ошибки**

| Код | Описание |
|---|---|
| `401 Unauthorized` | Пользователь не определён |
| `403 Forbidden` | Нет права `TRIGGER` |
| `404 Not Found` | Будильник не найден |

---

### Удалённо включить будильник

```http
POST /api/v1/alarms/{alarmId}/enable
```

Позволяет другому пользователю включить чужой будильник. Требует право `ENABLE_DISABLE`.

**Ответ `200 OK`**
```json
{
  "id": 2,
  "alarmId": 1,
  "actorUserId": 2,
  "targetUserId": 1,
  "actionType": "ENABLE",
  "status": "EXECUTED",
  "executedAt": "2026-04-04T20:12:00",
  "messageText": null
}
```

**Ошибки**

| Код | Описание |
|---|---|
| `401 Unauthorized` | Пользователь не определён |
| `403 Forbidden` | Нет права `ENABLE_DISABLE` |
| `404 Not Found` | Будильник не найден |

---

### Удалённо выключить будильник

```http
POST /api/v1/alarms/{alarmId}/disable
```

Позволяет другому пользователю выключить чужой будильник. Требует право `ENABLE_DISABLE`.

**Ответ `200 OK`**
```json
{
  "id": 3,
  "alarmId": 1,
  "actorUserId": 2,
  "targetUserId": 1,
  "actionType": "DISABLE",
  "status": "EXECUTED",
  "executedAt": "2026-04-04T20:13:00",
  "messageText": null
}
```

**Ошибки**

| Код | Описание |
|---|---|
| `401 Unauthorized` | Пользователь не определён |
| `403 Forbidden` | Нет права `ENABLE_DISABLE` |
| `404 Not Found` | Будильник не найден |

---

### Получить историю входящих действий

```http
GET /api/v1/alarms/actions/incoming
```

Возвращает историю действий, совершённых над будильниками текущего пользователя.

**Ответ `200 OK`**
```json
[
  {
    "id": 1,
    "alarmId": 1,
    "actorUserId": 2,
    "targetUserId": 1,
    "actionType": "TRIGGER_NOW",
    "status": "EXECUTED",
    "executedAt": "2026-04-04T20:10:00",
    "messageText": "Вставай, опоздаешь"
  }
]
```

**Ошибки**

| Код | Описание |
|---|---|
| `401 Unauthorized` | Пользователь не определён |

---

### Проверка состояния сервиса

```http
GET /api/v1/alarms/health
```

Проверяет, что сервис запущен и отвечает.

**Ответ `200 OK`**
```json
{
  "status": "ok",
  "module": "snoozy-alarm"
}
```

---

## Справочник значений

### `repeatDays`

| Значение | День |
|---|---|
| `MON` | Понедельник |
| `TUE` | Вторник |
| `WED` | Среда |
| `THU` | Четверг |
| `FRI` | Пятница |
| `SAT` | Суббота |
| `SUN` | Воскресенье |

### `permissionType`

| Значение | Описание |
|---|---|
| `TRIGGER` | Удалённо запустить будильник |
| `ENABLE_DISABLE` | Включать/выключать будильник |
| `CREATE_ALARM` | Создавать будильники |
| `UPDATE_ALARM` | Обновлять будильники |

### `actionType`

| Значение | Описание |
|---|---|
| `TRIGGER_NOW` | Немедленный запуск |
| `ENABLE` | Включение |
| `DISABLE` | Выключение |

### `status`

| Значение | Описание |
|---|---|
| `CREATED` | Создано |
| `EXECUTED` | Выполнено |
| `REJECTED` | Отклонено |
| `FAILED` | Ошибка |
