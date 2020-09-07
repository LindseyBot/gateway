package net.notfab.lindsey.core.commands;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.notfab.lindsey.framework.command.Bundle;
import net.notfab.lindsey.framework.command.Command;
import net.notfab.lindsey.framework.command.CommandDescriptor;
import net.notfab.lindsey.framework.i18n.Messenger;
import net.notfab.lindsey.framework.options.OptionManager;
import net.notfab.lindsey.framework.settings.ProfileManager;
import net.notfab.lindsey.framework.settings.UserProfile;
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
        UserProfile profile = profiles.get(member);
        channel.sendMessage(profile.getOwner() + " / " + profile.getLanguage().name()).queue();
        try {
            options.find("example.option").set(member.getGuild(), true);
        } catch (IllegalArgumentException ex) {
            msg.send(channel, sender(member) + ex.getMessage());
            return false;
        }
        boolean enabled = options.find("example.option").get(member.getGuild());
        msg.send(channel, sender(member) + " example.option is " + enabled);
        return false;
    }

}
