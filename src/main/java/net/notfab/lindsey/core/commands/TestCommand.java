package net.notfab.lindsey.core.commands;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.notfab.lindsey.framework.command.Bundle;
import net.notfab.lindsey.framework.command.Command;
import net.notfab.lindsey.framework.command.CommandDescriptor;
import net.notfab.lindsey.framework.settings.ProfileManager;
import net.notfab.lindsey.framework.settings.UserProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TestCommand implements Command {

    @Autowired
    private ProfileManager profiles;

    @Override
    public CommandDescriptor getInfo() {
        return new CommandDescriptor.Builder()
                .name("test")
                .permission("commands.test", "Testing command for developers")
                .help("Used for testing new features")
                .build();
    }

    @Override
    public boolean execute(Member member, TextChannel channel, String[] args, Bundle bundle) throws Exception {
        UserProfile profile = profiles.get(member);
        channel.sendMessage(profile.getOwner() + " / " + profile.getLanguage().name()).queue();
        return false;
    }

}
