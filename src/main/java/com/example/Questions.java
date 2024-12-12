package com.example;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Questions extends TelegramLongPollingBot {

    private final Map<String, Integer> userState = new HashMap<>();

    @Override
    public String getBotUsername() {
        return "olgas_psycho_bot";
    }

    @Override
    public String getBotToken() {
        return "7979867248:AAFgpvpYmPouKBdrj99lITYFc5NHh32NGeY";
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String chatId = update.getMessage().getChatId().toString();
            String userMessage = update.getMessage().getText();

            if (userMessage.equalsIgnoreCase("/start")) {
                userState.put(chatId, 1); // Начинаем с текста 1
                sendQuestion(chatId, "text_1", List.of("Да", "Нет"), List.of("yes", "no"));
            }
        }

        if (update.hasCallbackQuery()) {
            CallbackQuery callbackQuery = update.getCallbackQuery();
            String chatId = callbackQuery.getMessage().getChatId().toString();
            String callbackData = callbackQuery.getData();

            handleCallbackQuery(chatId, callbackData);
        }
    }

    private void handleCallbackQuery(String chatId, String callbackData) {
        int currentState = userState.getOrDefault(chatId, 1);

        switch (currentState) {
            case 1:
                if (callbackData.equals("yes") || callbackData.equals("no")) {
                    userState.put(chatId, 2);
                    sendQuestion(chatId, "text_2",
                            List.of(
                                    "Ловлю себя и довожу до результата",
                                    "Начинаю, но не хватает сил",
                                    "Нужна поддержка"
                            ),
                            List.of("result", "burnout", "support"));
                }
                break;
            case 2:
                userState.put(chatId, 3);
                sendQuestion(chatId, "text_3",
                        List.of(
                                "Тревогу",
                                "Вину",
                                "Раздражение",
                                "Смятение",
                                "Неудовлетворенность",
                                "Разочарование в себе",
                                "Ком неприятных эмоций"
                        ),
                        List.of("anxiety", "guilt", "irritation", "confusion", "dissatisfaction", "disappointment", "emotional_clump"));
                break;
            case 3:
                userState.put(chatId, 4);
                sendQuestion(chatId, "text_4",
                        List.of(
                                "Хочу действовать себе во благо",
                                "Догадываюсь",
                                "Не думал об этом",
                                "Буду знать"
                        ),
                        List.of("action", "guess", "not_thought", "noted"));
                break;
            case 4:
                userState.put(chatId, 5);
                sendQuestion(chatId, "text_5", List.of("Да", "Нет"), List.of("yes_final", "no_final"));
                break;
            case 5:
                // Отображаем текст 6 без кнопок
                userState.put(chatId, 6);
                sendSimpleText(chatId, "text_6");
                break;
        }
    }
    private void sendSimpleText(String chatId, String textKey) {
        String messageText = readFileContent(textKey); // Читаем текст из файла

        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(messageText);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendQuestion(String chatId, String questionTextKey, List<String> buttonLabels, List<String> callbackData) {
        String questionText = readFileContent(questionTextKey); // Читаем текст из файла

        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(questionText);

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        for (int i = 0; i < buttonLabels.size(); i++) {
            List<InlineKeyboardButton> row = new ArrayList<>();
            row.add(createButton(buttonLabels.get(i), callbackData.get(i)));
            keyboard.add(row);
        }

        markup.setKeyboard(keyboard);
        message.setReplyMarkup(markup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private InlineKeyboardButton createButton(String text, String callbackData) {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(text);
        button.setCallbackData(callbackData);
        return button;
    }

    private String readFileContent(String fileName) {
        try {
            Path filePath = Paths.get("src/main/resources/text/", fileName + ".txt");
            return Files.readString(filePath);
        } catch (IOException e) {
            e.printStackTrace();
            return "Ошибка при чтении файла: " + fileName;
        }
    }
}