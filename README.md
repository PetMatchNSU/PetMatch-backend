# PetMatch-backend

### Авторизация

Для запуска сервиса требуются 2 поля окружения: JWT_SECRET_ACCESS и JWT_SECRET_REFRESH. Сделано для безопасности, ибо
переменные окружения будут ограничиваться безопасностью ОС. Можно настроить так, что переменные окружения будет видеть 
только root пользователь.

Примеры создания:
export JWT_SECRET_ACCESS=$(openssl rand -hex 32)
export JWT_SECRET_REFRESH=$(openssl rand -base64 32)
