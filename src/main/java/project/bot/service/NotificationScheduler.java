package project.bot.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import project.bot.CommuteClient;
import project.bot.dto.NotificationDTO;


import java.time.LocalTime;
import java.util.List;

@Component
public class NotificationScheduler {
    private static final Logger logger = LoggerFactory.getLogger(NotificationScheduler.class);
    private final CommuteClient commuteClient;
    private final CommuteBot commuteBot;

    @Autowired
    public NotificationScheduler(CommuteBot commuteBot, CommuteClient commuteClient) {
        this.commuteBot = commuteBot;
        this.commuteClient = commuteClient;
    }

    @Scheduled(fixedRate = 300000)
    public void checkAndSendNotifications() {
        try {
            List<NotificationDTO> notifications = commuteClient.getNotifications();

            if (notifications != null && !notifications.isEmpty()) {
                for (NotificationDTO dto : notifications) {
                    commuteBot.sendMessage(dto.getTelegramUserId(), dto.getMessage());
                    commuteClient.markAsNotified(dto.getTelegramUserId());
                    logger.info("Уведомление отправлено пользователю {} в {}", dto.getTelegramUserId(), LocalTime.now());
                }
            } else {
                logger.info("Нет пользователей для уведомления на текущий момент {}", LocalTime.now());
            }

        } catch (Exception e) {
            logger.error("Ошибка при получении уведомлений: {}", e.getMessage(), e);
        }
    }
}
