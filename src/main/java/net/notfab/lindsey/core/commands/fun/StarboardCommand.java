package net.notfab.lindsey.core.commands.fun;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.notfab.lindsey.core.framework.command.*;
import net.notfab.lindsey.core.framework.command.help.HelpArticle;
import net.notfab.lindsey.core.framework.command.help.HelpPage;
import net.notfab.lindsey.core.framework.i18n.Messenger;
import net.notfab.lindsey.core.framework.i18n.Translator;
import net.notfab.lindsey.shared.entities.profile.server.StarboardSettings;
import net.notfab.lindsey.shared.repositories.sql.server.StarboardSettingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class StarboardCommand implements Command {

    @Autowired
    private Messenger msg;

    @Autowired
    private Translator i18n;

    @Autowired
    private StarboardSettingsRepository repository;

    @Override
    public CommandDescriptor getInfo() {
        return new CommandDescriptor.Builder()
            .name("starboard")
            .permission("commands.starboard", "permissions.command", false)
            .module(Modules.FUN)
            .build();
    }

    @Override
    public boolean execute(Member member, TextChannel channel, String[] args, Message message, Bundle bundle) throws Exception {
        if (args.length == 0) {
            HelpArticle article = this.help(member);
            article.send(channel, member, args, msg, i18n);
        } else {
            Optional<StarboardSettings> oSettings = repository.findById(member.getGuild().getIdLong());
            if (args[0].equalsIgnoreCase("OFF")) {
                if (oSettings.isPresent()) {
                    repository.deleteById(member.getGuild().getIdLong());
                }
                msg.send(channel, sender(member) + i18n.get(member, "commands.fun.starboard.disabled"));
            } else {
                TextChannel target = FinderUtil.findTextChannel(argsToString(args, 0), channel.getGuild());
                if (target == null) {
                    msg.send(channel, sender(member) + i18n.get(member, "search.channel", argsToString(args, 0)));
                    return false;
                }
                StarboardSettings settings = oSettings
                    .orElse(new StarboardSettings(member.getGuild().getIdLong()));
                settings.setChannel(target.getIdLong());
                this.repository.save(settings);
                msg.send(channel, sender(member) + i18n.get(member, "commands.fun.starboard.enabled", target.getAsMention()));
            }
        }
        return true;
    }

    @Override
    public HelpArticle help(Member member) {
        HelpPage page = new HelpPage("starboard")
            .text("commands.fun.starboard.description")
            .usage("L!starboard <channel or OFF>")
            .url("https://github.com/LindseyBot/core/wiki/commands-starboard")
            .permission("commands.starboard")
            .addExample("L!starboard #starboard")
            .addExample("L!starboard off");
        return HelpArticle.of(page);
    }

}
