package project.bot.DTO;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Setter
@Getter
@ToString
public class NotificationDTO {
    private Long telegramUserId;
    private String message;
    private LocalDateTime notifyTime;

    public NotificationDTO() {
    }

    public NotificationDTO(Long telegramUserId, String message, LocalDateTime notifyTime) {
        this.telegramUserId = telegramUserId;
        this.message = message;
        this.notifyTime = notifyTime;
    }
}
