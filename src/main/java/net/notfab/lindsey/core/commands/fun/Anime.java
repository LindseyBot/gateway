package net.notfab.lindsey.core.commands.fun;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.notfab.lindsey.framework.command.Bundle;
import net.notfab.lindsey.framework.command.Command;
import net.notfab.lindsey.framework.command.CommandDescriptor;
import net.notfab.lindsey.framework.command.Modules;
import net.notfab.lindsey.framework.command.help.HelpArticle;
import net.notfab.lindsey.framework.command.help.HelpPage;
import net.notfab.lindsey.framework.i18n.Messenger;
import net.notfab.lindsey.framework.i18n.Translator;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Arrays;

@Component
public class Anime implements Command {

    private static final OkHttpClient client = new OkHttpClient().newBuilder()
            .followSslRedirects(true)
            .build();

    @Autowired
    private Translator i18n;

    @Autowired
    private Messenger msg;

    @Override
    public CommandDescriptor getInfo() {
        return new CommandDescriptor.Builder()
                .name("anime")
                .module(Modules.FUN)
                .permission("commands.anime", "permissions.command")
                .build();
    }

    @Override
    public boolean execute(Member member, TextChannel channel, String[] args, Bundle bundle) throws Exception {
        Request req = new Request.Builder()
                .url("https://kitsu.io/api/edge/anime?filter[text]=" + Arrays.toString(args))
                .get()
                .build();

        Response resp = client.newCall(req).execute();
        JSONObject obj = new JSONObject(resp.body().string());
        JSONObject atr = obj.getJSONArray("data").getJSONObject(0).getJSONObject("attributes");
        String link = "https://kitsu.io/anime/" + obj.getJSONArray("data").getJSONObject(0)
                .getJSONObject("links").getString("self").split("anime/")[1];
        EmbedBuilder embed = new EmbedBuilder();

        boolean nsfw = false;
        if (!atr.isNull("nsfw")) {
            if (atr.getBoolean("nsfw")) {
                nsfw = true;
                embed.addField("NSFW", "Yes", true);
            } else {
                embed.addField("NSFW", "No", true);
            }
        }

        if (!nsfw || channel.isNSFW()) {
            if (atr.getJSONObject("titles").has("en")) {
                embed.setTitle(atr.getJSONObject("titles").getString("en") + " - " + atr.getJSONObject("titles").getString("ja_jp"), link);
            } else {
                embed.setTitle(atr.getJSONObject("titles").getString("en_jp") + " - " + atr.getJSONObject("titles").getString("ja_jp"), link);
            }

            embed.setFooter(i18n.get(member, "commands.fun.anime.request") + member.getEffectiveName() + "#" + member.getUser().getDiscriminator(),
                    member.getUser().getEffectiveAvatarUrl());
            if (!atr.isNull("synopsis")) {
                embed.setDescription(atr.getString("synopsis"));
            }

            if (!atr.getJSONObject("posterImage").isNull("original")) {
                embed.setThumbnail(atr.getJSONObject("posterImage").getString("original"));
            }

            if (!atr.isNull("status")) {
                embed.addField(i18n.get(member, "commands.fun.anime.status"), StringUtils.capitalize(atr.getString("status")), true);
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
                embed.addField(i18n.get(member, "commands.fun.anime.next"), atr.getString("nextRelease"), true);
            }

            if (!atr.isNull("ageRating") & atr.has("ageRatingGuide")) {
                embed.addField(i18n.get(member, "commands.fun.anime.age"), atr.getString("ageRating") + " - " + atr.getString("ageRatingGuide"), true);
            }

            msg.send(channel, embed.build());
            return true;
        } else {
            msg.send(channel, i18n.get(member, "core.not_nsfw"));
            return false;
        }
    }

    @Override
    public HelpArticle help(Member member) {
        HelpPage page = new HelpPage("anime")
                .text("commands.fun.anime.description")
                .usage("L!anime <name>")
                .permission("commands.anime")
                .addExample("L!anime konosuba");
        return HelpArticle.of(page);
    }

}
