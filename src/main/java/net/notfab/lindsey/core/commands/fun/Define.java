package net.notfab.lindsey.core.commands.fun;

import lombok.extern.slf4j.Slf4j;
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
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class Define implements Command {

    @Autowired
    private Translator i18n;

    @Autowired
    private Messenger msg;

    @Override
    public CommandDescriptor getInfo() {
        return new CommandDescriptor.Builder()
            .name("define")
            .module(Modules.FUN)
            .permission("commands.define", "permissions.command")
            .build();
    }

    @Override
    public boolean execute(Member member, TextChannel channel, String[] args, Message message, Bundle bundle) throws Exception {
        if (args.length == 0) {
            HelpArticle article = this.help(member);
            article.send(channel, member, args, msg, i18n);
            return false;
        }
        String term = this.argsToString(args, 0);
        String definition = define(term);
        if (definition == null) {
            msg.send(channel, sender(member) + i18n.get(member, "commands.fun.define.no_definitions", term));
        } else {
            msg.send(channel, sender(member) + "_" + definition + "_");
        }
        return false;
    }

    @Override
    public HelpArticle help(Member member) {
        HelpPage page = new HelpPage("define")
            .text("commands.fun.define.description")
            .usage("L!define <term>")
            .permission("commands.define")
            .addExample("L!define life");
        return HelpArticle.of(page);
    }

    private String define(String word) {
        StringBuilder builder = new StringBuilder("https://www.urbandictionary.com/define.php?term=");
        for (String s : word.split(" ")) {
            builder.append(URLEncoder.encode(s, StandardCharsets.UTF_8)).append("+");
        }
        builder.setLength(builder.length() - 1);
        try {
            Document d = Jsoup.connect(builder.toString()).followRedirects(true).get();
            Elements definitions = d.getElementsByClass("def-panel");
            if (definitions.size() > 0) {
                String definition = definitions.get(0).getElementsByClass("meaning").get(0).text();
                if (definition.length() > 300) {
                    definition = definition.substring(0, 300) + "...";
                }
                if (!definition.endsWith(".")) {
                    definition += ".";
                }
                return definition;
            }
        } catch (HttpStatusException ex) {
            if (ex.getStatusCode() == 404) {
                return null;
            }
            log.error("Error while defining term - HTTP " + ex.getStatusCode(), ex);
        } catch (Exception ex) {
            log.error("Error while defining term", ex);
        }
        return null;
    }

}
