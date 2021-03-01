package net.notfab.lindsey.core.commands.wiki;

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
public class Kickstarter implements Command {

    @Autowired
    private Translator i18n;

    @Autowired
    private Messenger msg;

    @Override
    public CommandDescriptor getInfo() {
        return new CommandDescriptor.Builder()
            .name("kickstarter")
            .module(Modules.GAME_WIKI)
            .permission("commands.kickstarter", "permissions.command")
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
            .url("https://www.kickstarter.com/projects/search.json?search=&term=" + this.argsToString(args, 0))
            .get()
            .build();
        Response resp = client.newCall(request).execute();
        JSONObject obj;
        try {
            obj = new JSONObject(resp.body().string()).getJSONArray("projects").getJSONObject(0);
        } catch (Exception e) {
            msg.send(channel, i18n.get(member, "core.not_found", this.argsToString(args, 0)));
            return false;
        }
        String currency = obj.getString("currency_symbol");
        embed.setTitle(obj.getString("name"), obj.getJSONObject("urls").getJSONObject("web").getString("project"));
        embed.setDescription(obj.getString("blurb"));
        embed.setFooter(i18n.get(member, "commands.fun.anime.request") + " " + member.getEffectiveName() + "#" + member.getUser().getDiscriminator(),
            member.getUser().getEffectiveAvatarUrl());
        embed.setImage(obj.getJSONObject("photo").getString("full"));
        embed.addField(i18n.get(member, "commands.wiki.crowdfunding.backers"), Integer.toString(obj.getInt("backers_count")), true);
        embed.addField(i18n.get(member, "commands.wiki.crowdfunding.goal"), currency + obj.getInt("goal"), true);
        embed.addField(i18n.get(member, "commands.wiki.crowdfunding.pledged"), currency + obj.getInt("pledged"), true);
        try {
            embed.addField(i18n.get(member, "commands.wiki.crowdfunding.creator"), obj.getJSONObject("creator").getString("slug"), true);
        } catch (Exception e) {
            embed.addField(i18n.get(member, "commands.wiki.crowdfunding.creator"), obj.getJSONObject("creator").getString("name"), true);
        }
        embed.addField(i18n.get(member, "commands.wiki.crowdfunding.city"), obj.getJSONObject("location").getString("displayable_name"), true);
        embed.addField(i18n.get(member, "commands.wiki.crowdfunding.category"), obj.getJSONObject("category").getString("name"), true);
        if (obj.getString("state").equals("live")) {
            embed.setColor(Color.GREEN);
        } else {
            embed.setColor(Color.RED);
        }
        msg.send(channel, embed.build());
        return true;
    }

    @Override
    public HelpArticle help(Member member) {
        HelpPage page = new HelpPage("kickstarter")
            .text("commands.wiki.crowdfunding.description")
            .usage("L!kickstarter <name>")
            .permission("commands.kickstarter")
            .addExample("L!kickstarter exemple");
        return HelpArticle.of(page);
    }

}
