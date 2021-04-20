package net.notfab.lindsey.core.framework.command;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;
import net.notfab.lindsey.shared.entities.commands.ExternalCommand;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class CommandRegistry {

    private final JedisPool redis;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ExpiringMap<String, ExternalCommand> commands = ExpiringMap.builder()
        .expiration(15, TimeUnit.MINUTES)
        .expirationPolicy(ExpirationPolicy.ACCESSED)
        .build();

    private final String COMMAND_REGISTRY_NAME = "Lindsey:Commands";
    private final String COMMAND_WORKER_NAME = "Lindsey:Workers:Commands";

    public CommandRegistry(JedisPool redis) {
        this.redis = redis;
    }

    // Fired when a worker leaves the network
    public void onUnregister(ExternalCommand command) {
        try (Jedis jedis = this.redis.getResource()) {
            long count = jedis.hincrBy(COMMAND_WORKER_NAME, command.getName(), 0L);
            if (count == 0) {
                this.commands.remove(command.getName());
                command.getAliases().forEach(this.commands::remove);
            }
        } catch (Exception ex) {
            log.error("Failed to unregister command", ex);
        }
    }

    public void register(ExternalCommand command) {
        List<String> names = new ArrayList<>();
        names.add(command.getName());
        names.addAll(command.getAliases());
        try (Jedis jedis = this.redis.getResource()) {
            String cmd = objectMapper.writeValueAsString(command);
            for (String name : names) {
                jedis.hset(COMMAND_REGISTRY_NAME, name, cmd);
            }
            jedis.hincrBy(COMMAND_WORKER_NAME, command.getName(), 1L);
        } catch (Exception ex) {
            log.error("Failed to register command", ex);
        }
    }

    public boolean exists(String commandName) {
        return this.get(commandName) != null;
    }

    public ExternalCommand get(String commandName) {
        commandName = commandName.toLowerCase();
        if (this.commands.containsKey(commandName)) {
            return this.commands.get(commandName);
        }
        try (Jedis jedis = this.redis.getResource()) {
            String raw = jedis.hget("Lindsey:Commands", commandName);
            if (raw == null) {
                return null;
            }
            ExternalCommand cmd = objectMapper.readValue(raw, ExternalCommand.class);
            this.commands.put(commandName, cmd);
            for (String alias : cmd.getAliases()) {
                this.commands.put(alias.toLowerCase(), cmd);
            }
            return cmd;
        } catch (Exception ex) {
            return null;
        }
    }

}
