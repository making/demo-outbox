package com.example.outbox.order;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.observation.annotation.Observed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
@Observed
public class OrderListener {
	private final Logger log = LoggerFactory.getLogger(OrderListener.class);

	private final ObjectMapper objectMapper;

	public OrderListener(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	@RabbitListener(queues = "order.event")
	public void handleOrderEvent(JsonNode payload, @Header("eventType") String eventType) {
		switch (eventType) {
			case "order_created" -> {
				final OrderEvents.Created event = this.objectMapper.convertValue(payload, OrderEvents.Created.class);
				log.info("Order Created: {}", event);
			}
			case "order_cancelled" -> {
				final OrderEvents.Cancelled event = this.objectMapper.convertValue(payload, OrderEvents.Cancelled.class);
				log.info("Order Cancelled: {}", event);
			}
			default -> log.warn("Unknown Event Type: {}", eventType);
		}
	}
}
