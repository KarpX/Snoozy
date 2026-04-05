# Как совершать запрос
#### Все эндпоинты требуют header `Authorization: Bearer <token>`
#### Например
```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI0IiwiZW1haWwiOiJhYXNzYWRzYWRzYUBnbWFpbC5jb20iLCJuYW1lIjoidGVzdF91c2VyIiwiYXV0aCI6IkJBU0lDIiwiaWF0IjoxNzc1MTI2NDE0LCJleHAiOjE3NzYwODY0MTR9.3FilXDth0Zc2TwzeQlgkAmLjSvoc4JEvEYsSkVce3us
```
#### Под token подразумевается JWT accessToken, который выдается при авторизации

# Получение информации о группе
### GET `/api/v1/groups/{id}`
#### id - айди группы
#### Request требует header Authorization

#### Response (200 OK)
```
{
  "id": 1,
  "name": "Some group",
  "ownerId": 4,
  "url": null,
  "members": [
    {
      "id": 4,
      "username": "test_user",
      "avatarUrl": "http://localhost:8080/api/v1/files/avatar?key=users/avatars/4/0e69bf86-f650-4785-92e2-123b6dfe4f61.png"
    },
    {
      "id": 2,
      "username": "Dmitry",
      "avatarUrl": "https://lh3.googleusercontent.com/a/ACg8ocJOYsJolSEeCW9Ejr_LUaIS1qSP3QcUK-LEzomSyJkJnxX_tg=s96-c"
    },
    {
      "id": 1,
      "username": "user",
      "avatarUrl": "https://lh3.googleusercontent.com/a/ACg8ocLQwi0c9RemDCo-Nwv-BsihYNfgOK2wg5KQOYZRmaj2HSOG5IxH=s96-c"
    }
  ]
}
```


# Получить список групп пользователя
### GET `/api/v1/groups`
#### Request требует header Authorization

#### Response (200 OK)
```
[
  {
    "id": 10,
    "name": "Morning Club",
    "ownerId": 4,
    "url": null,
    "members": [
      {
        "id": 4,
        "username": "test_user",
        "avatarUrl": "http://localhost:8080/api/v1/files/avatar?key=users/avatars/4/0e69bf86-f650-4785-92e2-123b6dfe4f61.png"
      },
    {
      "id": 2,
      "username": "Dmitry",
      "avatarUrl": "https://lh3.googleusercontent.com/a/ACg8ocJOYsJolSEeCW9Ejr_LUaIS1qSP3QcUK-LEzomSyJkJnxX_tg=s96-c"
    }
    ]
  },
  {
    "id": 11,
    "name": "Morning Club2",
    "ownerId": 2,
    "url": null,
    "members": [
      {
        "id": 2,
        "username": "Dmitry",
        "avatarUrl": "https://lh3.googleusercontent.com/a/ACg8ocJOYsJolSEeCW9Ejr_LUaIS1qSP3QcUK-LEzomSyJkJnxX_tg=s96-c"
      }
    ]
  },
]
```


# Создать группу
### POST `/api/v1/groups`
#### Request требует header Authorization

#### Request (application-json)
```
{
  "name": "My group",
  "membersId": [1, 2]
}
``` 

#### Response (200 OK)
```
{
  "id": 4,
  "name": "My group",
  "ownerId": 3,
  "url": null,
  "members": [
    {
      "id": 3,
      "username": "test_user",
      "avatarUrl": "http://localhost:8080/api/v1/files/avatar?key=users/avatars/4/0e69bf86-f650-4785-92e2-123b6dfe4f61.png"
    },
    {
      "id": 2,
      "username": "Dmitry",
      "avatarUrl": "https://lh3.googleusercontent.com/a/ACg8ocJOYsJolSEeCW9Ejr_LUaIS1qSP3QcUK-LEzomSyJkJnxX_tg=s96-c"
    },
    {
      "id": 1,
      "username": "Dmitry (user •_•)",
      "avatarUrl": "https://lh3.googleusercontent.com/a/ACg8ocLQwi0c9RemDCo-Nwv-BsihYNfgOK2wg5KQOYZRmaj2HSOG5IxH=s96-c"
    }
  ]
}
```


# Загрузить аватарку группы
### POST `/api/v1/groups/{id}`
#### id - айди группы
#### Request требует header Authorization
#### Загрузка аватарки через MULTIPART_FORM
##### key = file
##### value = avatar.png

#### Response (200 OK)
```
{
  "url": "http://localhost:8080/api/v1/files/avatar?key=groups/avatars/4/82415c3e-0af8-49c0-ad5d-5f7808a92cd1.png"
}
```