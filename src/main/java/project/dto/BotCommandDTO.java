package project.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class BotCommandDTO {
    private String command;
    private Long telegramId;
    private String message;

    public BotCommandDTO(String command, Long telegramId, String message) {
        this.command = command;
        this.telegramId = telegramId;
        this.message = message;
    }

    public BotCommandDTO() {
    }
}
