package com.example.outbox.order;

import com.example.outbox.order.OrderEvents.Cancelled;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;

@Configuration(proxyBeanMethods = false)
public class OrderConfig {

	@Bean
	public IntegrationFlow orderCreateFlow(OrderService orderService) {
		return IntegrationFlow.from("order.create")
				.routeToRecipients(routes -> routes.transactional()
						.recipientFlow(f -> f
								.<Order>handle((order, headers) -> orderService.create(order))
								.channel(c -> c.publishSubscribe("order.create.reply"))
								.transform(OrderEvents.Created::from)
								.enrichHeaders(h -> h.header("eventType", "order_created"))
								.channel("outbox")))
				.get();
	}

	@Bean
	public IntegrationFlow orderCancelFlow(OrderService orderService) {
		return IntegrationFlow.from("order.cancel")
				.routeToRecipients(routes -> routes.transactional()
						.recipientFlow(f -> f
								.<Long>handle((orderId, headers) -> {
									final int updated = orderService.cancel(orderId);
									return updated > 0 ? orderId : null;
								})
								.transform(Cancelled::new)
								.enrichHeaders(h -> h.header("eventType", "order_cancelled"))
								.channel("outbox")))
				.get();
	}
}
