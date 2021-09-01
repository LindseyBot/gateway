package net.notfab.lindsey.core.commands.music;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.notfab.lindsey.core.framework.command.Command;
import net.notfab.lindsey.core.framework.command.Modules;
import net.notfab.lindsey.core.framework.command.help.HelpArticle;
import net.notfab.lindsey.core.framework.command.help.HelpPage;
import net.notfab.lindsey.core.framework.i18n.Messenger;
import net.notfab.lindsey.core.framework.i18n.Translator;
import net.notfab.lindsey.core.service.AudioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Disconnect implements Command {

    @Autowired
    private Messenger msg;

    @Autowired
    private Translator i18n;

    @Autowired
    private AudioService audio;

    @Override
    public CommandDescriptor getInfo() {
        return new CommandDescriptor.Builder()
            .name("disconnect")
            .alias("leave", "stop")
            .permission("commands.disconnect", "permissions.command")
            .module(Modules.MUSIC)
            .build();
    }

    @Override
    public boolean execute(Member member, TextChannel channel, String[] args, Message message, Bundle bundle) throws Exception {
        Guild guild = member.getGuild();
        if (!guild.getAudioManager().isConnected()) {
            // Not connected
            msg.send(channel, sender(member) + i18n.get(member, "commands.music.disconnect.failed"));
            return false;
        }
        audio.destroy(guild);
        msg.send(channel, sender(member) + i18n.get(member, "commands.music.disconnect.disconnected", member.getUser().getAsTag()));
        return true;
    }

    @Override
    public HelpArticle help(Member member) {
        HelpPage page = new HelpPage("disconnect")
            .text("commands.music.disconnect.description")
            .usage("L!disconnect")
            .permission("commands.disconnect")
            .addExample("L!disconnect");
        return HelpArticle.of(page);
    }

}
