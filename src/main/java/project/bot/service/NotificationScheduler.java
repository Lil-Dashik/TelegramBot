package project.bot.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import project.bot.DTO.NotificationDTO;


import java.time.LocalTime;
import java.util.List;

@Component
public class NotificationScheduler {
    private static final Logger logger = LoggerFactory.getLogger(NotificationScheduler.class);
    private final RestTemplate restTemplate;
    private final CommuteBot commuteBot;

    @Autowired
    public NotificationScheduler(CommuteBot commuteBot, RestTemplate restTemplate) {
        this.commuteBot = commuteBot;
        this.restTemplate = restTemplate;
    }

    @Scheduled(fixedRate = 300000)
    public void checkAndSendNotifications() {
        String url = "http://localhost:8080/api/commute/notifications";

        try {
            ResponseEntity<List<NotificationDTO>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {
                    }
            );

            List<NotificationDTO> notifications = response.getBody();

            if (notifications != null && !notifications.isEmpty()) {
                for (NotificationDTO dto : notifications) {
                    commuteBot.sendMessage(dto.getTelegramUserId(), dto.getMessage());
                    String markUrl = "http://localhost:8080/api/commute/markNotified";
                    restTemplate.postForObject(markUrl, dto.getTelegramUserId(), Void.class);
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
