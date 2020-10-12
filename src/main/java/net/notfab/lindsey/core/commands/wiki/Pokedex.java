package net.notfab.lindsey.core.commands.wiki;

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
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Pokedex implements Command {

    @Autowired
    private Translator i18n;

    @Autowired
    private Messenger msg;

    @Override
    public CommandDescriptor getInfo() {
        return new CommandDescriptor.Builder()
            .name("pokedex")
            .alias("pokemon")
            .module(Modules.GAME_WIKI)
            .permission("commands.pokedex", "permissions.command")
            .build();
    }

    @Override
    public boolean execute(Member member, TextChannel channel, String[] args, Message message, Bundle bundle) throws Exception {
        if (args.length == 0) {
            HelpArticle article = this.help(member);
            article.send(channel, member, args, msg, i18n);
            return true;
        }

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
            .url("https://pokeapi.co/api/v2/pokemon/" + args[0])
            .get()
            .build();
        Response resp = client.newCall(request).execute();
        assert resp.body() != null;
        String str = resp.body().string();

        if (str.equals("Not Found")) {
            msg.send(channel, i18n.get(member, "commands.wiki.pokedex.404"));
            return true;
        }

        JSONObject obj = new JSONObject(str);
        JSONArray types = obj.getJSONArray("types");
        String name = obj.getString("name");

        EmbedBuilder embed = new EmbedBuilder()
            .setTitle(name.substring(0, 1).toUpperCase() + name.substring(1))
            .addField(i18n.get(member, "commands.wiki.pokedex.id"), Integer.toString(obj.getInt("id")), true)
            .addField(i18n.get(member, "commands.wiki.pokedex.height"), Double.toString((double) obj.getInt("height") / 10), true)
            .addField(i18n.get(member, "commands.wiki.pokedex.weight"), Double.toString((double) obj.getInt("weight") / 10), true)
            .setThumbnail(obj.getJSONObject("sprites").getString("front_default"))
            .setFooter(i18n.get(member, "commands.nsfw.request") + " " + member.getEffectiveName() + "#" + member.getUser().getDiscriminator(),
                member.getUser().getEffectiveAvatarUrl());
        if (types.length() == 1) {
            embed.addField(i18n.get(member, "commands.wiki.pokedex.type"), types.getJSONObject(0).getJSONObject("type").getString("name"), true);
        } else {
            embed.addField(i18n.get(member, "commands.wiki.pokedex.type"), types.getJSONObject(0).getJSONObject("type").getString("name") + " & " +
                types.getJSONObject(1).getJSONObject("type").getString("name"), true);
        }
        msg.send(channel, embed.build());
        return true;
    }

    @Override
    public HelpArticle help(Member member) {
        HelpPage page = new HelpPage("pokedex")
            .text("commands.wiki.pokedex.description")
            .usage("L!pokedex <pokemon>")
            .permission("commands.pokedex")
            .addExample("L!pokedex bulbasaur")
            .addExample("L!pokemon charmander");
        return HelpArticle.of(page);
    }

}
