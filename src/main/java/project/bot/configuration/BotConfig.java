package project.bot.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
@Setter
public class BotConfig {
    @Value("${bot.name}")
    private String botName;
    @Value("${bot.token}")
    private String botToken;

    public BotConfig() {
    }

    public BotConfig(String botName, String botToken) {
        this.botName = botName;
        this.botToken = botToken;
    }
}
