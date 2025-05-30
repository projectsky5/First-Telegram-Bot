package com.projectsky.first_telegram_bot;

import com.projectsky.first_telegram_bot.configuration.BotProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Component
public class UpdateConsumer implements LongPollingSingleThreadUpdateConsumer {

    private TelegramClient telegramClient;

    public UpdateConsumer(BotProperties botProperties) {
        this.telegramClient = new OkHttpTelegramClient(botProperties.getToken());
    }

    @Override
    public void consume(Update update) {
        String text = update.getMessage().getText();
        Long chatId = update.getMessage().getChatId();
        System.out.printf("Пришло сообщение %s от %s\n",
                text,
                chatId);
        SendMessage message = SendMessage.builder()
                .text("Привет! Твое сообщение: " + text)
                .chatId(chatId)
                .build();

        try {
            telegramClient.execute(message);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}
