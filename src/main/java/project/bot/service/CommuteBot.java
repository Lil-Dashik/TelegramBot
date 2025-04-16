package project.bot.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import project.bot.Configuration.BotConfig;
import project.bot.DTO.UserDTO;
import project.bot.DTO.UserDetailsDTO;


import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class CommuteBot extends TelegramLongPollingBot {
    private final BotConfig config;
    private final RestTemplate restTemplate;
    private final Map<Long, Boolean> userWaitingForData = new HashMap<>();

    @Autowired
    public CommuteBot(BotConfig config, RestTemplate restTemplate) {
        this.config = config;
        this.restTemplate = restTemplate;
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
                saveUserDataWork(message);
                userWaitingForData.put(chatId, false);
            } else if ("/stop".equalsIgnoreCase(messageText)) {
                disableNotifications(chatId);
                sendMessage(chatId, "Уведомления отключены. Чтобы снова их включить, начните с команды /start.");
            } else {
                sendMessage(chatId, "Вы отправили: " + messageText);
            }
        }
    }

    private void sendStartMessage(Long chatId) {
        String startMessage = "Привет! Я бот, который помогает прогнозировать время в пути до работы. \n" +
                "Просто отправь мне свой адрес дома и работы, и я помогу рассчитать оптимальное время для выезда.";

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
        UserDetailsDTO userDetailsDTO = new UserDetailsDTO(
                message.getFrom().getId(),
                message.getFrom().getUserName(),
                message.getFrom().getFirstName()
        );
        String url = "http://localhost:8080/api/commute/start";
        restTemplate.postForObject(url, userDetailsDTO, String.class);
    }

    private void sendGoToWorkMessage(Long chatId) {
        String requestMessage = "Пожалуйста, отправьте адрес дома, адрес работы и время, к которому нужно быть на работе в формате 'HH:mm'. Например: \n" +
                "'Москва, Красная площадь 1; Москва, Тверская улица 7; 09:00'";

        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(requestMessage);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void saveUserDataWork(Message message) {
        String userInput = message.getText().trim();

        String[] parts = userInput.split(";");

        if (parts.length == 3) {
            String homeAddress = parts[0].trim();
            String workAddress = parts[1].trim();
            String workStartTimeStr = parts[2].trim();
            LocalTime workStartTime = parseTime(workStartTimeStr);

            UserDTO userDTO = new UserDTO();
            userDTO.setTelegramUserId(message.getFrom().getId());
            userDTO.setHomeAddress(homeAddress);
            userDTO.setWorkAddress(workAddress);
            userDTO.setWorkStartTime(workStartTime);
            String url = "http://localhost:8080/api/commute/goToWork";
            RestTemplate restTemplate = new RestTemplate();
            try {
                restTemplate.postForObject(url, userDTO, String.class);
                sendMessage(message.getChatId(), "Отправим уведомление за 30 минут до выезда!");
            } catch (Exception e) {
                e.printStackTrace();
                sendMessage(message.getChatId(), "Произошла ошибка при расчете времени.");
            }
        } else {
            sendMessage(message.getChatId(), "Неверный формат времени. Пожалуйста, укажите время в формате HH:mm.");
        }
    }

    private LocalTime parseTime(String time) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

        try {
            return LocalTime.parse(time, formatter);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    public void sendMessage(Long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(text);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
    private void disableNotifications(Long telegramUserId) {
        String url = "http://localhost:8080/api/commute/stop";

        try {
            restTemplate.postForObject(url, telegramUserId, String.class);
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
