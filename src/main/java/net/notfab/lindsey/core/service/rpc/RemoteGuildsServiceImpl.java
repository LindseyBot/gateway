package net.notfab.lindsey.core.service.rpc;

import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.lindseybot.entities.discord.*;
import net.lindseybot.utils.RabbitUtils;
import net.notfab.lindsey.core.framework.FakeBuilder;
import net.notfab.lindsey.core.framework.permissions.PermissionManager;
import net.notfab.lindsey.shared.rpc.services.RemoteGuildsService;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.remoting.service.AmqpInvokerServiceExporter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RemoteGuildsServiceImpl implements RemoteGuildsService {

    private final ShardManager shardManager;
    private final PermissionManager permissions;

    public RemoteGuildsServiceImpl(ShardManager shardManager, PermissionManager permissions) {
        this.shardManager = shardManager;
        this.permissions = permissions;
    }

    private boolean hasPermission(Guild guild, long userId) {
        if (userId == 87166524837613568L) {
            return true;
        }
        Member member = guild.retrieveMemberById(userId)
            .complete();
        if (member == null) {
            return false;
        }
        return this.permissions.hasPermission(member, "ADMIN");
    }

    @Override
    public FGuild getGuild(long id, long userId) {
        Guild guild = this.shardManager.getGuildById(id);
        if (guild == null) {
            throw new IllegalArgumentException("Unknown guild");
        }
        if (!this.hasPermission(guild, userId)) {
            throw new IllegalStateException("No permission");
        }
        FGuild result = FakeBuilder.toFake(guild);

        List<FRole> roles = guild.getRoles().stream()
            .map(FakeBuilder::toFake)
            .collect(Collectors.toList());
        result.setRoles(roles);

        List<GuildChannel> topChannels = guild.getChannels().stream()
            .filter(channel -> channel.getParent() == null)
            .collect(Collectors.toList());
        result.setChannels(this.parseChannels(topChannels));
        return result;
    }

    @Override
    public List<FGuild> getDetails(List<Long> ids, long userId) {
        List<FGuild> guilds = new ArrayList<>();
        ids.parallelStream().forEach(id -> {
            FGuild guild;
            try {
                guild = this.getGuild(id, userId);
            } catch (IllegalStateException | IllegalArgumentException ex) {
                return;
            }
            guilds.add(guild);
        });
        return guilds;
    }

    private List<FChannel> parseCategory(Category category, int pos) {
        List<FChannel> channels = new ArrayList<>();
        for (GuildChannel gChannel : category.getChannels()) {
            FChannel fChannel = new FChannel();
            fChannel.setId(gChannel.getIdLong());
            fChannel.setName(gChannel.getName());
            fChannel.setPosition(pos++);
            if (gChannel.getType() == ChannelType.STORE) {
                fChannel.setType(FChannelType.STORE);
            } else if (gChannel.getType() == ChannelType.VOICE) {
                fChannel.setType(FChannelType.VOICE);
            } else {
                fChannel.setType(FChannelType.TEXT);
            }
            channels.add(fChannel);
        }
        return channels;
    }

    private List<FChannel> parseChannels(List<GuildChannel> guildChannels) {
        List<FChannel> channels = new ArrayList<>();
        int pos = 0;
        for (GuildChannel gChannel : guildChannels) {
            if (gChannel instanceof Category) {
                FCategory fCategory = new FCategory();
                fCategory.setId(gChannel.getIdLong());
                fCategory.setName(gChannel.getName());
                fCategory.setPosition(pos);
                fCategory.setType(FChannelType.CATEGORY);
                fCategory.setChannels(this.parseCategory((Category) gChannel, pos + 1));
                channels.add(fCategory);
                pos = pos + (fCategory.getChannels().size() + 1);
            } else {
                FChannel fChannel = new FChannel();
                fChannel.setId(gChannel.getIdLong());
                fChannel.setName(gChannel.getName());
                fChannel.setPosition(pos);
                if (gChannel.getType() == ChannelType.STORE) {
                    fChannel.setType(FChannelType.STORE);
                } else if (gChannel.getType() == ChannelType.VOICE) {
                    fChannel.setType(FChannelType.VOICE);
                } else if (gChannel.getType() == ChannelType.TEXT) {
                    fChannel.setType(FChannelType.TEXT);
                    fChannel.setNsfw(((TextChannel) gChannel).isNSFW());
                }
                channels.add(fChannel);
                pos++;
            }
        }
        return channels;
    }

    // -- RabbitMQ RPC Setup

    @Bean(name = "RemoteGuildsQ")
    public Queue queue() {
        return new Queue(this.getRabbitName(), false, false, true);
    }

    @Bean(name = "RemoteGuildsE")
    public DirectExchange exchange() {
        return new DirectExchange(this.getRabbitName(), false, true);
    }

    @Bean(name = "RemoteGuildsB")
    public Binding binding(@Qualifier("RemoteGuildsQ") Queue queue, @Qualifier("RemoteGuildsE") DirectExchange exchange) {
        return BindingBuilder.bind(queue)
            .to(exchange)
            .withQueueName();
    }

    @Bean(name = "RemoteGuildsS")
    public AmqpInvokerServiceExporter exporter(@Qualifier("rpc") AmqpTemplate template) {
        AmqpInvokerServiceExporter exporter = new AmqpInvokerServiceExporter();
        exporter.setServiceInterface(RemoteGuildsService.class);
        exporter.setService(this);
        exporter.setAmqpTemplate(template);
        exporter.setMessageConverter(RabbitUtils.jacksonConverter());
        return exporter;
    }

    @Bean(name = "RemoteGuildsL")
    public SimpleMessageListenerContainer listener(
        ConnectionFactory factory,
        @Qualifier("RemoteGuildsS") AmqpInvokerServiceExporter exporter,
        @Qualifier("RemoteGuildsQ") Queue queue
    ) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(factory);
        container.setMessageListener(exporter);
        container.setQueues(queue);
        return container;
    }

}
