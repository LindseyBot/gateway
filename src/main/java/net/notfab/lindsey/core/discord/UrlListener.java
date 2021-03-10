package net.notfab.lindsey.core.discord;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.jodah.expiringmap.ExpirationListener;
import net.notfab.lindsey.core.Lindsey;
import net.notfab.lindsey.core.framework.embeds.WebsiteEmbedder;
import net.notfab.lindsey.core.framework.profile.ProfileManager;
import net.notfab.lindsey.shared.entities.profile.UserProfile;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UrlListener extends ListenerAdapter implements ExpirationListener<Long, String> {

    private final ProfileManager profiles;
    private final List<WebsiteEmbedder> lista;

    public UrlListener(Lindsey lindsey, ProfileManager profiles, List<WebsiteEmbedder> embedders) {
        lindsey.addEventListener(this);
        this.profiles = profiles;
        this.lista = embedders;
    }

    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        Member member = event.getMember();
        TextChannel channel = event.getChannel();
        MessageEmbed embed;
        if (member == null || event.getAuthor().isBot() || event.isWebhookMessage()) {
            return;
        }
        for (WebsiteEmbedder embedder : this.lista) {
            if (!embedder.isSupported(event.getMessage().getContentRaw())) {
                continue;
            }
            try {
                embed = embedder.getEmbed(event.getMessage().getContentRaw(), member, channel.isNSFW());
            } catch (Exception e) {
                System.out.println(e);
                return;
            }
            if (embed == null) {
                return;
            }
            event.getMessage()
                .delete()
                .flatMap((d) -> event.getChannel().sendMessage(embed))
                .queue();
            break;
        }
    }

    @Override
    public void expired(Long id, String name) {
        UserProfile profile = profiles.getUser(id);
        profile.setName(name);
        profile.setLastSeen(System.currentTimeMillis());
        profiles.save(profile);
    }

}
