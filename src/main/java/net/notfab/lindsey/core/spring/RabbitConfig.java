package net.notfab.lindsey.core.spring;

import net.lindseybot.utils.RabbitUtils;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.concurrent.TimeUnit;

@Configuration
public class RabbitConfig {

    @Bean
    @Primary
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(this.jackson2JsonMessageConverter());
        template.setReplyTimeout(TimeUnit.SECONDS.toMillis(15));
        return template;
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // -- Spring AMQP Remoting

    @Bean(name = "rpc")
    public RabbitTemplate rpcTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(RabbitUtils.jacksonConverter());
        template.setReplyTimeout(TimeUnit.SECONDS.toMillis(15));
        return template;
    }

}
