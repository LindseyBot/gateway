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
import net.notfab.lindsey.core.service.TrackService;
import net.notfab.lindsey.shared.entities.music.Track;
import net.notfab.lindsey.shared.entities.playlist.PlayList;
import net.notfab.lindsey.shared.entities.profile.server.MusicSettings;
import net.notfab.lindsey.shared.repositories.sql.PlayListRepository;
import net.notfab.lindsey.shared.repositories.sql.server.MusicSettingsRepository;
import net.notfab.lindsey.shared.services.PlayListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class RemoveCmd implements Command {

    @Autowired
    private Messenger msg;

    @Autowired
    private Translator i18n;

    @Autowired
    private TrackService songs;

    @Autowired
    private PlayListService playlists;

    @Autowired
    private MusicSettingsRepository musicSettings;

    @Autowired
    private PlayListRepository repository;

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
            nameOrURL = songs.extract(nameOrURL);
        }

        MusicSettings settings = this.musicSettings.findById(member.getGuild().getIdLong())
            .orElse(new MusicSettings(member.getGuild().getIdLong()));
        if (settings.getActivePlayList() == null) {
            // No active playlist
            msg.send(channel, sender(member) + i18n.get(member, "commands.playlist.no_active"));
            return false;
        }

        Optional<PlayList> oPlayList = this.repository.findById(settings.getActivePlayList());
        if (oPlayList.isEmpty()) {
            // Deleted playlist
            settings.setActivePlayList(null);
            this.musicSettings.save(settings);
            msg.send(channel, sender(member) + i18n.get(member, "commands.playlist.no_active"));
            return false;
        }

        PlayList playList = oPlayList.get();
        if (!this.playlists.canModify(playList, member.getUser().getIdLong())) {
            // No permission to modify playlist
            msg.send(channel, sender(member) + i18n.get(member, "commands.playlist.locked"));
            // Private playlists are only usable in the owner's guild
            if (playList.getOwner() != member.getGuild().getOwnerIdLong()) {
                settings.setActivePlayList(null);
                this.musicSettings.save(settings);
            }
            return true;
        }

        Optional<Track> oTrack = playlists.findByName(playList.getId(), nameOrURL);
        long total_removed;
        if (oTrack.isEmpty()) {
            total_removed = 0;
        } else {
            total_removed = playlists.remove(playList.getId(), oTrack.get());
        }
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
