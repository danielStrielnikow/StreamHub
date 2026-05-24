package com.streamhub.userservice.infrastructure.messaging;

import com.streamhub.common.event.UserRegisteredEvent;
import com.streamhub.userservice.domain.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserEventConsumer {

    private final UserService userService;

    @KafkaListener(topics = "user-events", groupId = "user-service")
    public void handleUserRegistered(UserRegisteredEvent event) {
        userService.createFromEvent(event);
        log.info("User {} saved from Kafka event", event.email());
    }
}
