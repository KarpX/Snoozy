# Реализация сервиса авторизации (Retrofit + Hilt)

Этот документ содержит пошаговый план внедрения регистрации и входа в приложение Snoozy с использованием удаленного API.

## 1. Подготовка инфраструктуры (Data Layer)

### 1.1. Создание DTO (Data Transfer Objects)
В пакете `com.wem.snoozy.data.remote.dto` создать необходимые data-классы:
- `RegisterRequest`: поля `username`, `phoneNumber`, `password`, `confirmPassword`.
- `LoginRequest`: поля `phoneNumber`, `password`.
- `AuthResponse`: поле `accessToken`.

### 1.2. Определение API интерфейса
Создать `com.wem.snoozy.data.remote.ApiService`:kotlin interface ApiService { @POST("api/v1/auth/basic/register") suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>}


### 1.3. Настройка Hilt (NetworkModule)
Создать `com.wem.snoozy.di.NetworkModule` для предоставления зависимостей Retrofit:
- Base URL: `http://45.156.22.247:8081/`
- Converter: `GsonConverterFactory`
- Client: `OkHttpClient` (опционально с логированием через `HttpLoggingInterceptor`).

## 2. Слой репозитория (Domain & Data)

### 2.1. Интерфейс репозитория
Создать `AuthRepository` с методами `login` и `register`, возвращающими `Result<AuthResponse>`.

### 2.2. Реализация репозитория
В `AuthRepositoryImpl` реализовать вызовы к `ApiService`. При успешном ответе сохранять полученный `accessToken` в `UserPreferencesManager` (DataStore).

## 3. Слой логики (ViewModel)

### 3.1. Создание AuthViewModel
Разработать ViewModel для `AuthActivity`:
- **State**: `AuthUiState` (Idle, Loading, Success, Error).
- **Functions**: `onLoginClick(phone, pass)` и `onRegisterClick(username, phone, pass, confirm)`.
- **Validation**: Проверка полей на пустоту и совпадение паролей перед отправкой запроса.

## 4. Интеграция с UI (Presentation Layer)

### 4.1. LoginScreen.kt
- Подключить `AuthViewModel` через `hiltViewModel()`.
- Обновить кнопку "Войти": вызывать `viewModel.login()`.
- Добавить визуальную индикацию загрузки при состоянии `Loading`.
- При состоянии `Success` вызывать callback `onLoginSuccess`.

### 4.2. RegistrationScreen.kt
- Подключить `AuthViewModel`.
- Обновить кнопку "Создать": вызывать `viewModel.register()`.
- Добавить обработку ошибок (например, если пользователь уже существует).

### 4.3. AuthActivity.kt
- Обеспечить навигацию между `LoginScreen` и `RegistrationScreen`.
- В методе `navigateToMain()` убедиться, что стек переходов очищается (`FLAG_ACTIVITY_CLEAR_TASK`).

## 5. Безопасность и сессия
- [ ] Реализовать проверку токена при запуске `AuthActivity`: если токен уже есть в DataStore, сразу переходить в `MainActivity`.
- [ ] Добавить `AuthInterceptor` для автоматической подстановки заголовка `Authorization: Bearer <token>` в будущие запросы к API (для работы с группами и будильниками).