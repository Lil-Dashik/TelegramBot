package project.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import project.dto.NotificationDTO;

@Component
public class ConfirmationConsumer {
    private final CommuteBot commuteBot;


    @Autowired
    public ConfirmationConsumer(CommuteBot commuteBot) {
        this.commuteBot = commuteBot;
    }

    @KafkaListener(topics = "confirmations", groupId = "bot-group", containerFactory = "kafkaListenerContainerFactory")
    public void listenConfirmation(NotificationDTO notification) {
        commuteBot.sendMessage(notification.getTelegramUserId(), notification.getMessage());
    }
}
