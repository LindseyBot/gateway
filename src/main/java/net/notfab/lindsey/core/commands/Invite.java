package net.notfab.lindsey.core.commands;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Invite implements Command {

    @Autowired
    private Translator i18n;

    @Autowired
    private Messenger msg;

    @Override
    public CommandDescriptor getInfo() {
        return new CommandDescriptor.Builder()
            .name("invite")
            .module(Modules.CORE)
            .permission("commands.invite", "permissions.command")
            .build();
    }

    @Override
    public boolean execute(Member member, TextChannel channel, String[] args, Message message, Bundle bundle) throws Exception {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle(i18n.get(member, "commands.core.invite.title"));
        embed.setThumbnail("https://gblobscdn.gitbook.com/spaces%2F-LSekC_Gg_7hjGlZBZoU%2Favatar.png");
        embed.setFooter(i18n.get(member, "core.request", member.getEffectiveName() + "#" + member.getUser().getDiscriminator()),
            member.getUser().getEffectiveAvatarUrl());
        embed.setDescription("[" + i18n.get(member, "commands.core.invite.all") + "](https://goo.gl/PrPGky) \n [" + i18n.get(member, "commands.core.invite.music") +
            "](https://goo.gl/P7WfAh) \n [" + i18n.get(member, "commands.core.invite.mod") + "](https://goo.gl/Eo8Pze) \n [" +
            i18n.get(member, "commands.core.invite.minimal") + "](https://goo.gl/SN25iK) \n \n  [" + i18n.get(member, "commands.core.invite.site") + "](https://docs.notfab.net)");
        msg.send(channel, embed.build());
        return true;
    }

    @Override
    public HelpArticle help(Member member) {
        HelpPage page = new HelpPage("invite")
            .text("commands.core.invite.description")
            .usage("L!invite")
            .permission("commands.invite");
        return HelpArticle.of(page);
    }

}
