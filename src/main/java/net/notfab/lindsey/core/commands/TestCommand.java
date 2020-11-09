package net.notfab.lindsey.core.commands;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.notfab.lindsey.core.framework.command.Bundle;
import net.notfab.lindsey.core.framework.command.Command;
import net.notfab.lindsey.core.framework.command.CommandDescriptor;
import net.notfab.lindsey.core.framework.i18n.Messenger;
import net.notfab.lindsey.core.framework.options.OptionManager;
import net.notfab.lindsey.core.framework.profile.GuildProfile;
import net.notfab.lindsey.core.framework.profile.ProfileManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TestCommand implements Command {

    @Autowired
    private ProfileManager profiles;

    @Autowired
    private OptionManager options;

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
        GuildProfile profile = profiles.get(message.getGuild());
        System.out.println("Prefix: " + profile.getPrefix());
        profile.setPrefix("!");
        profiles.save(profile);
        return false;
    }

}
