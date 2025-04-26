package project.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import project.dto.NotificationDTO;

@Component
public class AlertConsumer {
    private final CommuteBot commuteBot;
    @Autowired
    public AlertConsumer(CommuteBot commuteBot) {
        this.commuteBot = commuteBot;
    }
    @KafkaListener(topics = "alerts", groupId = "bot-group", containerFactory = "kafkaListenerContainerFactory")
    public void listen(NotificationDTO notification) {
        commuteBot.sendMessage(notification.getTelegramUserId(), notification.getMessage());
        commuteBot.sendMarkNotified(notification.getTelegramUserId());
    }
}
