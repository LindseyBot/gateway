package net.notfab.lindsey.core.commands.music;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.notfab.lindsey.core.framework.Utils;
import net.notfab.lindsey.core.framework.command.Bundle;
import net.notfab.lindsey.core.framework.command.Command;
import net.notfab.lindsey.core.framework.command.CommandDescriptor;
import net.notfab.lindsey.core.framework.command.Modules;
import net.notfab.lindsey.core.framework.command.help.HelpArticle;
import net.notfab.lindsey.core.framework.command.help.HelpPage;
import net.notfab.lindsey.core.framework.i18n.Messenger;
import net.notfab.lindsey.core.framework.i18n.Translator;
import net.notfab.lindsey.core.framework.models.PlayList;
import net.notfab.lindsey.core.service.PlayListService;
import net.notfab.lindsey.core.service.SongService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class Remove implements Command {

    @Autowired
    private Messenger msg;

    @Autowired
    private Translator i18n;

    @Autowired
    private SongService songs;

    @Autowired
    private PlayListService playlists;

    @Override
    public CommandDescriptor getInfo() {
        return new CommandDescriptor.Builder()
            .name("remove")
            .permission("commands.remove", "permissions.command")
            .module(Modules.MUSIC)
            .build();
    }

    @Override
    public boolean execute(Member member, TextChannel channel, String[] args, Message message, Bundle bundle) throws Exception {
        if (args.length == 0) {
            HelpArticle article = this.help(member);
            article.send(channel, member, args, msg, i18n);
            return false;
        }
        String nameOrURL = this.argsToString(args, 0);

        if (Utils.isURL(nameOrURL)) {
            if (!Utils.isSupportedMusicURL(nameOrURL)) {
                // Not supported
                msg.send(channel, sender(member) + i18n.get(member, "commands.music.add.not_supported"));
                return false;
            }
            nameOrURL = songs.normalize(nameOrURL);
        }

        Optional<PlayList> oPlayList = playlists.findActive(member.getGuild());
        if (oPlayList.isEmpty()) {
            // No active playlist
            msg.send(channel, sender(member) + i18n.get(member, "commands.playlist.no_active"));
            return false;
        }
        PlayList playList = oPlayList.get();
        if (!playlists.hasPermission(playList, member.getUser())) {
            // No permission to modify this PlayList
            msg.send(channel, sender(member) + i18n.get(member, "commands.playlist.locked"));
            return false;
        }

        long total_removed = playlists.remove(playList, nameOrURL);
        msg.send(channel, sender(member) + i18n.get(member, "commands.music.remove.removed", total_removed));
        return true;
    }

    @Override
    public HelpArticle help(Member member) {
        HelpPage page = new HelpPage("remove")
            .text("commands.music.remove.description")
            .usage("L!remove <name or URL>")
            .permission("commands.remove")
            .addExample("L!remove MonsterCat")
            .addExample("L!remove https://www.youtube.com/watch?v=ddFAIkUb7A0");
        return HelpArticle.of(page);
    }

}
