package com.projectsky.first_telegram_bot;

import com.projectsky.first_telegram_bot.configuration.BotProperties;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;

@Component
public class MyTelegramBot implements SpringLongPollingBot {

    private final UpdateConsumer updateConsumer;
    private final BotProperties botProperties;

    public MyTelegramBot(UpdateConsumer updateConsumer,
                         BotProperties botProperties) {
        this.updateConsumer = updateConsumer;
        this.botProperties = botProperties;
    }

    @Override
    public String getBotToken() {
        return botProperties.getToken();
    }

    @Override
    public LongPollingUpdateConsumer getUpdatesConsumer() {
        return updateConsumer;
    }
}
