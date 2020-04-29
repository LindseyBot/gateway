package net.notfab.lindsey.core.commands.fun;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.notfab.lindsey.framework.command.Bundle;
import net.notfab.lindsey.framework.command.Command;
import net.notfab.lindsey.framework.command.CommandDescriptor;
import net.notfab.lindsey.framework.command.Modules;
import net.notfab.lindsey.utils.Messenger;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONObject;
import org.springframework.util.StringUtils;

import java.util.Arrays;

public class Anime implements Command {

    private static final OkHttpClient client = new OkHttpClient().newBuilder()
            .followSslRedirects(true)
            .build();

    @Override
    public CommandDescriptor getInfo() {
        return new CommandDescriptor.Builder()
                .name("anime")
                .module(Modules.FUN)
                .permission("commands.anime", "Permission to use the base command")
                .permission("commands.anime.nsfw", "Permission to use the command on nsfw channels")
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

        if (atr.getJSONObject("titles").has("en")) {
            embed.setTitle(atr.getJSONObject("titles").getString("en") + " - " + atr.getJSONObject("titles").getString("ja_jp"), link);
        } else {
            embed.setTitle(atr.getJSONObject("titles").getString("en_jp") + " - " + atr.getJSONObject("titles").getString("ja_jp"), link);
        }

        //embed.setColor(new Color(255, 0, 54));
        embed.setFooter("Requested by " + member.getEffectiveName() + "#" + member.getUser().getDiscriminator(),
                member.getUser().getEffectiveAvatarUrl());

        if (!atr.isNull("synopsis")) {
            embed.setDescription(atr.getString("synopsis"));
        }


        if (!atr.getJSONObject("posterImage").isNull("original")) {
            embed.setThumbnail(atr.getJSONObject("posterImage").getString("original"));
        }

        if (!atr.isNull("status")) {
            embed.addField("Status", StringUtils.capitalize(atr.getString("status")), true);
        }

        if (!atr.isNull("episodeCount")) {
            int eps = atr.getInt("episodeCount");
            embed.addField("Episodes", String.valueOf(eps), true);
        }

        if (!atr.isNull("averageRating")) {
            String rating = atr.getString("averageRating");
            embed.addField("Rating", rating + " / 100", true);
        }

        if (!atr.isNull("ratingRank")) {
            int rank = atr.getInt("ratingRank");
            embed.addField("Rank", String.valueOf(rank), true);
        }

        if (!atr.isNull("popularityRank")) {
            embed.addField("Popularity", String.valueOf(atr.getInt("popularityRank")), true);
        }

        if (!atr.isNull("startDate")) {
            embed.addField("First Airing", atr.getString("startDate"), true);
        }

        if (!atr.isNull("endDate")) {
            embed.addField("Last Airing", atr.getString("endDate"), true);
        }

        if (!atr.isNull("nextRelease")) {
            embed.addField("Next Airing", atr.getString("nextRelease"), true);
        }

        if (!atr.isNull("ageRating") & atr.has("ageRatingGuide")) {
            embed.addField("Age Rating", atr.getString("ageRating") + " - " + atr.getString("ageRatingGuide"), true);
        }

        if (!atr.isNull("nsfw")) {
            if (atr.getBoolean("nsfw")) {
                embed.addField("NSFW", "Yes", true);
            } else {
                embed.addField("NSFW", "No", true);
            }
        }
        Messenger.send(channel, embed.build());
        return false;
    }

}
