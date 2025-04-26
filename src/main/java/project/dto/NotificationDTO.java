package project.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.ZonedDateTime;

@Setter
@Getter
@ToString
public class NotificationDTO {
    private Long telegramUserId;
    private String message;
    private ZonedDateTime notifyTime;

    public NotificationDTO() {
    }

    public NotificationDTO(Long telegramUserId, String message, ZonedDateTime notifyTime) {
        this.telegramUserId = telegramUserId;
        this.message = message;
        this.notifyTime = notifyTime;
    }
}
