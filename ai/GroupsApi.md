# План реализации Groups API

Этот документ описывает шаги по интеграции функционала групп с бэкендом на основе [документации](docs/groups.md).

## 1. Подготовка сетевого слоя
- [ ] **Добавить Interceptor для авторизации**: 
    - Создать `AuthInterceptor` в `com.wem.snoozy.data.remote`.
    - Извлекать токен из `UserPreferencesManager` и добавлять в заголовок `Authorization: Bearer <token>`.
- [ ] **Обновить ApiFactory/NetworkModule**:
    - Зарегистрировать `AuthInterceptor` в `OkHttpClient`.
- [ ] **Определить DTO (Data Transfer Objects)** в `com.wem.snoozy.data.remote.dto`:
    - `GroupResponse`: для получения данных о группе (id, name, ownerId, members).
    - `MemberDto`: для данных об участнике (id, username, avatarUrl).
    - `CreateGroupRequest`: `{ name: String, membersId: List<Int> }`.
    - `AvatarResponse`: `{ url: String }`.

## 2. Обновление ApiService.kt
Добавить следующие эндпоинты:
- `GET api/v1/groups`: получение списка групп.
- `GET api/v1/groups/{id}`: получение детальной информации о группе.
- `POST api/v1/groups`: создание новой группы.
- `POST api/v1/groups/{id}`: загрузка аватарки (Multipart).

## 3. Слой Domain (Репозиторий и Use Cases)
- [ ] **Обновить GroupItem**:
    - Привести структуру в соответствие с API (добавить `ownerId`, изменить тип участников).
- [ ] **Создать/Обновить GroupsRepository**:
    - Методы: `getGroups()`, `getGroupById(id)`, `createGroup(name, memberIds)`, `uploadGroupAvatar(groupId, file)`.
- [ ] **Реализовать Use Cases**:
    - `GetGroupsUseCase`
    - `CreateGroupUseCase`

## 4. Слой Data (Реализация репозитория)
- [ ] **GroupsRepositoryImpl**:
    - Реализовать сетевые вызовы через `ApiService`.
    - Добавить маппинг из DTO в Domain модели.
    - (Опционально) Кэширование в Room для оффлайн режима.

## 5. Слой Presentation (ViewModel & UI)
- [ ] **GroupsViewModel**:
    - Загрузка списка групп при старте экрана.
    - Обработка состояний Loading/Success/Error.
- [ ] **AddMembers / NewGroup Flow**:
    - Поиск пользователей по API (если предусмотрено) или использование контактов.
    - Вызов `createGroup` при нажатии "Создать".
    - После создания группы — вызов загрузки аватарки, если она была выбрана.
- [ ] **Обновление UI**:
    - Отображение реальных аватарок участников через `AsyncImage` (Coil).
    - Обработка клика по группе для перехода в детали.

## 6. Тестирование
- [ ] Проверить создание группы с разными наборами участников.
- [ ] Проверить корректность обработки истекшего токена (401 Error).
- [ ] Проверить загрузку изображений разных форматов.
