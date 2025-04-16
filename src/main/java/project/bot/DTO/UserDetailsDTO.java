package project.bot.DTO;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class UserDetailsDTO {
    private Long telegramUserId;
    private String username;
    private String firstName;
    public UserDetailsDTO() {}
    public UserDetailsDTO(Long telegramUserId, String username, String firstName) {
        this.telegramUserId = telegramUserId;
        this.username = username;
        this.firstName = firstName;
    }
}
