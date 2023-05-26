package com.example.outbox.order.web;

import java.math.BigDecimal;

record OrderRequest(BigDecimal amount) {
}
