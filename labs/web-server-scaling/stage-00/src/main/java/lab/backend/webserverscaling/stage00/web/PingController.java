package lab.backend.webserverscaling.stage00.web;

import java.util.Map;

import lab.backend.webserverscaling.stage00.service.PingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PingController {

    private final PingService pingService;

    public PingController(PingService pingService) {
        this.pingService = pingService;
    }

    @GetMapping("/ping")
    public Map<String, Object> ping() {
        return pingService.ping();
    }

    @GetMapping("/healthz")
    public Map<String, Object> healthz() {
        return Map.of("status", "UP");
    }
}
