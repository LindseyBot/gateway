package net.notfab.lindsey.core.spring;

import net.notfab.lindsey.core.service.rpc.RemoteGuildsImpl;
import net.notfab.lindsey.shared.enums.RabbitExchange;
import net.notfab.lindsey.shared.rpc.services.RemoteGuilds;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.remoting.service.AmqpInvokerServiceExporter;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = "net.notfab.lindsey.core.service.rpc")
public class RabbitConfig {

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jackson());
        return template;
    }

    @Bean
    public Jackson2JsonMessageConverter jackson() {
        return new Jackson2JsonMessageConverter();
    }

    // -- RPC

    public static final String EXCHANGE = "RPC";
    public static final String QUEUE_NAME = "Discord";

    @Bean
    AmqpInvokerServiceExporter exporter(RemoteGuildsImpl implementation, AmqpTemplate template) {
        AmqpInvokerServiceExporter exporter = new AmqpInvokerServiceExporter();
        exporter.setServiceInterface(RemoteGuilds.class);
        exporter.setService(implementation);
        exporter.setAmqpTemplate(template);
        return exporter;
    }

    @Bean
    SimpleMessageListenerContainer listener(ConnectionFactory factory, AmqpInvokerServiceExporter exporter) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(factory);
        container.setMessageListener(exporter);
        container.setQueueNames(QUEUE_NAME);
        return container;
    }

    @Bean
    public Queue rpcQueue() {
        return new Queue(QUEUE_NAME);
    }

    @Bean(name = "rpc")
    public Exchange rpcExchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Binding rpcBinding() {
        return BindingBuilder.bind(rpcQueue()).to(rpcExchange()).with("discord.#").and(null);
    }

    // -- Others

    @Bean
    public DirectExchange commandExchange() {
        return new DirectExchange(RabbitExchange.COMMANDS.getName());
    }

    // -- Gateway Messaging

    @Bean
    public Queue anonQueue() {
        return new AnonymousQueue();
    }

    @Bean
    public FanoutExchange gatewayExchange() {
        return new FanoutExchange(RabbitExchange.GATEWAYS.getName());
    }

    @Bean
    public Binding gatewayBinding() {
        return BindingBuilder.bind(anonQueue()).to(gatewayExchange());
    }

}
