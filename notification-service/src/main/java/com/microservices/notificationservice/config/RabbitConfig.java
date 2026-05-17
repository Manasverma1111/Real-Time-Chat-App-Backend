package com.microservices.notificationservice.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

//    RabbitConfig is a configuration class that sets up the RabbitMQ messaging infrastructure for the notification service.
    public static final String EXCHANGE = "notification.exchange";
    public static final String QUEUE = "notification.queue";
    public static final String ROUTING_KEY = "notification.routing";

//    exchange() method defines a TopicExchange bean with the name "notification.exchange".
//    This exchange will be used to route messages based on the routing key.
    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE);
    }

//    queue() method defines a Queue bean with the name "notification.queue".
//    This queue will be used to receive messages related to notifications.
    @Bean
    public Queue queue() {
        return new Queue(QUEUE);
    }

//    binding() method defines a Binding bean that binds the queue to the exchange using the specified routing key.
    @Bean
    public Binding binding() {
        return BindingBuilder
                .bind(queue())
                .to(exchange())
                .with(ROUTING_KEY);
    }

//     JSON Converter
//     The messageConverter() method defines a Jackson2JsonMessageConverter bean
//     that will be used to convert messages to and from JSON format when sending
//     and receiving messages through RabbitMQ.
    @Bean
    public Jackson2JsonMessageConverter messageConverter() {

        Jackson2JsonMessageConverter converter =
                new Jackson2JsonMessageConverter();

        converter.setAlwaysConvertToInferredType(true);

        return converter;
    }

//     Listener factory with converter
//     The rabbitListenerContainerFactory() method defines a SimpleRabbitListenerContainerFactory bean
//     that configures the connection factory and sets the message converter for RabbitMQ listeners.
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory) {

        SimpleRabbitListenerContainerFactory factory =
                new SimpleRabbitListenerContainerFactory();

//         The connection factory is set to the provided connectionFactory parameter,
//        and the message converter is set to the Jackson2JsonMessageConverter defined in the messageConverter() method.
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter());

        return factory;
    }
}