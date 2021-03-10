package net.notfab.lindsey.core.framework.embeds;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.notfab.lindsey.core.framework.i18n.Translator;
import net.notfab.lindsey.shared.entities.profile.server.BetterEmbedsSettings;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class KitsuEmbedder implements WebsiteEmbedder {

    @Value("${bot.integrations.kitsu}")
    private String key;

    @Autowired
    private Translator i18n;

    private final Pattern pattern = Pattern.compile("^(?:https?://)?(?:www\\.)?(?:kitsu\\.io)(?:/anime)/([\\w-]+)(?:[?\\w.=/\\\\]*)$");

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
        Request req = new Request.Builder()
            .url("https://kitsu.io/api/edge/anime?filter[slug]=" + args)
            .addHeader("Authorization", "Bearer " + key)
            .get()
            .build();
        Response resp = client.newCall(req).execute();
        JSONObject obj = new JSONObject(resp.body().string());
        JSONObject atr = obj.getJSONArray("data").getJSONObject(0).getJSONObject("attributes");
        String link = "https://kitsu.io/anime/" + obj.getJSONArray("data").getJSONObject(0)
            .getJSONObject("links").getString("self").split("anime/")[1];
        EmbedBuilder embed = new EmbedBuilder();

        boolean adult = false;
        if (!atr.isNull("nsfw")) {
            if (atr.getBoolean("nsfw")) {
                adult = true;
                embed.addField("NSFW", "Yes", true);
            } else {
                embed.addField("NSFW", "No", true);
            }
        }
        if (adult && !nsfw) {
            return null;
        }
        if (atr.getJSONObject("titles").has("en")) {
            embed.setTitle(atr.getJSONObject("titles").getString("en") + " - " + atr.getJSONObject("titles").getString("ja_jp"), link);
        } else {
            embed.setTitle(atr.getJSONObject("titles").getString("en_jp") + " - " + atr.getJSONObject("titles").getString("ja_jp"), link);
        }
        embed.setFooter(i18n.get(member, "core.request", member.getEffectiveName() + "#" + member.getUser().getDiscriminator()),
            member.getUser().getEffectiveAvatarUrl());
        if (!atr.isNull("synopsis")) {
            embed.setDescription(atr.getString("synopsis"));
        }
        if (!atr.getJSONObject("posterImage").isNull("original")) {
            embed.setThumbnail(atr.getJSONObject("posterImage").getString("original"));
        }
        if (!atr.isNull("status")) {
            embed.addField(i18n.get(member, "commands.fun.anime.status"), (atr.getString("status")).toUpperCase(), true);
        }
        if (!atr.isNull("episodeCount")) {
            int eps = atr.getInt("episodeCount");
            embed.addField(i18n.get(member, "commands.fun.anime.episodes"), String.valueOf(eps), true);
        }
        if (!atr.isNull("averageRating")) {
            String rating = atr.getString("averageRating");
            embed.addField(i18n.get(member, "commands.fun.anime.rating"), rating + " / 100", true);
        }
        if (!atr.isNull("ratingRank")) {
            int rank = atr.getInt("ratingRank");
            embed.addField(i18n.get(member, "commands.fun.anime.rank"), String.valueOf(rank), true);
        }
        if (!atr.isNull("popularityRank")) {
            embed.addField(i18n.get(member, "commands.fun.anime.popularity"), String.valueOf(atr.getInt("popularityRank")), true);
        }
        if (!atr.isNull("startDate")) {
            embed.addField(i18n.get(member, "commands.fun.anime.first"), atr.getString("startDate"), true);
        }
        if (!atr.isNull("endDate")) {
            embed.addField(i18n.get(member, "commands.fun.anime.last"), atr.getString("endDate"), true);
        }
        if (!atr.isNull("nextRelease")) {
            embed.addField(i18n.get(member, "commands.fun.anime.next"), atr.getString("nextRelease").substring(0, 10), true);
        }
        if (!atr.isNull("ageRating") & !atr.isNull("ageRatingGuide")) {
            embed.addField(i18n.get(member, "commands.fun.anime.age"), atr.getString("ageRating") + " - " + atr.getString("ageRatingGuide"), true);
        }
        return embed.build();
    }

    @Override
    public boolean isEnabled(BetterEmbedsSettings settings) {
        return settings.isKitsu();
    }

}
