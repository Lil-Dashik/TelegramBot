package project.bot;

import feign.Headers;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import project.bot.dto.NotificationDTO;

import java.util.List;

@FeignClient(name = "commuteClient", url = "http://localhost:8080")
public interface CommuteClient {
    @PostMapping(value = "/api/commute/bot/commands/start", consumes = MediaType.TEXT_PLAIN_VALUE)
    @Headers("Content-Type: text/plain; charset=UTF-8")
    void sendStartData(@RequestBody String body);

    @PostMapping(value = "/api/commute/bot/commands/goToWork", consumes = MediaType.TEXT_PLAIN_VALUE)
    @Headers("Content-Type: text/plain; charset=UTF-8")
    void sendWorkData(@RequestParam("telegramId") Long telegramId, @RequestBody String body);

    @PostMapping(value = "/api/commute/bot/commands/stop")
    void stopNotifications(@RequestBody Long telegramUserId);

    @GetMapping("/api/commute/notifications")
    List<NotificationDTO> getNotifications();

    @PostMapping("/api/commute/markNotified")
    void markAsNotified(@RequestBody Long telegramUserId);
}
