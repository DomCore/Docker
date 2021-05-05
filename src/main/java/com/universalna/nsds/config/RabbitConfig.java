package com.universalna.nsds.config;


import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.Collection;

import static com.universalna.nsds.Profiles.METADATA;

@EnableRabbit
@Profile(METADATA)
@Configuration
@ComponentScan("com.universalna.nsds.controller.amqp")
public class RabbitConfig {

    public static final String SETTLEMENT_CASE_CREATED_EXHANGE = "settlement.case.created.exchange";
    public static final String NSDS_SETTLEMENT_CASE_CREATED_QUEUE = "nsds.settlement.case.created";

    @Bean
    public Queue settlementCaseCreatedQueue(@Value("${spring.rabbitmq.cluster}") final String cluster) {
        return QueueBuilder.durable(cluster + "." + NSDS_SETTLEMENT_CASE_CREATED_QUEUE).build();
    }

    @Bean
    public FanoutExchange settlementCaseCreatedExchange(@Value("${spring.rabbitmq.cluster}") final String cluster) {
        return (FanoutExchange) ExchangeBuilder.fanoutExchange(cluster + "." + SETTLEMENT_CASE_CREATED_EXHANGE).build();
    }

    @Bean
    public Binding binding(final Queue settlementCaseCreatedQueue,
                           final FanoutExchange settlementCaseCreatedExchange) {
        return BindingBuilder
                .bind(settlementCaseCreatedQueue)
                .to(settlementCaseCreatedExchange);
    }

//    TODO: dirty solution, refactor if possible
    @Bean
    public AmqpAdmin amqpAdmin(final ConnectionFactory connectionFactory,
                               final Collection<Queue> queues,
                               final Collection<Exchange> exchanges,
                               final Collection<Binding> bindings) {
        final AmqpAdmin amqpAdmin = new RabbitAdmin(connectionFactory);
        queues.forEach(amqpAdmin::declareQueue);
        exchanges.forEach(amqpAdmin::declareExchange);
        bindings.forEach(amqpAdmin::declareBinding);
        return amqpAdmin;
    }

}
