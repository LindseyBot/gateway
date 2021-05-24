package net.notfab.lindsey.core.commands;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.notfab.lindsey.core.Lindsey;
import net.notfab.lindsey.core.framework.actions.ScriptError;
import net.notfab.lindsey.core.framework.actions.ScriptingService;
import net.notfab.lindsey.core.framework.command.Bundle;
import net.notfab.lindsey.core.framework.command.Command;
import net.notfab.lindsey.core.framework.command.CommandDescriptor;
import net.notfab.lindsey.core.framework.command.Modules;
import net.notfab.lindsey.core.framework.command.help.HelpArticle;
import net.notfab.lindsey.core.framework.command.help.HelpPage;
import net.notfab.lindsey.core.framework.i18n.Messenger;
import net.notfab.lindsey.core.framework.i18n.Translator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class DevCommand implements Command {

    @Autowired
    private Translator i18n;

    @Autowired
    private Messenger msg;

    @Autowired
    private ScriptingService scripts;

    @Autowired
    private Lindsey lindsey;

    @Override
    public CommandDescriptor getInfo() {
        return new CommandDescriptor.Builder()
            .name("dev")
            .module(Modules.CORE)
            .permission("commands.dev", "permissions.command")
            .build();
    }

    @Override
    public boolean execute(Member member, TextChannel channel, String[] args, Message message, Bundle bundle) throws Exception {
        if (member.getIdLong() != 87166524837613568L && member.getIdLong() != 119566649731842049L) {
            return false;
        }
        if (args.length == 0) {
            msg.send(channel, sender(member) + "Invalid Command, Available: `reload`, `eval`");
            return false;
        }
        if (args[0].equalsIgnoreCase("reload")) {
            Message reply = message.reply("Reloading languages...")
                .mentionRepliedUser(false)
                .complete();
            int total = this.i18n.reloadLanguages();
            reply.editMessage("Loaded " + total + " language files")
                .queue();
        } else if (args[0].equalsIgnoreCase("eval")) {
            Map<String, Object> params = new HashMap<>();
            params.put("channel", channel);
            params.put("member", member);
            params.put("author", message.getAuthor());
            params.put("message", message);
            params.put("guild", member.getGuild());
            params.put("lindsey", this.lindsey);

            try {
                int index = message.getContentDisplay()
                    .indexOf(" eval ") + " eval ".length();
                String script = message.getContentDisplay()
                    .substring(index);
                String result = String.valueOf(this.scripts.unsafe(TimeUnit.SECONDS.toMillis(5), script, params));
                if (result.length() > 1500) {
                    message.reply(result.getBytes(StandardCharsets.UTF_8), "result.txt")
                        .mentionRepliedUser(false)
                        .queue();
                } else {
                    message.reply(result)
                        .mentionRepliedUser(false)
                        .queue();
                }
            } catch (ScriptError error) {
                switch (error.getType()) {
                    case EXCEPTION -> message.reply("Exception: " + error.getCause().getMessage())
                        .mentionRepliedUser(false)
                        .queue();
                    case TIMEOUT -> message.reply("Timed out.")
                        .mentionRepliedUser(false)
                        .queue();
                    default -> message.reply("Execution was interrupted.")
                        .mentionRepliedUser(false)
                        .queue();
                }
            }
        } else {
            msg.send(channel, sender(member) + "Invalid Command, Available: `reload`, `eval`");
            return false;
        }
        return true;
    }

    @Override
    public HelpArticle help(Member member) {
        HelpPage page = new HelpPage("dev")
            .text("commands.core.dev.description")
            .usage("L!dev")
            .permission("commands.dev");
        return HelpArticle.of(page);
    }

}
