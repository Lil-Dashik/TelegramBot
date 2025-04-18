package project.bot.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import project.bot.CommuteClient;
import project.bot.configuration.BotConfig;
import project.bot.mapper.MessageMapper;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class CommuteBot extends TelegramLongPollingBot {
    private final BotConfig config;
    private final CommuteClient commuteClient;
    private final MessageMapper messageMapper;
    private final Map<Long, Boolean> userWaitingForData = new HashMap<>();

    @Autowired
    public CommuteBot(BotConfig config, CommuteClient commuteClient, MessageMapper messageMapper) {
        this.config = config;
        this.commuteClient = commuteClient;
        this.messageMapper = messageMapper;
    }

    @Override
    public void onUpdateReceived(Update update) {
        Message message = update.getMessage();

        if (message != null && message.hasText()) {
            String messageText = message.getText();
            Long chatId = message.getChatId();

            if ("/start".equals(messageText)) {
                sendStartMessage(chatId);
                saveUserData(message);
            } else if ("/go_to_work".equals(messageText)) {
                sendGoToWorkMessage(chatId);
                userWaitingForData.put(chatId, true);
            } else if (userWaitingForData.containsKey(chatId) && userWaitingForData.get(chatId)) {
                saveUserDataWork(chatId, message);
                userWaitingForData.put(chatId, false);
            } else if ("/stop".equalsIgnoreCase(messageText)) {
                disableNotifications(chatId);
                sendMessage(chatId, "Уведомления отключены. Чтобы снова их включить, начните с команды /start.");
            } else {
                sendMessage(chatId, "Неверная команда. Вы отправили: " + messageText);
            }
        }
    }

    private void sendStartMessage(Long chatId) {
        String startMessage = "Привет! Я бот, который помогает прогнозировать время в пути до работы. \n" +
                "Выбери команду /go_to_work для того, чтобы начать расчёт времени.";

        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(startMessage);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void saveUserData(Message message) {
        String body = String.format(
                "%d; %s; %s",
                message.getFrom().getId(),
                message.getFrom().getUserName(),
                message.getFrom().getFirstName()
        );
        try {
            commuteClient.sendStartData(body);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendGoToWorkMessage(Long chatId) {
        String requestMessage = "Пожалуйста, отправьте <адрес дома>; <адрес работы>; и время ('HH:mm'), к которому нужно быть на работе. Например: \n" +
                "Москва, Красная площадь 1; Москва, Тверская улица 7; 09:00";

        SendMessage message = messageMapper.toSendMessage(chatId, requestMessage);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void saveUserDataWork(Long telegramId, Message message) {
        String userInput = message.getText().trim();
        try {
            commuteClient.sendWorkData(telegramId, userInput);
            sendMessage(message.getChatId(), "Отправим уведомление за 30 минут до выезда!");
        } catch (HttpClientErrorException.BadRequest e) {
            sendMessage(message.getChatId(), "Ошибка: " + e.getResponseBodyAsString());
        } catch (Exception e) {
            e.printStackTrace();
            sendMessage(message.getChatId(), "Произошла ошибка при обработке данных. Проверь формат: <дом>; <работа>; <HH:mm>. Снова выполните команду /go_to_work");
        }
    }


    public void sendMessage(Long chatId, String text) {
        SendMessage message = messageMapper.toSendMessage(chatId, text);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void disableNotifications(Long telegramUserId) {
        try {
            commuteClient.stopNotifications(telegramUserId);
        } catch (Exception e) {
            e.printStackTrace();
            sendMessage(telegramUserId, "Не удалось отключить уведомления. Попробуйте позже.");
        }
    }

    @PostConstruct
    public void initCommands() {
        List<BotCommand> commands = List.of(
                new BotCommand("start", "начать работу"),
                new BotCommand("go_to_work", "рассчитать время до работы"),
                new BotCommand("stop", "отключить уведомления")
        );

        try {
            this.execute(new SetMyCommands(commands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }


    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getBotToken();
    }
}
