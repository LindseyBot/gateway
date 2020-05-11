package net.notfab.lindsey.core.commands;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.notfab.lindsey.framework.command.Bundle;
import net.notfab.lindsey.framework.command.Command;
import net.notfab.lindsey.framework.command.CommandDescriptor;
import net.notfab.lindsey.framework.settings.UserSettings;
import net.notfab.lindsey.framework.settings.repositories.UserSettingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class TestCommand implements Command {

    @Autowired
    private UserSettingsRepository repository;

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
        Optional<UserSettings> profileOptional = repository.findById(member.getIdLong());
        UserSettings settings;
        if (profileOptional.isPresent()) {
            settings = profileOptional.get();
            channel.sendMessage("Test: " + settings.isTest()).queue();
            settings.setTest(!settings.isTest());
        } else {
            settings = new UserSettings();
            settings.setOwner(member.getIdLong());
            settings.setTest(true);
        }

        repository.save(settings);
        return false;
    }

}
