package net.notfab.lindsey.core.commands.wiki;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.notfab.lindsey.core.framework.GFXUtils;
import net.notfab.lindsey.core.framework.command.Command;
import net.notfab.lindsey.core.framework.command.Modules;
import net.notfab.lindsey.core.framework.command.help.HelpArticle;
import net.notfab.lindsey.core.framework.command.help.HelpPage;
import net.notfab.lindsey.core.framework.i18n.Messenger;
import net.notfab.lindsey.core.framework.i18n.Translator;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class Catarse implements Command {

    @Autowired
    private Translator i18n;

    @Autowired
    private Messenger msg;

    @Override
    public CommandDescriptor getInfo() {
        return new CommandDescriptor.Builder()
            .name("catarse")
            .module(Modules.GAME_WIKI)
            .permission("commands.catarse", "permissions.command")
            .build();
    }

    @Override
    public boolean execute(Member member, TextChannel channel, String[] args, Message message, Bundle bundle) throws Exception {
        if (args.length == 0) {
            HelpArticle article = this.help(member);
            article.send(channel, member, args, msg, i18n);
            return false;
        }
        if (args[0].equals("user") && args.length > 1) {
            return getUser(args, channel, member);
        }
        return getProject(args, channel, member);
    }

    private boolean getProject(String[] args, TextChannel channel, Member member) throws IOException {
        OkHttpClient client = new OkHttpClient();
        EmbedBuilder embed = new EmbedBuilder();
        RequestBody body = new FormBody.Builder()
            .add("query", this.argsToString(args, 0))
            .build();
        Request request = new Request.Builder()
            .url("https://api.catarse.me/rpc/project_search")
            .post(body)
            .addHeader("Content-Type", "application/x-www-form-urlencoded")
            .build();
        Response resp = client.newCall(request).execute();
        JSONObject obj;
        try {
            obj = new JSONArray(resp.body().string()).getJSONObject(0);
        } catch (Exception e) {
            msg.send(channel, i18n.get(member, "core.not_found", this.argsToString(args, 0)));
            return false;
        }
        boolean nsfw = false;
        if (obj.getBoolean("is_adult_content")) {
            nsfw = true;
        }
        if (nsfw && !channel.isNSFW()) {
            msg.send(channel, i18n.get(member, "core.not_nsfw"));
            return false;
        }
        embed.setTitle(obj.getString("project_name"), "https://www.catarse.me/" + obj.getString("permalink"));
        if (!obj.isNull("headline")) {
            embed.setDescription(obj.getString("headline"));
        }
        embed.setFooter(i18n.get(member, "core.request", member.getEffectiveName() + "#" + member.getUser().getDiscriminator()),
            member.getUser().getEffectiveAvatarUrl());
        embed.setImage(obj.getString("project_img"));
        if (obj.getBoolean("open_for_contributions")) {
            embed.setColor(GFXUtils.YELLOW);
        } else {
            if (obj.getFloat("progress") > 99.99) {
                embed.setColor(GFXUtils.GREEN);
            } else {
                embed.setColor(GFXUtils.RED);
            }
        }
        if (!obj.isNull("headline")) {
            embed.setDescription(obj.getString("headline"));
        }
        embed.addField(i18n.get(member, "commands.wiki.crowdfunding.mode"), obj.getString("mode"), true);
        embed.addField(i18n.get(member, "commands.wiki.crowdfunding.category"), obj.getString("category_name"), true);
        embed.addField(i18n.get(member, "core.nsfw"), Boolean.toString(obj.getBoolean("is_adult_content")), true);
        if (!obj.isNull("pledged")) {
            embed.addField(i18n.get(member, "commands.wiki.crowdfunding.pledged"), "R$" + Math.round(obj.getFloat("pledged")), true);
        }
        if (!obj.isNull("progress")) {
            embed.addField(i18n.get(member, "commands.wiki.crowdfunding.progress"), obj.getInt("progress") + "%", true);
        }
        embed.addField(i18n.get(member, "commands.wiki.crowdfunding.creator"), obj.getString("owner_public_name"), true);
        if (!obj.getJSONObject("remaining_time").isNull("total") && obj.getBoolean("open_for_contributions")) {
            if (obj.getJSONObject("remaining_time").getInt("total") != 0) {
                embed.addField(i18n.get(member, "commands.wiki.crowdfunding.remaining"), obj.getJSONObject("").getInt("total") +
                    " " + obj.getJSONObject("remaining_time").getString("unit"), true);
            } else {
                embed.addField(i18n.get(member, "commands.wiki.crowdfunding.remaining"), i18n.get(member, "commands.wiki.crowdfunding.noDate"), true);
            }
        }
        if (!obj.getJSONObject("elapsed_time").isNull("total")) {
            embed.addField(i18n.get(member, "commands.wiki.crowdfunding.elapsed"), obj.getJSONObject("elapsed_time").getInt("total") +
                " " + obj.getJSONObject("elapsed_time").getString("unit"), true);
        }
        embed.addField(i18n.get(member, "commands.wiki.crowdfunding.city"), obj.getString("city_name"), true);
        msg.send(channel, embed.build());
        return true;
    }

    private boolean getUser(String[] args, TextChannel channel, Member member) throws IOException {
        String url = "https://api.catarse.me/user_details?public_name=ilike.";
        if (args[1].matches("[0-9]+")) {
            url = "https://api.catarse.me/user_details?id=eq.";
        }
        OkHttpClient client = new OkHttpClient();
        EmbedBuilder embed = new EmbedBuilder();
        Request request = new Request.Builder()
            .url(url + args[1])
            .get()
            .build();
        Response resp = client.newCall(request).execute();
        JSONObject obj;
        try {
            obj = new JSONArray(resp.body().string()).getJSONObject(0);
        } catch (Exception e) {
            msg.send(channel, i18n.get(member, "core.not_found", this.argsToString(args, 0)));
            return false;
        }
        embed.setTitle(obj.getString("public_name"), "https://www.catarse.me/pt/users/" + obj.getInt("id"));
        embed.setThumbnail(obj.getString("profile_img_thumbnail"));
        embed.setFooter(i18n.get(member, "commands.fun.anime.request") + " " + member.getEffectiveName() + "#" + member.getUser().getDiscriminator(),
            member.getUser().getEffectiveAvatarUrl());
        embed.addField(i18n.get(member, "commands.wiki.crowdfunding.contributed"), Integer.toString(obj.getInt("total_contributed_projects")), true);
        embed.addField(i18n.get(member, "commands.wiki.crowdfunding.published"), Integer.toString(obj.getInt("total_published_projects")), true);
        embed.addField(i18n.get(member, "commands.wiki.crowdfunding.created"), obj.getString("created_at").split("T")[0], true);
        embed.addField(i18n.get(member, "commands.wiki.crowdfunding.follows"), Integer.toString(obj.getInt("follows_count")), true);
        embed.addField(i18n.get(member, "commands.wiki.crowdfunding.followers"), Integer.toString(obj.getInt("followers_count")), true);
        msg.send(channel, embed.build());
        return true;
    }

    @Override
    public HelpArticle help(Member member) {
        HelpPage page = new HelpPage("catarse")
            .text("commands.wiki.crowdfunding.description")
            .usage("L!catarse <name>")
            .permission("commands.catarse")
            .addExample("L!catarse kingsland")
            .addExample("L!catarse user 1441251")
            .addExample("L!catarse user quobbs");
        return HelpArticle.of(page);
    }

}
