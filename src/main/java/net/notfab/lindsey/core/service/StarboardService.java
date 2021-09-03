package net.notfab.lindsey.core.service;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.notfab.lindsey.core.repositories.sql.StarboardRepository;
import net.notfab.lindsey.core.repositories.sql.StarboardSettingsRepository;
import net.notfab.lindsey.shared.entities.Starboard;
import net.notfab.lindsey.shared.entities.profile.server.StarboardSettings;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class StarboardService {

    private final StarboardRepository repository;
    private final StarboardSettingsRepository settingsRepository;

    public StarboardService(StarboardRepository repository, StarboardSettingsRepository settingsRepository) {
        this.repository = repository;
        this.settingsRepository = settingsRepository;
    }

    @NotNull
    public Starboard getStarboard(Message message, TextChannel starboardChannel) {
        Optional<Starboard> oStarboard;
        if (message.getChannel().getId().equals(starboardChannel.getId())) {
            oStarboard = repository.findByStarboardMessageId(message.getIdLong());
        } else {
            oStarboard = repository.findById(message.getIdLong());
        }
        if (oStarboard.isEmpty()) {
            Starboard starboard = new Starboard();
            starboard.setId(message.getIdLong());
            starboard.setChannelId(message.getChannel().getIdLong());
            starboard.setGuildId(message.getGuild().getIdLong());
            return starboard;
        } else {
            return oStarboard.get();
        }
    }

    public void delete(Starboard starboard) {
        this.repository.deleteById(starboard.getId());
    }

    public void save(Starboard starboard) {
        this.repository.save(starboard);
    }

    public TextChannel getChannel(Guild guild) {
        StarboardSettings settings = this.settingsRepository.findById(guild.getIdLong())
            .orElse(new StarboardSettings());
        if (!settings.isEnabled()) {
            return null;
        }
        Long channel = settings.getChannel();
        if (channel == null) {
            return null;
        } else {
            return guild.getTextChannelById(channel);
        }
    }


}
