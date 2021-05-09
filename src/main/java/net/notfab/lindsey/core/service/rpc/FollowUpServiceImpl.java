package net.notfab.lindsey.core.service.rpc;

import net.dv8tion.jda.api.sharding.ShardManager;
import net.lindseybot.discord.Message;
import net.lindseybot.utils.RabbitUtils;
import net.notfab.lindsey.shared.rpc.services.FollowUpService;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.remoting.service.AmqpInvokerServiceExporter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

@Service
public class FollowUpServiceImpl implements FollowUpService {

    private final ShardManager shardManager;

    public FollowUpServiceImpl(ShardManager shardManager) {
        this.shardManager = shardManager;
    }

    @Override
    public long sendMessage(long guild, long channel, Message message) {
        System.out.println(message.getName());
        return 1L;
    }

    @Override
    public long editMessage(long guild, long channel, long messageId, Message message) {
        return 0;
    }

    // -- RabbitMQ RPC Setup

    @Bean(name = "FollowUpQ")
    public Queue queue() {
        return new Queue(this.getRabbitName(), false, false, true);
    }

    @Bean(name = "FollowUpE")
    public DirectExchange exchange() {
        return new DirectExchange(this.getRabbitName(), false, true);
    }

    @Bean(name = "FollowUpB")
    public Binding binding(@Qualifier("FollowUpQ") Queue queue, @Qualifier("FollowUpE") DirectExchange exchange) {
        return BindingBuilder.bind(queue)
            .to(exchange)
            .withQueueName();
    }

    @Bean(name = "FollowUpS")
    public AmqpInvokerServiceExporter exporter(@Qualifier("rpc") AmqpTemplate template) {
        AmqpInvokerServiceExporter exporter = new AmqpInvokerServiceExporter();
        exporter.setServiceInterface(FollowUpService.class);
        exporter.setService(this);
        exporter.setAmqpTemplate(template);
        exporter.setMessageConverter(RabbitUtils.jacksonConverter());
        return exporter;
    }

    @Bean(name = "FollowUpL")
    public SimpleMessageListenerContainer listener(
        ConnectionFactory factory,
        @Qualifier("FollowUpS") AmqpInvokerServiceExporter exporter,
        @Qualifier("FollowUpQ") Queue queue
    ) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(factory);
        container.setMessageListener(exporter);
        container.setQueues(queue);
        return container;
    }

}
