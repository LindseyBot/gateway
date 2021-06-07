package net.notfab.lindsey.core.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.lindseybot.enums.AutoModFeature;
import net.lindseybot.services.EventService;
import net.notfab.eventti.EventHandler;
import net.notfab.eventti.Listener;
import net.notfab.lindsey.core.automod.AutoModerator;
import net.notfab.lindsey.core.framework.Utils;
import net.notfab.lindsey.core.framework.events.StrikeReceivedEvent;
import net.notfab.lindsey.core.repositories.sql.AutoModRepository;
import net.notfab.lindsey.shared.entities.profile.member.Strike;
import net.notfab.lindsey.shared.entities.profile.server.AutoModSettings;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class AutoModService implements Listener {

    private final StringRedisTemplate pool;
    private final ObjectMapper objectMapper;
    private final AutoModRepository settingsRepository;
    private final ShardManager shardManager;
    private final Map<AutoModFeature, AutoModerator> moderators = new HashMap<>();

    public AutoModService(
        StringRedisTemplate pool, ObjectMapper objectMapper,
        AutoModRepository settingsRepository,
        List<AutoModerator> moderators,
        EventService events,
        ShardManager shardManager) {
        this.pool = pool;
        this.objectMapper = objectMapper;
        this.settingsRepository = settingsRepository;
        this.shardManager = shardManager;
        moderators.forEach(moderator -> this.moderators.put(moderator.feature(), moderator));
        events.addListener(this);
    }

    @EventHandler
    public void onStrike(StrikeReceivedEvent event) {
        Strike strike = event.getStrike();
        log.debug("User " + strike.getUser() + " received a strike on " + strike.getGuild() + ", totalling " + event.getTotal());
        if (event.getTotal() < 10) {
            return;
        }
        AutoModSettings settings = this.settingsRepository.findById(strike.getGuild())
            .orElse(new AutoModSettings());
        if (settings.isBanEnabled()) {
            Guild guild = this.shardManager.getGuildById(strike.getGuild());
            if (guild == null) {
                return;
            }
            guild.ban(String.valueOf(strike.getUser()), 0, strike.getReason())
                .queue(Utils.noop(), Utils.noop());
        }
    }

    /**
     * @param guild Guild to check.
     * @return If auto-moderation is enabled on this guild in any form.
     */
    public boolean isEnabled(long guild) {
        return this.pool.opsForHash().hasKey("Lindsey:AutoMod", String.valueOf(guild));
    }

    /**
     * Lists all auto-moderation features enabled for a specific guild.
     *
     * @param guild Guild to retrieve.
     * @return List of AutoModFeature(s).
     */
    private List<AutoModFeature> getFeatures(long guild) {
        String response = (String) this.pool.opsForHash()
            .get("Lindsey:AutoMod", String.valueOf(guild));
        AutoModFeature[] features;
        try {
            features = objectMapper.readValue(response, AutoModFeature[].class);
        } catch (JsonProcessingException e) {
            return Collections.emptyList();
        }
        return Arrays.asList(features);
    }

    /**
     * Moderates a message, this loops through all auto-moderators that are active for
     * this guild and stops at the first that finds an offense.
     *
     * @param message The message to moderate.
     * @param author  The message's author.
     */
    public boolean moderate(@NotNull Message message, @NotNull Member author) {
        List<AutoModFeature> features = this.getFeatures(message.getGuild().getIdLong());
        boolean actionTaken = false;
        for (AutoModFeature feature : features) {
            AutoModerator moderator = this.moderators.get(feature);
            if (moderator == null) {
                continue;
            }
            try {
                actionTaken = moderator.moderate(message, author);
                if (actionTaken) {
                    break;
                }
            } catch (Exception ex) {
                log.error("Failed to auto-moderate message", ex);
            }
        }
        return actionTaken;
    }

}
