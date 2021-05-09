package net.notfab.lindsey.core.service.rpc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.lindseybot.discord.Message;
import net.lindseybot.followup.FollowUp;
import net.lindseybot.utils.RabbitUtils;
import net.notfab.lindsey.core.framework.DiscordAdapter;
import net.notfab.lindsey.core.framework.Utils;
import net.notfab.lindsey.shared.rpc.services.FollowUpService;
import net.notfab.lindsey.shared.utils.Snowflake;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.remoting.service.AmqpInvokerServiceExporter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class FollowUpServiceImpl implements FollowUpService {

    private final ShardManager shardManager;
    private final DiscordAdapter adapter;
    private final StringRedisTemplate redis;
    private final Snowflake snowflake;
    private final ObjectMapper objectMapper;

    public FollowUpServiceImpl(
        ShardManager shardManager,
        DiscordAdapter adapter,
        StringRedisTemplate redis,
        Snowflake snowflake,
        ObjectMapper objectMapper
    ) {
        this.shardManager = shardManager;
        this.adapter = adapter;
        this.redis = redis;
        this.snowflake = snowflake;
        this.objectMapper = objectMapper;
    }

    @Override
    public long sendMessage(long guildId, long channelId, Message message) {
        Guild guild = this.shardManager.getGuildById(guildId);
        if (guild == null) {
            throw new IllegalArgumentException("Unknown guild");
        }
        TextChannel channel = guild.getTextChannelById(channelId);
        if (channel == null) {
            throw new IllegalArgumentException("Unknown channel");
        }
        long interaction = this.snowflake.next();
        channel.sendMessage(this.adapter.toMessage(message, guild))
            .queue(msg -> this.save(interaction, msg), Utils.noop());
        return interaction;
    }

    @Override
    public long editMessage(long guildId, long channelId, long messageId, Message message) {
        Guild guild = this.shardManager.getGuildById(guildId);
        if (guild == null) {
            throw new IllegalArgumentException("Unknown guild");
        }
        TextChannel channel = guild.getTextChannelById(channelId);
        if (channel == null) {
            throw new IllegalArgumentException("Unknown channel");
        }
        long interaction = this.snowflake.next();
        channel.editMessageById(messageId, this.adapter.toMessage(message, guild))
            .queue(msg -> this.save(interaction, msg), Utils.noop());
        return interaction;
    }

    @Override
    public long editMessage(long interaction, Message message) {
        FollowUp followUp = this.find(interaction);
        if (followUp == null) {
            throw new IllegalArgumentException("Unknown interaction (Expired?)");
        }
        Guild guild = this.shardManager.getGuildById(followUp.getGuildId());
        if (guild == null) {
            throw new IllegalArgumentException("Unknown guild");
        }
        TextChannel channel = guild.getTextChannelById(followUp.getChannelId());
        if (channel == null) {
            throw new IllegalArgumentException("Unknown channel");
        }
        channel.editMessageById(followUp.getMessageId(), this.adapter.toMessage(message, guild))
            .queue(msg -> this.save(interaction, msg), Utils.noop());
        return interaction;
    }

    @Override
    public void deleteMessage(long interaction) {
        FollowUp followUp = this.find(interaction);
        if (followUp == null) {
            throw new IllegalArgumentException("Unknown interaction (Expired?)");
        }
        Guild guild = this.shardManager.getGuildById(followUp.getGuildId());
        if (guild == null) {
            throw new IllegalArgumentException("Unknown guild");
        }
        TextChannel channel = guild.getTextChannelById(followUp.getChannelId());
        if (channel == null) {
            throw new IllegalArgumentException("Unknown channel");
        }
        channel.deleteMessageById(followUp.getMessageId())
            .queue(Utils.noop(), Utils.noop());
    }

    @Override
    public long reply(long interaction, Message message, boolean mention) {
        FollowUp followUp = this.find(interaction);
        if (followUp == null) {
            throw new IllegalArgumentException("Unknown interaction (Expired?)");
        }
        Guild guild = this.shardManager.getGuildById(followUp.getGuildId());
        if (guild == null) {
            throw new IllegalArgumentException("Unknown guild");
        }
        TextChannel channel = guild.getTextChannelById(followUp.getChannelId());
        if (channel == null) {
            throw new IllegalArgumentException("Unknown channel");
        }
        long interactionReply = this.snowflake.next();
        channel.retrieveMessageById(followUp.getMessageId())
            .flatMap(msg -> msg.reply(this.adapter.toMessage(message, guild)).mentionRepliedUser(mention))
            .queue(msg -> this.save(interactionReply, msg), Utils.noop());
        return interactionReply;
    }

    // -- FollowUp Handling

    private void save(long id, net.dv8tion.jda.api.entities.Message message) {
        FollowUp followUp = new FollowUp();
        followUp.setId(id);
        followUp.setChannelId(message.getChannel().getIdLong());
        followUp.setGuildId(message.getGuild().getIdLong());
        followUp.setMessageId(message.getIdLong());
        try {
            this.redis.opsForValue()
                .set("FollowUp:" + id, objectMapper.writeValueAsString(followUp), 5, TimeUnit.MINUTES);
        } catch (JsonProcessingException ex) {
            log.error("Failed to serialize follow-up", ex);
        }
    }

    private FollowUp find(long id) {
        Boolean has = this.redis.hasKey("FollowUp:" + id);
        if (has != null && has) {
            try {
                return objectMapper.readValue(this.redis.opsForValue()
                    .get("FollowUp:" + id), FollowUp.class);
            } catch (JsonProcessingException ex) {
                log.error("Failed to serialize follow-up", ex);
            }
        }
        return null;
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
