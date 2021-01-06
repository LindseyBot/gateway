package net.notfab.lindsey.core.commands.music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.GuildVoiceState;
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
import net.notfab.lindsey.core.framework.models.PlayListSecurity;
import net.notfab.lindsey.core.framework.models.Song;
import net.notfab.lindsey.core.service.AudioService;
import net.notfab.lindsey.core.service.PlayListService;
import net.notfab.lindsey.core.service.SongService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class Play implements Command {

    @Autowired
    private Messenger msg;

    @Autowired
    private Translator i18n;

    @Autowired
    private AudioService audio;

    @Autowired
    private SongService songs;

    @Autowired
    private PlayListService playlists;

    @Override
    public CommandDescriptor getInfo() {
        return new CommandDescriptor.Builder()
            .name("play")
            .alias("start")
            .permission("commands.play", "permissions.command")
            .module(Modules.MUSIC)
            .build();
    }

    @Override
    public boolean execute(Member member, TextChannel channel, String[] args, Message message, Bundle bundle) throws Exception {
        if (!audio.isConnected(member.getGuild())) {
            GuildVoiceState state = member.getVoiceState();
            if (state == null || !state.inVoiceChannel()) {
                // Not in voice channel
                msg.send(channel, sender(member) + i18n.get(member, "commands.music.connect.not_in_vc"));
                return false;
            }
            if (!audio.connect(member.getGuild(), state.getChannel())) {
                // Failed to connect, probably permission error
                msg.send(channel, sender(member) + i18n.get(member, "commands.music.connect.failed"));
                return false;
            }
        }

        Optional<PlayList> oPlayList = playlists.findActive(member.getGuild());
        if (oPlayList.isEmpty()) {
            // No active playlist
            msg.send(channel, sender(member) + i18n.get(member, "commands.playlist.no_active"));
            return false;
        }
        PlayList playList = oPlayList.get();
        if (playList.getSecurity() == PlayListSecurity.PRIVATE
            && !playlists.hasPermission(playList, member.getUser())) {
            // No permission to use this playlist
            msg.send(channel, sender(member) + i18n.get(member, "commands.playlist.locked"));
            // Private playlists are only usable in the owner's guild
            if (!String.valueOf(playList.getOwner()).equals(member.getGuild().getOwnerId())) {
                playlists.setActive(member.getGuild(), null);
            }
            return true;
        }

        Song song;
        if (args.length == 0) {
            song = playlists.findNextSong(playList, member.getGuild().getIdLong());
            if (song == null) {
                // Failed to start playing (No songs found)
                msg.send(channel, sender(member) + i18n.get(member, "commands.music.play.failed_songs"));
                return true;
            }
        } else {
            String nameOrURL = this.argsToString(args, 0);
            if (Utils.isURL(nameOrURL)) {
                if (!Utils.isSupportedMusicURL(nameOrURL)) {
                    // Not supported
                    msg.send(channel, sender(member) + i18n.get(member, "commands.music.add.not_supported"));
                    return false;
                }
                nameOrURL = songs.normalize(nameOrURL);
            }
            song = playlists.findSong(playList, nameOrURL);
            if (song == null) {
                // Song not found
                msg.send(channel, sender(member) + i18n.get(member, "commands.music.play.not_found", nameOrURL));
                return true;
            }
        }
        if (!playlists.updateCursor(playList, song, member.getGuild().getIdLong())) {
            // Failed to start playing (No songs found)
            msg.send(channel, sender(member) + i18n.get(member, "commands.music.play.failed_internal"));
            return true;
        }
        AudioTrack track = songs.toAudioTrack(song);
        if (!audio.play(member.getGuild(), track)) {
            // Failed to start playing (No voice connection)
            msg.send(channel, sender(member) + i18n.get(member, "commands.music.play.failed_voice"));
        }
        return true;
    }

    @Override
    public HelpArticle help(Member member) {
        HelpPage page = new HelpPage("play")
            .text("commands.music.play.description")
            .usage("L!play <song>")
            .permission("commands.play")
            .addExample("L!play https://www.youtube.com/watch?v=ddFAIkUb7A0");
        return HelpArticle.of(page);
    }

}
