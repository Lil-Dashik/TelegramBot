package project.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import project.dto.BotCommandDTO;

@Service
public class KafkaBotProducer {
    private final KafkaTemplate<Long, BotCommandDTO> kafkaTemplate;

    @Autowired
    public KafkaBotProducer(KafkaTemplate<Long, BotCommandDTO> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendCommand(BotCommandDTO dto) {
        kafkaTemplate.send("producer-bot-topic", dto.getTelegramId(), dto);
    }
}
