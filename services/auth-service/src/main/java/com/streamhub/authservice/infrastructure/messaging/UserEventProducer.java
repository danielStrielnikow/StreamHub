package com.streamhub.authservice.infrastructure.messaging;

import com.streamhub.common.event.UserRegisteredEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserEventProducer {
    private final KafkaTemplate<String, UserRegisteredEvent> kafkaTemplate;
    
    public void publish(UUID userId, String email) {
        UserRegisteredEvent event = new UserRegisteredEvent(
                UUID.randomUUID(),
                userId,
                email,
                Instant.now()
        );
        kafkaTemplate.send("user-events", userId.toString(), event);
    }
}
