package com.example.outbox.order;

import java.util.Random;

import io.micrometer.observation.annotation.Observed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

@Component
@Observed
public class OrderListener {
	private final Logger log = LoggerFactory.getLogger(OrderListener.class);

	@RabbitListener(queues = "order.created")
	public void handleOrderCreated(Message<OrderEvents.Created> event) {
		log.info("Received {}", event);
		if (new Random().nextInt(100) == 0) {
			log.error("Fail!");
			throw new RuntimeException("!!");
		}
	}

	@RabbitListener(queues = "order.cancelled")
	public void handleOrderCancelled(Message<OrderEvents.Cancelled> event) {
		log.info("Received {}", event);
	}
}
