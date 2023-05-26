package com.example.outbox;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.ContainerCustomizer;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.amqp.RabbitTemplateCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AmqpConfig {

	@Bean
	public TopicExchange orderExchange() {
		return new TopicExchange("order");
	}

	@Bean
	public Queue orderCreatedQueue() {
		return new Queue("order.created");
	}

	@Bean
	public Queue orderCancelledQueue() {
		return new Queue("order.cancelled");
	}

	@Bean
	public Binding orderCreatedBinding(@Qualifier("orderCreatedQueue") Queue queue, TopicExchange exchange) {
		return BindingBuilder.bind(queue).to(exchange).with("created");
	}

	@Bean
	public Binding orderCancelledBinding(@Qualifier("orderCancelledQueue") Queue queue, TopicExchange exchange) {
		return BindingBuilder.bind(queue).to(exchange).with("cancelled");
	}

	@Bean
	public Jackson2JsonMessageConverter jsonMessageConverter() {
		return new Jackson2JsonMessageConverter();
	}

	@Bean
	public RabbitTemplateCustomizer rabbitTemplateCustomizer() {
		return rabbitTemplate -> rabbitTemplate.setObservationEnabled(true);
	}

	@Bean
	public ContainerCustomizer<SimpleMessageListenerContainer> containerCustomizer() {
		return container -> container.setObservationEnabled(true);
	}
}
