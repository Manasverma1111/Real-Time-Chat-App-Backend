package com.microservices.messageservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

//    Define constants for the RabbitMQ exchange, queue, and routing key names.
    public static final String EXCHANGE = "notification.exchange";
    public static final String QUEUE = "notification.queue";
    public static final String ROUTING_KEY = "notification.routing";

//    The exchange() method defines a TopicExchange bean with the specified exchange name.
    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE);
    }

//    The queue() method defines a Queue bean with the specified queue name.
    @Bean
    public Queue queue() {
        return new Queue(QUEUE);
    }

//    The binding() method defines a Binding bean that binds the queue to the exchange using the specified routing key.
    @Bean
    public Binding binding() {
        return BindingBuilder
                .bind(queue())
                .to(exchange())
                .with(ROUTING_KEY);
    }

    /*
     CRITICAL: JSON converter
    */
//    The messageConverter() method defines a Jackson2JsonMessageConverter bean,
//    which is used to convert messages to and from JSON format when sending and receiving messages through RabbitMQ.
    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /*
     CRITICAL: attach converter to RabbitTemplate
    */
//    The rabbitTemplate() method defines a RabbitTemplate bean that is configured with the provided ConnectionFactory.
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}