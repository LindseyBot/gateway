package net.notfab.lindsey.core.commands;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.notfab.lindsey.core.framework.command.Bundle;
import net.notfab.lindsey.core.framework.command.Command;
import net.notfab.lindsey.core.framework.command.CommandDescriptor;
import net.notfab.lindsey.core.framework.i18n.Messenger;
import net.notfab.lindsey.core.framework.models.PlayList;
import net.notfab.lindsey.core.framework.options.OptionManager;
import net.notfab.lindsey.core.framework.profile.ProfileManager;
import net.notfab.lindsey.core.service.AudioService;
import net.notfab.lindsey.core.service.PlayListService;
import net.notfab.lindsey.core.service.SongService;
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

    @Autowired
    private AudioService audio;

    @Autowired
    private SongService songs;

    @Autowired
    private PlayListService playListService;

    @Override
    public CommandDescriptor getInfo() {
        return new CommandDescriptor.Builder()
            .name("test")
            .permission("commands.test", "Testing command for developers")
            .build();
    }

    @Override
    public boolean execute(Member member, TextChannel channel, String[] args, Message message, Bundle bundle) throws Exception {
        PlayList playList = playListService.findByName(member.getUser(), "test");
        if (playList == null) {
            playList = playListService.create(member.getUser(), "test");
        }
        playListService.setActive(member.getGuild(), playList.getId());
        return false;
    }

}
