package com.example.outbox.order;

import java.io.Serializable;
import java.math.BigDecimal;

public class OrderEvents {
	public record Created(Long orderId, BigDecimal amount) implements Serializable {

		public static Created from(Order order) {
			return new Created(order.orderId(), order.amount());
		}
	}

	public record Cancelled(Long orderId) implements Serializable {

	}
}
