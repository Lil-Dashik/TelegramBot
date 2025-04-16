package project.bot.Configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
@Setter
public class BotConfig {
    @Value("${bot.name}")
    String botName;
    @Value("${bot.token}")
    String botToken;

    public BotConfig() {
    }

    public BotConfig(String botName, String botToken) {
        this.botName = botName;
        this.botToken = botToken;
    }
}
