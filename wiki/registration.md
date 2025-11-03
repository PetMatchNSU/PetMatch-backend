# Registration API

**Summary:**  
Registers a new user via email, sends a verification code to the provided address, and returns JWT tokens.
---

## Request

**Method:** `POST`  
**URL:** `/api/v1/user/register`  
**Content-Type:** `application/json`

Описание параметров:

- "email" - email пользователя
- "password" - пароль
- "firstName" - Имя
- "secondName" - Фамилия
- "lastName" - Отчество
- "gender" - гендер M/F
- "region" - регион проживания пользователя
- "city" - город проживания пользователя
- "bondTime" - массив времени для связь
- "bondTimeStart" - начало временного интервала
- "bondTimeEnd" - конец временного интервала
- "contactInfo" - массив способов связи
- "type" - тип связи, возможные варианты PHONE/EMAIL/TELEGRAM/VK
- "contact" - ссылка, номер или email
- "visible" - флаг отображения конкретного типа связи

Тело запроса:

```json
{
  "email": "newuser@example.com",
  "password": "Password123",
  "firstName": "Тестовик ",
  "secondName": "Тестович",
  "lastName": "Тестер",
  "gender": "M",
  "region": "Новосибирская область",
  "city": "Новосибирск",
  "bondTime": [
    {
      "bondTimeStart": "10:00",
      "bondTimeEnd": "12:00"
    },
    {
      "bondTimeStart": "16:00",
      "bondTimeEnd": "18:00"
    }
  ],
  "contactInfo": [
    {
      "type": "VK",
      "contact": "https://vk.com/t.test",
      "visible": true
    }
  ]
}
```

Тело ответа 200 OK:

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "isEmailVerified": false
  }
}
```

Тело ответа 409 (Conflict):

```json
{
  "message": "Пользователь с таким email уже существует"
}
```