package net.notfab.lindsey.framework.options;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildChannel;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class OptionManager {

    @Getter
    private static OptionManager instance;
    private final StringRedisTemplate redis;
    private final Map<String, Option> optionMap = new HashMap<>();

    public OptionManager(ObjectMapper objectMapper, StringRedisTemplate redis) {
        this.redis = redis;
        CollectionType collectionType = objectMapper.getTypeFactory()
            .constructCollectionType(List.class, Option.class);
        try (InputStream stream = new ClassPathResource("options.json").getInputStream()) {
            List<Option> options = objectMapper.readValue(stream, collectionType);
            options.forEach(o -> this.optionMap.put(o.getName().toLowerCase(), o));
            log.info("Loaded " + options.size() + " options.");
        } catch (IOException ex) {
            log.error("Failed to register options", ex);
        }
        instance = this;
    }

    public Option find(String name) {
        return this.optionMap.get(name);
    }

    public <T> T get(Option option, Guild guild) {
        if (!redis.opsForHash().hasKey("Settings:" + guild.getId(), option.getName())) {
            String fallback = option.getFallback();
            if (fallback == null) {
                return null;
            } else {
                return option.getType().parse(guild, fallback);
            }
        }
        Object object = redis.opsForHash().get("Settings:" + guild.getId(), option.getName());
        return option.getType().parse(guild, object);
    }

    public void set(Option option, Guild guild, Object value) throws IllegalArgumentException {
        String target;
        if (value == null) {
            target = option.getFallback();
        } else {
            Object parsed = option.getType().parse(guild, value);
            if (parsed instanceof GuildChannel) {
                parsed = ((GuildChannel) parsed).getId();
            } else {
                parsed = String.valueOf(parsed);
            }
            target = (String) parsed;
        }
        if (target == null || target.equals(option.getFallback())) {
            redis.opsForHash().delete("Settings:" + guild.getId(), option.getName());
        } else {
            redis.opsForHash().put("Settings:" + guild.getId(), option.getName(), target);
        }
    }

}
