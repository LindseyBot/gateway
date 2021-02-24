package net.notfab.lindsey.core.commands.music;

import net.dv8tion.jda.api.entities.*;
import net.notfab.lindsey.core.framework.command.*;
import net.notfab.lindsey.core.framework.command.help.HelpArticle;
import net.notfab.lindsey.core.framework.command.help.HelpPage;
import net.notfab.lindsey.core.framework.i18n.Messenger;
import net.notfab.lindsey.core.framework.i18n.Translator;
import net.notfab.lindsey.core.service.AudioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Connect implements Command {

    @Autowired
    private Messenger msg;

    @Autowired
    private Translator i18n;

    @Autowired
    private AudioService audio;

    @Override
    public CommandDescriptor getInfo() {
        return new CommandDescriptor.Builder()
            .name("connect")
            .alias("join")
            .permission("commands.connect", "permissions.command")
            .module(Modules.MUSIC)
            .build();
    }

    @Override
    public boolean execute(Member member, TextChannel channel, String[] args, Message message, Bundle bundle) throws Exception {
        VoiceChannel target;
        if (args.length == 0) {
            GuildVoiceState state = member.getVoiceState();
            if (state == null || !state.inVoiceChannel()) {
                // Not in voice channel
                msg.send(channel, sender(member) + i18n.get(member, "commands.music.connect.not_in_vc"));
                return false;
            }
            target = state.getChannel();
        } else {
            target = FinderUtil.findVoiceChannel(argsToString(args, 0), member.getGuild());
        }
        if (target == null) {
            msg.send(channel, sender(member) + i18n.get(member, "search.voice", argsToString(args, 0)));
            return true;
        }
        if (!audio.connect(member.getGuild(), target)) {
            // Failed to connect, probably permission error
            msg.send(channel, sender(member) + i18n.get(member, "commands.music.connect.failed"));
        } else {
            // Connected to voice
            msg.send(channel, sender(member) + i18n.get(member, "commands.music.connect.connected", target.getName()));
        }
        return true;
    }

    @Override
    public HelpArticle help(Member member) {
        HelpPage page = new HelpPage("connect")
            .text("commands.music.connect.description")
            .usage("L!connect <voiceChannel>")
            .permission("commands.connect")
            .addExample("L!connect music-01");
        return HelpArticle.of(page);
    }

}
