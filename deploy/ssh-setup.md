# Настройка SSH доступа для GitHub Actions

## 1. Создание SSH ключа для GitHub Actions

На вашей локальной машине выполните:

```bash
# Создайте SSH ключ специально для GitHub Actions
ssh-keygen -t ed25519 -C "github-actions-petmatch" -f ~/.ssh/github_actions_petmatch

# Скопируйте публичный ключ
cat ~/.ssh/github_actions_petmatch.pub
```

## 2. Добавление публичного ключа на сервер

Подключитесь к серверу и добавьте публичный ключ:

```bash
# На сервере 158.160.173.155
ssh rallentando@158.160.173.155

# Создайте директорию для SSH ключей (если не существует)
mkdir -p ~/.ssh
chmod 700 ~/.ssh

# Добавьте публичный ключ в authorized_keys
echo "ВАШ_ПУБЛИЧНЫЙ_КЛЮЧ_ЗДЕСЬ" >> ~/.ssh/authorized_keys
chmod 600 ~/.ssh/authorized_keys
```

## 3. Настройка GitHub Secrets

В настройках вашего GitHub репозитория добавьте следующие secrets:

### Repository Settings → Secrets and variables → Actions

1. **PRODUCTION_HOST**: `158.160.173.155`
2. **PRODUCTION_USER**: `rallentando`
3. **PRODUCTION_SSH_KEY**: Содержимое приватного ключа `~/.ssh/github_actions_petmatch`
4. **DOCKER_USERNAME**: Ваш Docker Hub username
5. **DOCKER_PASSWORD**: Ваш Docker Hub password/token

## 4. Тестирование SSH подключения

Проверьте подключение с вашей локальной машины:

```bash
ssh -i ~/.ssh/github_actions_petmatch root@158.160.173.155
```