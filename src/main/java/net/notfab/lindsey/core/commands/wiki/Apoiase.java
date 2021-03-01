package net.notfab.lindsey.core.commands.wiki;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.notfab.lindsey.core.framework.GFXUtils;
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

@Component
public class Apoiase implements Command {

    @Autowired
    private Translator i18n;

    @Autowired
    private Messenger msg;

    @Override
    public CommandDescriptor getInfo() {
        return new CommandDescriptor.Builder()
            .name("apoiase")
            .module(Modules.GAME_WIKI)
            .permission("commands.apoiase", "permissions.command")
            .build();
    }

    @Override
    public boolean execute(Member member, TextChannel channel, String[] args, Message message, Bundle bundle) throws Exception {
        if (args.length == 0) {
            HelpArticle article = this.help(member);
            article.send(channel, member, args, msg, i18n);
            return false;
        }
        String name = "";
        OkHttpClient client = new OkHttpClient();
        EmbedBuilder embed = new EmbedBuilder();
        Request request = new Request.Builder()
            .url("https://apoia.se/api/v1/users/campaign?page=0&q=" + this.argsToString(args, 0))
            .get()
            .build();
        Response resp = client.newCall(request).execute();
        JSONObject obj = new JSONObject(resp.body().string());
        try {
            name = obj.getJSONArray("campaigns").getJSONObject(0).getJSONObject("campaign").getString("slug");
        } catch (Exception e) {
            msg.send(channel, i18n.get(member, "core.not_found", this.argsToString(args, 0)));
            return false;
        }
        request = new Request.Builder()
            .url("https://apoia.se/api/v1/users/" + name)
            .get()
            .build();
        resp = client.newCall(request).execute();
        obj = new JSONObject(resp.body().string());
        JSONObject campaigns = obj.getJSONArray("campaigns").getJSONObject(0);
        JSONObject state = obj.getJSONArray("address").getJSONObject(0);
        boolean nsfw = false;
        if (campaigns.getBoolean("explicit")) {
            nsfw = true;
        }
        if (nsfw && !channel.isNSFW()) {
            msg.send(channel, i18n.get(member, "core.not_nsfw"));
            return false;
        }
        embed.setTitle(campaigns.getString("name"), "https://www.apoia.se/" + campaigns.getString("slug"));
        if (!campaigns.getJSONObject("about").isNull("slogan")) {
            embed.setDescription(campaigns.getJSONObject("about").getString("slogan"));
        }
        embed.setFooter(i18n.get(member, "core.request", member.getEffectiveName() + "#" + member.getUser().getDiscriminator()),
            member.getUser().getEffectiveAvatarUrl());
        embed.setImage(campaigns.getJSONObject("about").getString("photo"));
        embed.addField(i18n.get(member, "commands.wiki.crowdfunding.category"), campaigns.getJSONArray("categories").getString(0), true);
        if (!campaigns.getJSONArray("goals").isEmpty()) {
            embed.addField(i18n.get(member, "commands.wiki.crowdfunding.goal"), "R$" + campaigns.getJSONArray("goals").getJSONObject(0).getInt("value"), true);
        }
        embed.addField(i18n.get(member, "commands.wiki.crowdfunding.creator"), obj.getString("username"), true);
        embed.addField(i18n.get(member, "commands.wiki.crowdfunding.createdDate"), campaigns.getString("createdDate").split("T")[0], true);
        embed.addField(i18n.get(member, "commands.wiki.crowdfunding.city"), state.getString("city") + " - " + state.getString("state"), true);
        embed.addField(i18n.get(member, "core.nsfw"), Boolean.toString(nsfw), true);
        if (campaigns.getString("status").equals("published")) {
            embed.setColor(GFXUtils.GREEN);
        } else {
            embed.setColor(GFXUtils.RED);
        }
        msg.send(channel, embed.build());
        return true;
    }

    @Override
    public HelpArticle help(Member member) {
        HelpPage page = new HelpPage("apoiase")
            .text("commands.wiki.crowdfunding.description")
            .usage("L!apoiase <name>")
            .permission("commands.apoiase")
            .addExample("L!apoiase death-tale");
        return HelpArticle.of(page);
    }

}
