package com.projectsky.first_telegram_bot;

import com.projectsky.first_telegram_bot.configuration.BotProperties;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class UpdateConsumer implements LongPollingSingleThreadUpdateConsumer {

    private final TelegramClient telegramClient;

    public UpdateConsumer(BotProperties botProperties) {
        this.telegramClient = new OkHttpTelegramClient(botProperties.getToken());
    }

    @Override
    public void consume(Update update) {
        if(update.hasMessage()){
            String messageText = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();

            if(messageText.equals("/start")){
                sendMainMenu(chatId);
            } else {
                sendMessage(chatId, "Я вас не понимаю");
            }
        } else if(update.hasCallbackQuery()){
            handleCallbackQuery(update.getCallbackQuery());
        }
    }

    private void handleCallbackQuery(CallbackQuery callbackQuery) {
        String data = callbackQuery.getData();
        Long chatId = callbackQuery.getFrom().getId();
        User user = callbackQuery.getFrom();
        switch(data){
            case "my_name" -> sendMyName(chatId, user);
            case "random" -> sendRandom(chatId);
            case "get_random_photo" -> sendImage(chatId);
            default -> sendMessage(chatId, "Неизвестная команда");
        }
    }

    private void sendMessage(Long chatId, String messageText) {
        SendMessage message = SendMessage.builder()
                .text(messageText)
                .chatId(chatId)
                .build();

        try {
            telegramClient.execute(message);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendImage(Long chatId) {
        sendMessage(chatId, "Подождите, происходит генерация картинки");
        new Thread(() -> {
            String imageUrl = "https://picsum.photos/200";
            try {
                URL url = new URL(imageUrl);
                InputStream inputStream = url.openStream();

                SendPhoto sendPhoto = SendPhoto.builder()
                        .chatId(chatId)
                        .photo(new InputFile(inputStream, "random.jpeg"))
                        .caption("Ваша случайная картинка:")
                        .build();

                telegramClient.execute(sendPhoto);

            } catch (IOException | TelegramApiException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    private void sendRandom(Long chatId) {
        int random = ThreadLocalRandom.current().nextInt();
        String text = "Ваше случайное число: %d".formatted(random);
        sendMessage(chatId, text);
    }

    private void sendMyName(Long chatId, User user) {
        String text = "Привет!\n\nВас зовут: %s\nВаш ник: %s"
                .formatted(
                        user.getFirstName(),
                        user.getUserName()
                );
        sendMessage(chatId, text);
    }

    private void sendMainMenu(Long chatId) {
        SendMessage message = SendMessage.builder()
                .text("Добро пожаловать! Выбери действие:")
                .chatId(chatId)
                .build();

        InlineKeyboardButton button1 = InlineKeyboardButton.builder()
                .text("Как меня зовут?")
                .callbackData("my_name") // при нажатии на кнопку определить на какую именно кнопку нажал пользователь
                .build();

        InlineKeyboardButton button2 = InlineKeyboardButton.builder()
                .text("Случайное число")
                .callbackData("random") // при нажатии на кнопку определить на какую именно кнопку нажал пользователь
                .build();

        InlineKeyboardButton button3 = InlineKeyboardButton.builder()
                .text("Получить случайную картинку")
                .callbackData("get_random_photo") // при нажатии на кнопку определить на какую именно кнопку нажал пользователь
                .build();

        List<InlineKeyboardRow> keyboardRows = List.of(
                new InlineKeyboardRow(button1),
                new InlineKeyboardRow(button2),
                new InlineKeyboardRow(button3)
        );

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup(keyboardRows);

        message.setReplyMarkup(markup);

        try {
            telegramClient.execute(message);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}
