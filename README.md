# PaperLeaf

PaperLeaf is a Telegram bot for creating interactive story graphs and branching dialogues.

The project uses a custom state machine architecture and Neo4j graphical databases to build flexible story paths, variants, and logical story nodes.

---

## Launching the bot 🚀

### 1. Obtaining a Telegram bot token

Create a bot using the BotFather Telegram bot:

https://telegram.me/BotFather

After creating the bot, issue an access token.

---

### 2. Configuring `application.properties`

Add the bot token and secret administrative command:

```properties
telegram.bot.token=YOUR_BOT_TOKEN
admin.secret.command.cleanNeo4j=YOUR_SECRET_COMMAND
```

3. Running the project

Run the following commands in the project root:
``` bash
docker compose -d --build
```