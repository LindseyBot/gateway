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
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.awt.Color;

@Component
public class Picarto implements Command {

    @Autowired
    private Translator i18n;

    @Autowired
    private Messenger msg;

    @Override
    public CommandDescriptor getInfo() {
        return new CommandDescriptor.Builder()
            .name("picarto")
            .module(Modules.FUN)
            .permission("commands.picarto", "permissions.command")
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
            .url("https://api.picarto.tv/v1/channel/name/" + args[0])
            .get()
            .build();
        Response resp = client.newCall(request).execute();
        JSONObject obj = new JSONObject(resp.body().string());
        String nsfw = String.valueOf(obj.getBoolean("adult"));
        nsfw = nsfw.substring(0, 1).toUpperCase() + nsfw.substring(1).toLowerCase();
        if (nsfw.equals("True") && !channel.isNSFW()) {
            msg.send(channel, i18n.get(member, "core.not_nsfw"));
            return false;
        }
        EmbedBuilder embed = new EmbedBuilder();
        String title = obj.getString("name");
        embed.setThumbnail(obj.getString("avatar"));
        if (obj.getBoolean("online")) {
            title = title + " - Online";
            embed.addField(i18n.get(member, "commands.lives.title"), obj.getString("title"), true);
            embed.addField(i18n.get(member, "commands.lives.category"), obj.getString("category"), true);
            embed.addField("NSFW", nsfw, true);
            embed.addField(i18n.get(member, "commands.lives.viewers"), String.valueOf(obj.getInt("viewers")), true);
            embed.setImage(obj.getJSONObject("thumbnails").getString("web"));
            embed.setColor(Color.red);
        } else {
            embed.addField(i18n.get(member, "commands.lives.last"), obj.getString("last_live").substring(0, 9), true);
            embed.addField(i18n.get(member, "commands.lives.category"), obj.getString("category"), true);
            embed.addField("NSFW", String.valueOf(obj.getBoolean("adult")), true);
            embed.addField(i18n.get(member, "commands.lives.total"), String.valueOf(obj.getInt("viewers_total")), true);
            embed.setColor(Color.gray);
        }
        embed.setTitle(title, "https://picarto.tv/" + obj.getString("name"));
        embed.addField(i18n.get(member, "commands.lives.followers"), String.valueOf(obj.getInt("followers")), true);
        embed.addField(i18n.get(member, "commands.lives.language"), obj.getJSONArray("languages").getJSONObject(0).getString("name"), true);
        embed.addField(i18n.get(member, "commands.lives.tags"), obj.getJSONArray("tags").toString(), true);
        embed.setFooter(i18n.get(member, "core.request", member.getEffectiveName() + "#" + member.getUser().getDiscriminator()),
            member.getUser().getEffectiveAvatarUrl());

        msg.send(channel, embed.build());
        return true;
    }

    @Override
    public HelpArticle help(Member member) {
        HelpPage page = new HelpPage("picarto")
            .text("commands.lives.picarto")
            .usage("L!picarto <streamer>")
            .permission("commands.picarto")
            .addExample("L!picarto lyndsey");
        return HelpArticle.of(page);
    }

}
