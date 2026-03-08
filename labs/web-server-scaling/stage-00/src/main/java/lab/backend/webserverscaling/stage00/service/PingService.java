package lab.backend.webserverscaling.stage00.service;

import java.time.Instant;
import java.util.Map;

import org.springframework.stereotype.Service;

@Service
public class PingService {

    public Map<String, Object> ping() {
        return Map.of(
            "message", "pong",
            "application", "web-server-scaling-stage-00",
            "timestamp", Instant.now().toString()
        );
    }
}
