package leafCatcher.config;

import leafCatcher.LeafCatcher;
import leafCatcher.history.HistoryService;
import leafCatcher.service.EventMainService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Slf4j
@Configuration
public class TelegramConfiguration {

    @Value("${admin.secret.command.cleanNeo4j:}")
    private String adminCleanDb;

    @Value("${telegram.bot.token}")
    private String botToken;

    @Bean
    public TelegramClient telegramClientBean() {
        return new OkHttpTelegramClient(botToken);
    }

    @Bean
    public MessageSource messageSource() {
        ResourceBundleMessageSource source = new ResourceBundleMessageSource();
        source.setBasename("messages");
        source.setDefaultEncoding("UTF-8");
        return source;
    }


    @Bean
    public LeafCatcher leafCatcher(TelegramClient telegramClient,
                                   EventMainService eventMainService, HistoryService historyService) {
        return new LeafCatcher(telegramClient, eventMainService, historyService, adminCleanDb);
    }

    @Bean
    public TelegramBotsLongPollingApplication telegramBotsApp() {
        return new TelegramBotsLongPollingApplication();
    }

    @Bean
    public ApplicationRunner startBot(TelegramBotsLongPollingApplication botsApp,
                                      LeafCatcher bot) {
        return args -> {
            botsApp.registerBot(botToken, bot);
            log.info("✅ LeafCatcher started");
        };
    }
}
