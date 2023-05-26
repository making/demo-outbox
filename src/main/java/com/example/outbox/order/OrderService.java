package com.example.outbox.order;

import com.example.outbox.order.Order.OrderStatus;
import io.micrometer.observation.annotation.Observed;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
@Observed
public class OrderMapper {
	private final JdbcTemplate jdbcTemplate;

	public OrderMapper(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public Order insert(Order order) {
		final Long orderId = this.jdbcTemplate.queryForObject("""
				INSERT INTO "order"(amount, status) VALUES(?, ?) RETURNING order_id
				""".trim(), Long.class, order.amount(), order.status().name());
		return new Order(orderId, order.amount(), order.status());
	}

	public int cancel(Long orderId) {
		return this.jdbcTemplate.update("""
				UPDATE "order" SET status=? WHERE id=?
				""".trim(), OrderStatus.CANCELLED.name(), orderId);
	}
}
