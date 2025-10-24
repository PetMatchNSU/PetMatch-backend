# PetMatch Production Deployment Guide

## Обзор архитектуры

У вас есть два сервера:

- **Сервер тестирования**: Без публичного IP, используется для GitHub Actions тестов
- **Сервер продакшена**: `158.160.173.155` - для развертывания приложения

## Архитектура развертывания

```
GitHub Actions (тесты) → Docker Hub → Production Server (158.160.173.155)
                                    ↓
                              Nginx (порт 80)
                                    ↓
                              Docker Images (без исходного кода)
```

**На продакшен сервере находятся только:**

- Docker образы (загружаемые из Docker Hub)
- Конфигурационные файлы (docker-compose.prod.yml, .env.production)
- Скрипты развертывания

## Доступные URL после развертывания

### Основные сервисы

- **Backend API**: `http://158.160.173.155:8091/` (прямой доступ)
- **API Documentation**: `http://158.160.173.155:8091/swagger-ui.html`
- **Frontend**: `http://158.160.173.155/` (через nginx)

### Мониторинг и администрирование (прямой доступ по портам)

- **Grafana Dashboard**: `http://158.160.173.155:3000/`
- **Prometheus Metrics**: `http://158.160.173.155:9090/`
- **MinIO Console**: `http://158.160.173.155:9001/`
- **Alloy UI**: `http://158.160.173.155:9080/`

### Прямой доступ к сервисам (для разработки)

- **PostgreSQL**: `158.160.173.155:5433`
- **Redis**: `158.160.173.155:6379`
- **MinIO API**: `158.160.173.155:9000`
- **Backend Health Check**: `http://158.160.173.155:8091/actuator/health`

## Последовательность действий для развертывания

### 1. Первоначальная настройка сервера

```bash
# Скопируйте файл setup-server.sh на сервер и запустите
scp deploy/setup-server.sh root@158.160.173.155:/tmp/
ssh root@158.160.173.155 "chmod +x /tmp/setup-server.sh && /tmp/setup-server.sh"
```

### 2. Проверка файлов на сервере

После выполнения скрипта настройки на сервере уже будут созданы все необходимые файлы:

- `/opt/petmatch/docker-compose.prod.yml` - конфигурация Docker Compose
- `/opt/petmatch/.env.production` - переменные окружения
- `/opt/petmatch/deploy.sh` - скрипт развертывания
- `/opt/petmatch/start-infrastructure.sh` - скрипт запуска инфраструктуры

**Исходный код проекта НЕ копируется на сервер!**

### 3. Настройка переменных окружения

```bash
# На сервере отредактируйте файл с паролями
ssh root@158.160.173.155 "vim /opt/petmatch/.env.production"
```

### 4. Запуск инфраструктурных сервисов

```bash
# На сервере запустите инфраструктуру
ssh root@158.160.173.155 "cd /opt/petmatch && ./start-infrastructure.sh"
```

### 5. Настройка GitHub Actions

Добавьте secrets в GitHub репозиторий:

- `PRODUCTION_HOST`: `158.160.173.155`
- `PRODUCTION_USER`: `root`
- `PRODUCTION_SSH_KEY`: Приватный SSH ключ
- `DOCKER_USERNAME`: Ваш Docker Hub username
- `DOCKER_PASSWORD`: Ваш Docker Hub password

### 6. Автоматический деплой

После настройки GitHub Actions, при каждом push в `develop` или `master`:

1. Тесты запустятся на тестовом сервере
2. Если тесты пройдут, соберется Docker образ
3. Образ загрузится в Docker Hub
4. На продакшен сервере обновится только backend сервис

**Первый деплой произойдет автоматически при первом push в develop/master!**

## Автоматическое развертывание

После настройки, при каждом push в ветки `develop` или `master`:

1. GitHub Actions запустит тесты на сервере тестирования
2. Если тесты пройдут, соберет Docker образ
3. Загрузит образ в Docker Hub
4. Подключится к продакшен серверу по SSH
5. Запустит скрипт развертывания с новым тегом
6. Обновит только backend сервис, оставив инфраструктуру работать

## Резервное копирование

### База данных PostgreSQL

```bash
# Создание бэкапа
ssh root@158.160.173.155 "docker exec petmatch-postgres pg_dump -U petmatch petmatch > /opt/petmatch/backup_$(date +%Y%m%d_%H%M%S).sql"

# Восстановление из бэкапа
ssh root@158.160.173.155 "docker exec -i petmatch-postgres psql -U petmatch petmatch < /opt/petmatch/backup_file.sql"
```

### Docker volumes

```bash
# Создание бэкапа volumes
ssh root@158.160.173.155 "docker run --rm -v petmatch_postgres_data:/data -v /opt/petmatch/backups:/backup alpine tar czf /backup/postgres_data_$(date +%Y%m%d_%H%M%S).tar.gz -C /data ."
```

## Устранение неполадок

### Проблемы с деплоем

```bash
# Проверка статуса сервисов
ssh root@158.160.173.155 "cd /opt/petmatch && docker-compose -f docker-compose.prod.yml ps"

# Перезапуск сервиса
ssh root@158.160.173.155 "cd /opt/petmatch && docker-compose -f docker-compose.prod.yml restart petmatch-service"

# Полный перезапуск
ssh root@158.160.173.155 "cd /opt/petmatch && docker-compose -f docker-compose.prod.yml down && docker-compose -f docker-compose.prod.yml up -d"
```

### Проблемы с nginx

```bash
# Проверка конфигурации nginx
ssh root@158.160.173.155 "nginx -t"

# Перезагрузка nginx
ssh root@158.160.173.155 "systemctl reload nginx"

# Просмотр логов nginx
ssh root@158.160.173.155 "tail -f /var/log/nginx/error.log"
```
