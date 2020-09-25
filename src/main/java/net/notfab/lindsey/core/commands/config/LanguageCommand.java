package net.notfab.lindsey.core.commands.config;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.notfab.lindsey.framework.command.*;
import net.notfab.lindsey.framework.command.help.HelpArticle;
import net.notfab.lindsey.framework.command.help.HelpPage;
import net.notfab.lindsey.framework.i18n.Language;
import net.notfab.lindsey.framework.i18n.Messenger;
import net.notfab.lindsey.framework.i18n.Translator;
import net.notfab.lindsey.framework.profile.ProfileManager;
import net.notfab.lindsey.framework.profile.UserProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LanguageCommand implements Command {

    @Autowired
    private Translator i18n;

    @Autowired
    private Messenger msg;

    @Autowired
    private ProfileManager profiles;

    @Override
    public CommandDescriptor getInfo() {
        return new CommandDescriptor.Builder()
            .name("language")
            .alias("lang")
            .module(Modules.CORE)
            .permission("commands.language", "permissions.command")
            .build();
    }

    @Override
    public boolean execute(Member member, TextChannel channel, String[] args, Message message, Bundle bundle) throws Exception {
        if (args.length == 0) {
            HelpArticle article = this.help(member);
            article.send(channel, member, args, msg, i18n);
        } else if (args.length == 1) {
            Member target = FinderUtil.findMember(args[0], message);
            if (target == null) {
                try {
                    Language language = Language.valueOf(args[0]);
                    UserProfile profile = profiles.get(member);
                    profile.setLanguage(language);
                    profiles.save(profile);
                    msg.send(channel, sender(member) + i18n.get(member, "commands.core.language.updated", profile.getLanguage().name()));
                } catch (IllegalArgumentException ex) {
                    msg.send(channel, sender(member) + i18n.get(member, "commands.core.language.unknown"));
                    return false;
                }
            } else {
                UserProfile profile = profiles.get(target);
                msg.send(channel, sender(member) + i18n.get(member, "commands.core.language.target", profile.getLanguage().name()));
            }
        } else {
            HelpArticle article = this.help(member);
            article.send(channel, member, args, msg, i18n);
            return false;
        }
        return true;
    }

    @Override
    public HelpArticle help(Member member) {
        HelpPage page = new HelpPage("language")
            .text("commands.core.language.description")
            .usage("L!language <@user/name>")
            .permission("commands.language")
            .addExample("L!language en_US")
            .addExample("L!language @NotFab");
        return HelpArticle.of(page);
    }

}
