package net.notfab.lindsey.core.commands.fun;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
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

import java.util.Arrays;

@Component
public class MyAnimeList implements Command {

    @Autowired
    private Translator i18n;

    @Autowired
    private Messenger msg;

    @Override
    public CommandDescriptor getInfo() {
        return new CommandDescriptor.Builder()
            .name("myanimelist")
            .alias("mal")
            .module(Modules.FUN)
            .permission("commands.myanimelist", "permissions.command")
            .build();
    }

    @Override
    public boolean execute(Member member, TextChannel channel, String[] args, Message message, Bundle bundle) throws Exception {
        if (args.length == 0) {
            HelpArticle article = this.help(member);
            article.send(channel, member, args, msg, i18n);
            return false;
        }

        OkHttpClient client = new OkHttpClient();
        EmbedBuilder embed = new EmbedBuilder();

        Request request = new Request.Builder()
            .url("https://api.jikan.moe/v3/search/anime?q=" + Arrays.toString(args))
            .get()
            .build();
        Response resp = client.newCall(request).execute();
        JSONObject obj = new JSONObject(resp.body().string());

        if (obj.getJSONArray("results").isEmpty()) {
            msg.send(channel, i18n.get(member, "commands.fun.anime.404"));
            return true;
        }

        JSONObject anime = obj.getJSONArray("results").getJSONObject(0);

        String rate = "";
        if (!anime.isNull("rated")) {
            rate = anime.getString("rated");
            embed.addField(i18n.get(member, "commands.fun.anime.age"), rate, true);
        }
        if (rate.equals("Rx") && !channel.isNSFW()) {
            msg.send(channel, i18n.get(member, "core.not_nsfw"));
            return false;
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

        embed.setFooter(i18n.get(member, "commands.fun.anime.request") + " " + member.getEffectiveName() + "#" + member.getUser().getDiscriminator(),
            member.getUser().getEffectiveAvatarUrl());

        msg.send(channel, embed.build());
        return true;
    }

    @Override
    public HelpArticle help(Member member) {
        HelpPage page = new HelpPage("myanimelist")
            .text("commands.fun.anime.description")
            .usage("L!myanimelist <name>")
            .permission("commands.myanimelist")
            .addExample("L!mal One Piece")
            .addExample("L!myanimelist konosuba");
        return HelpArticle.of(page);
    }

}
