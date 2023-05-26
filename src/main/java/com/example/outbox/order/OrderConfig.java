package com.example.outbox;

import java.sql.Timestamp;

import javax.sql.DataSource;

import com.example.outbox.order.Order;
import org.slf4j.LoggerFactory;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.jdbc.JdbcMessageHandler;
import org.springframework.integration.jdbc.store.JdbcChannelMessageStore;
import org.springframework.integration.jdbc.store.channel.PostgresChannelMessageStoreQueryProvider;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;

@Configuration
public class IntegrationConfig {
	@Bean
	public JdbcChannelMessageStore jdbcChannelMessageStore(DataSource dataSource) {
		JdbcChannelMessageStore jdbcChannelMessageStore = new JdbcChannelMessageStore(dataSource);
		jdbcChannelMessageStore.setChannelMessageStoreQueryProvider(new PostgresChannelMessageStoreQueryProvider());
		return jdbcChannelMessageStore;
	}

	@Bean
	public JdbcMessageHandler jdbcMessageHandler(JdbcTemplate jdbcTemplate) {
		final JdbcMessageHandler jdbcMessageHandler = new JdbcMessageHandler(jdbcTemplate, """
				INSERT INTO "order"(name, amount, created_at) VALUES(:name, :amount, :createdAt)
				""");
		jdbcMessageHandler.setSqlParameterSourceFactory(input -> {
			final Order order = (Order) ((Message<?>) input).getPayload();
			return new MapSqlParameterSource()
					.addValue("name", order.name())
					.addValue("amount", order.amount())
					.addValue("createdAt", Timestamp.from(order.createdAt()));
		});
		return jdbcMessageHandler;
	}

	@Bean
	public IntegrationFlow integrationFlow(JdbcMessageHandler jdbcMessageHandler) {
		return IntegrationFlow.from("outbox.input")
				.routeToRecipients(routes -> routes.transactional()
						.recipientFlow(f -> f.handle(jdbcMessageHandler))
						.recipientFlow(f -> f.handle(new MessageHandler() {
							@Override
							public void handleMessage(Message<?> message) throws MessagingException {
								LoggerFactory.getLogger("relay").info("{}", message);
							}
						})))
				.get();
	}
}
