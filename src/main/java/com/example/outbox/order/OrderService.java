package com.example.outbox.order;

import com.example.outbox.order.Order.OrderStatus;
import io.micrometer.observation.annotation.Observed;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@Observed
public class OrderService {
	private final JdbcTemplate jdbcTemplate;

	public OrderService(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public Order create(Order order) {
		final Long orderId = this.jdbcTemplate.queryForObject("""
				INSERT INTO "order"(amount, status) VALUES(?, ?) RETURNING order_id
				""".trim(), Long.class, order.amount(), order.status().name());
		return order.withId(orderId);
	}

	public int cancel(Long orderId) {
		return this.jdbcTemplate.update("""
				UPDATE "order" SET status=? WHERE order_id=? AND status <> ?
				""".trim(), OrderStatus.CANCELLED.name(), orderId, OrderStatus.CANCELLED.name());
	}
}
