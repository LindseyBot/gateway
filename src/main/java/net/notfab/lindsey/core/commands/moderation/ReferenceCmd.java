package net.notfab.lindsey.core.commands.moderation;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.notfab.lindsey.core.framework.command.*;
import net.notfab.lindsey.core.framework.command.help.HelpArticle;
import net.notfab.lindsey.core.framework.command.help.HelpPage;
import net.notfab.lindsey.core.framework.i18n.Messenger;
import net.notfab.lindsey.core.framework.i18n.Translator;
import net.notfab.lindsey.shared.rpc.FMember;
import net.notfab.lindsey.shared.services.ReferencingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ReferenceCmd implements Command {

    @Autowired
    private Messenger msg;

    @Autowired
    private Translator i18n;

    @Autowired
    private ReferencingService service;

    @Override
    public CommandDescriptor getInfo() {
        return new CommandDescriptor.Builder()
            .name("reference")
            .permission("commands.reference", "permissions.command", false)
            .module(Modules.MODERATION)
            .build();
    }

    @Override
    public boolean execute(Member member, TextChannel channel, String[] args, Message message, Bundle bundle) throws Exception {
        if (args.length == 0) {
            HelpArticle article = this.help(member);
            article.send(channel, member, args, msg, i18n);
        } else {
            Member target = FinderUtil.findMember(argsToString(args, 0), message);
            if (target == null || target.getUser().isBot()) {
                msg.send(channel, sender(member) + i18n.get(member, "core.member_nf"));
                return false;
            }
            FMember reference = new FMember();
            reference.setId(target.getIdLong());
            reference.setName(target.getUser().getName());
            reference.setDiscrim(target.getUser().getDiscriminator());
            reference.setAvatarUrl(target.getUser().getEffectiveAvatarUrl());
            reference.setGuildId(target.getGuild().getIdLong());
            reference.setGuildName(target.getGuild().getName());
            String ticket = this.service.create(reference);
            msg.send(channel, sender(member) + i18n.get(member, "commands.mod.reference.created", ticket));
        }
        return true;
    }

    @Override
    public HelpArticle help(Member member) {
        HelpPage page = new HelpPage("reference")
            .text("commands.mod.reference.description")
            .usage("L!reference <@user>")
            .permission("commands.reference")
            .addExample("L!reference @Lindsey");
        return HelpArticle.of(page);
    }

}
