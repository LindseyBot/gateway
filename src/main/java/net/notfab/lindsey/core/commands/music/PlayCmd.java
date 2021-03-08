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
import net.notfab.lindsey.core.service.AudioService;
import net.notfab.lindsey.core.service.TrackService;
import net.notfab.lindsey.shared.entities.music.Track;
import net.notfab.lindsey.shared.entities.playlist.PlayList;
import net.notfab.lindsey.shared.entities.profile.server.MusicSettings;
import net.notfab.lindsey.shared.enums.PlayListSecurity;
import net.notfab.lindsey.shared.repositories.sql.PlayListRepository;
import net.notfab.lindsey.shared.repositories.sql.server.MusicSettingsRepository;
import net.notfab.lindsey.shared.services.PlayListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class PlayCmd implements Command {

    @Autowired
    private Messenger msg;

    @Autowired
    private Translator i18n;

    @Autowired
    private AudioService audio;

    @Autowired
    private TrackService tracks;

    @Autowired
    private PlayListService playlists;

    @Autowired
    private MusicSettingsRepository musicSettings;

    @Autowired
    private PlayListRepository repository;

    @Override
    public CommandDescriptor getInfo() {
        return new CommandDescriptor.Builder()
            .name("play")
            .alias("start", "skip")
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
        if (playList.getSecurity() == PlayListSecurity.PRIVATE
            && !this.playlists.canRead(playList, member.getUser().getIdLong())) {
            // No permission to use this playlist
            msg.send(channel, sender(member) + i18n.get(member, "commands.playlist.locked"));
            // Private playlists are only usable in the owner's guild
            if (playList.getOwner() != member.getGuild().getOwnerIdLong()) {
                settings.setActivePlayList(null);
                this.musicSettings.save(settings);
            }
            return true;
        }

        Optional<Track> oTrack;
        if (args.length == 0) {
            oTrack = playlists.getNext(playList.getId(), settings.getPosition());
            if (oTrack.isEmpty()) {
                // Failed to start playing (No songs found)
                msg.send(channel, sender(member) + i18n.get(member, "commands.music.play.failed_songs"));
                return true;
            }
        } else {
            String nameOrURL = this.argsToString(args, 0);
            if (Utils.isInt(nameOrURL)) {
                int pos = Integer.parseInt(nameOrURL);
                oTrack = playlists.getByPos(playList.getId(), pos);
            } else if (Utils.isURL(nameOrURL)) {
                nameOrURL = tracks.extract(nameOrURL);
                if (nameOrURL == null) {
                    // Not supported
                    msg.send(channel, sender(member) + i18n.get(member, "commands.music.add.not_supported"));
                    return false;
                }
                oTrack = playlists.findByCode(playList.getId(), nameOrURL);
            } else {
                oTrack = playlists.findByName(playList.getId(), nameOrURL);
            }
            if (oTrack.isEmpty()) {
                // Song not found
                msg.send(channel, sender(member) + i18n.get(member, "commands.music.play.not_found", nameOrURL));
                return true;
            }
        }

        settings.setPosition(oTrack.get().getPosition());
        {
            // TODO: Remove once web is up
            settings.setLogTracks(true);
            settings.setLogChannel(channel.getIdLong());
            this.musicSettings.save(settings);
        }
        this.musicSettings.save(settings);

        AudioTrack track = tracks.toAudioTrack(oTrack.get());
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
