package project.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import project.dto.NotificationDTO;

@Component
public class ConfirmationConsumer {
    private final CommuteBot commuteBot;
    private final ObjectMapper objectMapper;

    @Autowired
    public ConfirmationConsumer(CommuteBot commuteBot, ObjectMapper objectMapper) {
        this.commuteBot = commuteBot;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "confirmations", groupId = "bot-group", containerFactory = "kafkaListenerContainerFactory")
    public void listenConfirmation(String message) {
        try {
            NotificationDTO notification = objectMapper.readValue(message, NotificationDTO.class);
            commuteBot.sendMessage(notification.getTelegramUserId(), notification.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
