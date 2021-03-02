package net.notfab.lindsey.core.commands.fun;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.notfab.lindsey.core.framework.command.Bundle;
import net.notfab.lindsey.core.framework.command.Command;
import net.notfab.lindsey.core.framework.command.CommandDescriptor;
import net.notfab.lindsey.core.framework.command.Modules;
import net.notfab.lindsey.core.framework.command.help.HelpArticle;
import net.notfab.lindsey.core.framework.command.help.HelpPage;
import net.notfab.lindsey.core.framework.i18n.Messenger;
import net.notfab.lindsey.core.framework.i18n.Translator;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.awt.Color;

@Component
public class Twitch implements Command {

    @Autowired
    private Translator i18n;

    @Autowired
    private Messenger msg;

    @Value("${bot.integrations.twitchKey}")
    private String twitchKey;

    @Value("${bot.integrations.twitchClient}")
    private String twitchClient;

    @Override
    public CommandDescriptor getInfo() {
        return new CommandDescriptor.Builder()
            .name("twitch")
            .alias("live")
            .module(Modules.FUN)
            .permission("commands.twitch", "permissions.command")
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
        Request request = new Request.Builder()
            .url("https://api.twitch.tv/helix/streams?user_login=" + args[0])
            .addHeader("Client-Id", twitchClient)
            .addHeader("Authorization", "Bearer " + twitchKey)
            .get()
            .build();
        Response resp = client.newCall(request).execute();
        JSONArray arr = new JSONObject(resp.body().string()).getJSONArray("data");
        EmbedBuilder embed;

        if (arr.isEmpty()) {
            request = new Request.Builder()
                .url("https://api.twitch.tv/helix/search/channels?first=1&query=" + args[0])
                .addHeader("Client-Id", twitchClient)
                .addHeader("Authorization", "Bearer " + twitchKey)
                .get()
                .build();
            resp = client.newCall(request).execute();
            JSONObject obj = new JSONObject(resp.body().string()).getJSONArray("data").getJSONObject(0);

            String name = obj.getString("display_name");
            embed = new EmbedBuilder()
                .setTitle(name, "https://www.twitch.tv/" + name)
                .setThumbnail(obj.getString("thumbnail_url"))
                .addField(i18n.get(member, "commands.lives.online"), String.valueOf(obj.getBoolean("is_live")), true)
                .addField(i18n.get(member, "commands.lives.language"), obj.getString("broadcaster_language"), true)
                .addField(i18n.get(member, "commands.lives.title"), obj.getString("title"), false)
                .setColor(Color.gray)
                .setFooter(i18n.get(member, "core.request", member.getEffectiveName() + "#" + member.getUser().getDiscriminator()),
                    member.getUser().getEffectiveAvatarUrl());
            msg.send(channel, embed.build());
            return true;
        }

        JSONObject obj = arr.getJSONObject(0);
        String name = obj.getString("user_name");
        embed = new EmbedBuilder()
            .setTitle(name, "https://www.twitch.tv/" + name)
            .setImage(obj.getString("thumbnail_url").replace("{width}", "840").replace("{height}", "480"))
            .addField(i18n.get(member, "commands.lives.playing"), obj.getString("game_name"), true)
            .addField(i18n.get(member, "commands.lives.language"), obj.getString("language"), true)
            .addField(i18n.get(member, "commands.lives.viewers"), String.valueOf(obj.getInt("viewer_count")), true)
            .addField(i18n.get(member, "commands.lives.title"), obj.getString("title"), true)
            .setColor(Color.red)
            .setFooter(i18n.get(member, "core.request", member.getEffectiveName() + "#" + member.getUser().getDiscriminator()),
                member.getUser().getEffectiveAvatarUrl());
        msg.send(channel, embed.build());
        return true;
    }

    @Override
    public HelpArticle help(Member member) {
        HelpPage page = new HelpPage("twitch")
            .text("commands.lives.twitch")
            .usage("L!twitch <streamer>")
            .permission("commands.twitch")
            .addExample("L!twitch fabricio20");
        return HelpArticle.of(page);
    }

}
