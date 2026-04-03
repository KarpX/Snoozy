# План реализации AuthActivity (Login & Registration)

Этот план описывает процесс выноса экранов входа и регистрации в отдельное Activity, которое будет являться точкой входа в приложение.

## 1. Изменения в навигации (Screen.kt)
- [ ] Проверить наличие маршрутов `Screen.Login` и `Screen.AddMembers` (используется как Registration).
- [ ] Убедиться, что константы маршрутов уникальны.

## 2. Создание AuthActivity.kt
- [ ] Создать файл в пакете `com.wem.snoozy.presentation.activity`.
- [ ] Реализовать класс `AuthActivity : ComponentActivity()`.
- [ ] Добавить аннотацию `@AndroidEntryPoint` для поддержки Hilt.
- [ ] В `onCreate` вызвать `enableEdgeToEdge()` и установить `setContent { SnoozyTheme { ... } }`.
- [ ] Внутри темы инициализировать `val authNavController = rememberNavController()`.

## 3. Настройка графа авторизации в AuthActivity
- [ ] Создать `NavHost` с `startDestination = Screen.Login.route`.
- [ ] Добавить `composable(Screen.Login.route)`:
    - Передать в `LoginScreen` лямбду `onLoginSuccess`, которая запускает `MainActivity` через Intent.
    - Передать лямбду `onRegisterClick`, которая делает `authNavController.navigate(Screen.AddMembers.route)`.
- [ ] Добавить `composable(Screen.AddMembers.route)`:
    - Передать в `RegistrationScreen` лямбду `onBackClick`, которая делает `authNavController.popBackStack()`.
    - Передать лямбду `onRegistrationSuccess`, которая также запускает `MainActivity`.

## 4. Логика перехода (Intent)
- [ ] Реализовать в `AuthActivity` функцию для перехода:
  kotlin private fun navigateToMain() { val intent = Intent(this, MainActivity::class.java) intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK startActivity(intent) finish() }

## 5. Обновление UI экранов
- [ ] **LoginScreen.kt**:
    - Добавить параметры: `onLoginSuccess: () -> Unit` и `onRegisterClick: () -> Unit`.
    - Привязать `onLoginSuccess` к кнопке "Войти".
    - Привязать `onRegisterClick` к тексту "Зарегистрироваться" внизу экрана.
    - Добавить `Modifier.verticalScroll(rememberScrollState())` к основной колонке.
- [ ] **RegistrationScreen.kt**:
    - Добавить параметры: `onBackClick: () -> Unit` и `onRegistrationSuccess: () -> Unit`.
    - Привязать `onBackClick` к иконке стрелки в `TopAppBar` и к тексту "Войти" внизу.
    - Привязать `onRegistrationSuccess` к кнопке "Создать".
    - Заменить фиксированный `Spacer` перед ссылкой на `Modifier.weight(1f)` (внутри Column со скроллом).

## 6. Изменения в MainActivity.kt и AppNavGraph.kt
- [ ] Удалить `Screen.Login` и `Screen.AddMembers` из `AppNavGraph.kt` (они теперь в `AuthActivity`).
- [ ] В `AppNavGraph.kt` изменить `startDestination` на `Screen.Home.route`.
- [ ] В `MyNavItem.kt` удалить проверку `nonBarRoutes`, так как `MainActivity` теперь всегда отображается с BottomBar.

## 7. Конфигурация Manifest (AndroidManifest.xml)
- [ ] Найти `MainActivity` и убрать у неё `<intent-filter>` с `LAUNCHER`.
- [ ] Объявить `AuthActivity` как точку входа: