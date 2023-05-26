package com.example.outbox.order;

import java.math.BigDecimal;

public record Order(Long orderId, BigDecimal amount, OrderStatus status) {
	public enum OrderStatus {CREATED, CANCELLED}


	public Order withId(Long orderId) {
		return new Order(orderId, this.amount, this.status);
	}
}
