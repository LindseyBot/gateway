package net.notfab.lindsey.core.commands.fun;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Neko implements Command {

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
            .name("neko")
            .module(Modules.FUN)
            .permission("commands.neko", "permissions.command")
            .build();
    }

    @Override
    public boolean execute(Member member, TextChannel channel, String[] args, Message message, Bundle bundle) throws Exception {

        boolean nsfw = true;
        String url = "";
        if (args.length != 0) {
            switch (args[0]) {
                case "nsfw":
                    url = "https://nekos.club/api/nsfw-nekos/";
                    break;
                case "nekopara":
                    url = "https://nekos.club/api/nsfw-nekopara/";
                    break;
                case "maid":
                    url = "https://nekos.club/api/nsfw-maids/";
                    break;
                default:
                    url = "https://nekos.club/api/sfw-nekos/";
                    nsfw = false;
                    break;
            }
        } else {
            nsfw = false;
            url = "https://nekos.club/api/sfw-nekos/";
        }

        if (nsfw && !channel.isNSFW()) {
            msg.send(channel, i18n.get(member, "core.not_nsfw"));
            return false;
        }
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        Response resp = client.newCall(request).execute();
        JSONArray arr = new JSONArray(resp.body().string());
        msg.sendImage(channel, arr.getJSONObject(0).getString("Image"));

        return true;
    }

    @Override
    public HelpArticle help(Member member) {
        HelpPage page = new HelpPage("neko")
            .text("commands.fun.neko.description")
            .usage("L!neko [subcommand]")
            .permission("commands.neko")
            .addExample("L!neko")
            .addExample("L!neko nsfw")
            .addExample("L!neko nekopara")
            .addExample("L!neko maid");
        return HelpArticle.of(page);
    }

}
