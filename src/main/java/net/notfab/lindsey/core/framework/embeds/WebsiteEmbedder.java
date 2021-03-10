package net.notfab.lindsey.core.framework.embeds;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.notfab.lindsey.shared.entities.profile.server.BetterEmbedsSettings;

import java.io.IOException;

public interface WebsiteEmbedder {

    boolean isSupported(String url);

    MessageEmbed getEmbed(String url, Member member, boolean nsfw) throws IOException;

    boolean isEnabled(BetterEmbedsSettings settings);

}
