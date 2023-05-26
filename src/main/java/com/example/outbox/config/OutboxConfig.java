package com.example.outbox.config;

import javax.sql.DataSource;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.amqp.dsl.Amqp;
import org.springframework.integration.amqp.dsl.AmqpOutboundChannelAdapterSpec;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.integration.jdbc.store.JdbcChannelMessageStore;
import org.springframework.integration.jdbc.store.channel.PostgresChannelMessageStoreQueryProvider;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;

@Configuration(proxyBeanMethods = false)
public class OutboxConfig {

	@Bean
	public JdbcChannelMessageStore jdbcChannelMessageStore(DataSource dataSource) {
		JdbcChannelMessageStore jdbcChannelMessageStore = new JdbcChannelMessageStore(dataSource);
		jdbcChannelMessageStore.setChannelMessageStoreQueryProvider(new PostgresChannelMessageStoreQueryProvider());
		return jdbcChannelMessageStore;
	}

	@Bean
	public QueueChannel outbox(JdbcChannelMessageStore jdbcChannelMessageStore) {
		return MessageChannels.queue(jdbcChannelMessageStore, "outbox").getObject();
	}

	@Bean
	public AmqpOutboundChannelAdapterSpec amqpHandler(AmqpTemplate amqpTemplate) {
		return Amqp.outboundAdapter(amqpTemplate)
				.exchangeNameFunction(m -> getEventType(m).split("_")[0])
				.routingKeyFunction(m -> getEventType(m).split("_")[1]);
	}

	static String getEventType(Message<?> message) {
		return (String) message.getHeaders().get("eventType");
	}

	@Bean
	public IntegrationFlow messageRelayFlow(MessageHandler amqpHandler) {
		return IntegrationFlow.from("outbox")
				.handle(amqpHandler, e -> e.poller(poller -> poller.fixedDelay(5_000, 5_000).transactional()))
				.get();
	}

}
