# План реализации загрузки контактов в AddMembersScreen

Этот документ описывает шаги по интеграции реальных контактов Android в экран выбора участников группы.

## 1. Конфигурация разрешений (Permissions)
- [ ] Добавить в `AndroidManifest.xml`: `<uses-permission android:name="android.permission.READ_CONTACTS" />`.
- [ ] В `AddMembersScreen.kt` внедрить запрос разрешения (Runtime Permission) через `ActivityResultLauncher` или библиотеку Accompanist.

## 2. Слой данных (Data & Domain)
- [ ] **Entity**: Создать `ContactItem` (id, name, phoneNumber, photoUri, isSelected).
- [ ] **Repository**: Создать интерфейс `ContactRepository` с методом `fetchContacts(): Flow<List<ContactItem>>`.
- [ ] **Implementation**: Реализовать репозиторий, используя `ContentResolver` и `ContactsContract.CommonDataKinds.Phone`.
- [ ] **DI**: Зарегистрировать репозиторий в Hilt модуле.

## 3. Слой логики (ViewModel)
- [ ] Создать `AddMembersViewModel` по аналогии с `AddAlarmViewModel`.
- [ ] Добавить `StateFlow` для управления:
    - `contacts`: полный список.
    - `searchText`: строка поиска.
    - `isLoading`: состояние загрузки.
- [ ] Реализовать функции:
    - `toggleSelection(contactId)`: выбор/отмена выбора контакта.
    - `onSearchQueryChanged(query)`: фильтрация списка.
    - `getSelectedContacts()`: сбор данных для перехода на `NewGroupScreen`.

## 4. Слой интерфейса (UI - AddMembersScreen.kt)
- [ ] **MembersSearchBar**: Подключить к `viewModel.searchText`.
- [ ] **MembersList**:
    - Заменить `Box` с текстом "Контакты не найдены" на `LazyColumn`.
    - Создать Composable `ContactRow` с аватаром (или инициалами), именем и Checkbox/RadioButton.
    - Отображать состояние "Загрузка" (CircularProgressIndicator) или "Ничего не найдено".
- [ ] **SelectedMembers**: Реализовать горизонтальный список аватарок выбранных пользователей над основным списком.

## 5. Навигация и передача данных
- [ ] При клике на `AddMembersButton` во `ViewModel` формируется список ID или объектов выбранных участников.
- [ ] Передать эти данные в `NewGroupScreen` через `navController` (через аргументы или SharedViewModel).
- [ ] В `NewGroupScreen` отобразить полученный список в компоненте `GroupMembersInNewGroup`.

## Оптимизация
- [ ] Реализовать кеширование списка контактов, чтобы не запрашивать их при каждом повороте экрана.
- [ ] Добавить обработку случая "Доступ запрещен навсегда" с кнопкой "Перейти в настройки".