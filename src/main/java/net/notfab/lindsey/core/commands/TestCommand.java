package net.notfab.lindsey.core.commands;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;
import net.notfab.lindsey.core.framework.command.Bundle;
import net.notfab.lindsey.core.framework.command.Command;
import net.notfab.lindsey.core.framework.command.CommandDescriptor;
import net.notfab.lindsey.core.framework.i18n.Messenger;
import net.notfab.lindsey.core.framework.profile.ProfileManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TestCommand implements Command {

    @Autowired
    private ProfileManager profiles;

    @Autowired
    private Messenger msg;

    @Override
    public CommandDescriptor getInfo() {
        return new CommandDescriptor.Builder()
            .name("test")
            .permission("commands.test", "Testing command for developers")
            .build();
    }

    @Override
    public boolean execute(Member member, TextChannel channel, String[] args, Message message, Bundle bundle) throws Exception {

        SubcommandData sub = new SubcommandData("sub1", "test");
        sub.addOption(OptionType.BOOLEAN, "istrue", "if this is true");

        SubcommandData sub2 = new SubcommandData("sub2", "test 2");
        sub2.addOption(OptionType.INTEGER, "number", "a number");

        SubcommandGroupData group = new SubcommandGroupData("group", "test");
        group.addSubcommands(sub);
        group.addSubcommands(sub2);

        SubcommandData sub3 = new SubcommandData("sub3", "test 3");
        sub3.addOption(OptionType.INTEGER, "number", "a number");
        sub3.addOption(OptionType.BOOLEAN, "isbool", "if true");

        CommandData data = new CommandData("beta", "test command with subcommands")
            .addSubcommandGroups(group);

        member.getGuild().upsertCommand(data)
            .queue();

        return false;
    }

}
