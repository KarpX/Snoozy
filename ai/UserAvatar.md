# План реализации загрузки и отображения аватара пользователя

## 1. Загрузка аватара на странице профиля (`ProfileScreen.kt`)

### 1.1. Обновление `ApiService.kt`
- Добавить метод для загрузки аватара:
  ```kotlin
  @Multipart
  @POST("api/v1/users/avatar")
  suspend fun uploadUserAvatar(@Part file: MultipartBody.Part): Response<AvatarResponse>
  ```
- Добавить метод для получения аватара (как `ResponseBody` для прямого отображения или скачивания):
  ```kotlin
  @GET("api/v1/users/avatar")
  suspend fun getUserAvatar(): Response<ResponseBody>
  ```

### 1.2. Создание `ProfileViewModel.kt`
- Создать ViewModel для управления состоянием профиля.
- Реализовать функцию `uploadAvatar(uri: Uri)`, которая:
    1. Копирует файл во внутреннее хранилище.
    2. Создает `MultipartBody.Part`.
    3. Вызывает `apiService.uploadUserAvatar`.
    4. Обновляет локальное состояние или сохраняет URL в `UserPreferencesManager`.
- Реализовать загрузку текущего аватара при старте.

### 1.3. Обновление `ProfileScreen.kt`
- Интегрировать `ProfileViewModel`.
- Добавить `rememberLauncherForActivityResult` с `GetContent()` для выбора изображения из галереи.
- Сделать иконку аватара кликабельной (`Modifier.clickable`), вызывающей лаунчер.
- Заменить статичную иконку на `AsyncImage` из Coil для отображения загруженного аватара.
- Добавить индикатор загрузки (`CircularProgressIndicator`) поверх аватара во время выполнения запроса.

---

## 2. Отображение аватаров в группах и списках

### 2.1. Обновление `GroupsScreen.kt`
- Убедиться, что `Member` модель в `GroupItem` корректно содержит `avatarLink`.
- Проверить, что при получении списка групп ссылки на аватары актуальны.

### 2.2. Обновление `MissedAlarmItem.kt` и `UpcomingAlarmItem.kt`
- В данных компонентах уже используется `AsyncImage` для `avatarLink`.
- Настроить `headers` в Coil (через `ImageLoader` или `AsyncImage(model = ImageRequest...)`), если сервер требует заголовок `Authorization` даже для GET-запроса картинки.
- Добавить `placeholder` и `error` картинки для `AsyncImage` (например, `R.drawable.ic_no_avatar`).

---

## 3. Технические детали
- **Авторизация**: Заголовок `Authorization: Bearer <token>` будет добавляться автоматически через существующий `AuthInterceptor`.
- **Обработка Multipart**: Использовать `ContentResolver` для получения `InputStream` из `Uri` и записи во временный файл перед отправкой.
- **Кеширование**: Coil автоматически закеширует изображения по URL. Если URL не меняется при обновлении аватара, добавить `parameter` с таймстампом к URL (cache busting).
