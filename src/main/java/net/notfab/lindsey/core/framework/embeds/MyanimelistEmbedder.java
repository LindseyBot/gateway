package net.notfab.lindsey.core.framework.embeds;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.notfab.lindsey.core.framework.i18n.Translator;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class MyanimelistEmbedder implements WebsiteEmbedder {

    @Autowired
    private Translator i18n;

    private final Pattern pattern = Pattern.compile("^(?:https?://)?(?:www\\.)?(?:myanimelist\\.net)(?:/anime/)([\\w-]+/[\\w-]+)(?:[?\\w.=]*)$");

    @Override
    public boolean isSupported(String url) {
        return pattern.matcher(url).find();
    }

    @Override
    public MessageEmbed getEmbed(String url, Member member, boolean nsfw) throws IOException {
        Matcher matcher = this.pattern.matcher(url);
        if (!matcher.find()) {
            return null;
        }
        String args = matcher.group(1);
        OkHttpClient client = new OkHttpClient();
        EmbedBuilder embed = new EmbedBuilder();
        Request request = new Request.Builder()
            .url("https://api.jikan.moe/v3/search/anime?q=" + args)
            .get()
            .build();
        Response resp = client.newCall(request).execute();
        JSONObject obj = new JSONObject(resp.body().string());
        JSONObject anime = obj.getJSONArray("results").getJSONObject(0);
        String rate = "";
        if (!anime.isNull("rated")) {
            rate = anime.getString("rated");
            embed.addField(i18n.get(member, "commands.fun.anime.age"), rate, true);
        }
        if (rate.equals("Rx") && !nsfw) {
            return null;
        }
        embed.setTitle(anime.getString("title"), anime.getString("url"));
        if (!anime.isNull("synopsis")) {
            embed.setDescription(anime.getString("synopsis"));
        }
        if (!anime.isNull("image_url")) {
            embed.setThumbnail(anime.getString("image_url"));
        }
        if (!anime.isNull("type")) {
            embed.addField(i18n.get(member, "commands.fun.anime.type"), anime.getString("type"), true);
        }
        if (!anime.isNull("airing")) {
            if (anime.getBoolean("airing")) {
                embed.addField(i18n.get(member, "commands.fun.anime.status"), i18n.get(member, "commands.fun.anime.airing"), true);
            } else {
                embed.addField(i18n.get(member, "commands.fun.anime.status"), i18n.get(member, "commands.fun.anime.finished"), true);
            }
        }
        if (!anime.isNull("episodes")) {
            embed.addField(i18n.get(member, "commands.fun.anime.episodes"), Integer.toString(anime.getInt("episodes")), true);
        }
        if (!anime.isNull("score")) {
            embed.addField(i18n.get(member, "commands.fun.anime.score"), Integer.toString(anime.getInt("score")), true);
        }
        if (!anime.isNull("members")) {
            embed.addField(i18n.get(member, "commands.fun.anime.members"), Integer.toString(anime.getInt("members")), true);
        }
        if (!anime.isNull("start_date")) {
            embed.addField(i18n.get(member, "commands.fun.anime.first"), anime.getString("start_date").substring(0, 10), true);
        }
        if (!anime.isNull("end_date")) {
            embed.addField(i18n.get(member, "commands.fun.anime.last"), anime.getString("end_date").substring(0, 10), true);
        }
        embed.setFooter(i18n.get(member, "core.request", member.getEffectiveName() + "#" + member.getUser().getDiscriminator()),
            member.getUser().getEffectiveAvatarUrl());
        return embed.build();
    }

}