# PaperLeaf

PaperLeaf — Telegram-бот для создания интерактивных сюжетных графов и разветвлённых диалогов.

Проект использует собственную архитектуру конечного автомата и графовую базу данных Neo4j для построения гибких сюжетных путей, вариантов выбора и связанных узлов истории.

---

## Запуск бота 🚀

### 1. Получение Telegram Bot Token

Создайте бота через Telegram-бота BotFather:

https://telegram.me/BotFather

После создания бота получите токен доступа.

---

### 2. Настройка `application.properties`

Добавьте токен бота и секретную административную команду:

```properties
telegram.bot.token=YOUR_BOT_TOKEN
admin.secret.command.cleanNeo4j=YOUR_SECRET_COMMAND
```

3. Запуск проекта

Выполните команду в корне проекта:
```bash
docker compose up -d --build
```



