package net.notfab.lindsey.core.service;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.notfab.lindsey.core.framework.profile.ProfileManager;
import net.notfab.lindsey.core.framework.profile.guild.Starboard;
import net.notfab.lindsey.core.repositories.mongo.StarboardRepository;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class StarboardService {

    private final StarboardRepository repository;
    private final ProfileManager profiles;

    public StarboardService(StarboardRepository repository, ProfileManager profiles) {
        this.repository = repository;
        this.profiles = profiles;
    }

    @NotNull
    public Starboard getStarboard(Message message, TextChannel starboardChannel) {
        Optional<Starboard> oStarboard;
        if (message.getChannel().getId().equals(starboardChannel.getId())) {
            oStarboard = repository.findByStarboardMessageId(message.getIdLong());
        } else {
            oStarboard = repository.findById(message.getId());
        }
        if (oStarboard.isEmpty()) {
            Starboard starboard = new Starboard();
            starboard.setId(message.getId());
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
        Long channel = this.profiles.get(guild).getStarboardChannelId();
        if (channel == null) {
            return null;
        } else {
            return guild.getTextChannelById(channel);
        }
    }


}
