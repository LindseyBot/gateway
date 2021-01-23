package net.notfab.lindsey.core.service;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.managers.AudioManager;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.notfab.lindsey.core.framework.AudioPlayerSendHandler;
import net.notfab.lindsey.core.framework.PlaybackListener;
import net.notfab.lindsey.core.framework.i18n.Messenger;
import net.notfab.lindsey.core.framework.i18n.Translator;
import net.notfab.lindsey.core.framework.models.PlayList;
import net.notfab.lindsey.core.framework.models.PlayListCursor;
import net.notfab.lindsey.core.framework.models.Song;
import net.notfab.lindsey.core.framework.profile.ProfileManager;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class AudioService {

    private final AudioPlayerManager playerManager;
    private final Map<Long, AudioPlayer> playerMap = new HashMap<>();
    private final Map<Long, PlaybackListener> listenerMap = new HashMap<>();
    private final PlayListService playlists;
    private final ShardManager shardManager;
    private final Messenger msg;
    private final Translator i18n;
    private final SongService songs;
    private final ProfileManager profiles;

    public AudioService(AudioPlayerManager playerManager, PlayListService playlists,
                        ShardManager shardManager, Messenger msg, Translator i18n, SongService songs, ProfileManager profiles) {
        this.playerManager = playerManager;
        this.playlists = playlists;
        this.shardManager = shardManager;
        this.msg = msg;
        this.i18n = i18n;
        this.songs = songs;
        this.profiles = profiles;
    }

    /**
     * Connects to a voice channel.
     *
     * @param guild   - Guild.
     * @param channel - Voice Channel.
     * @return If connection was successful.
     */
    public boolean connect(Guild guild, VoiceChannel channel) {
        AudioManager audioManager = guild.getAudioManager();
        try {
            audioManager.openAudioConnection(channel);
        } catch (InsufficientPermissionException exception) {
            return false;
        }
        AudioPlayer player = this.createPlayer(guild.getIdLong());
        audioManager.setSendingHandler(new AudioPlayerSendHandler(player));
        return true;
    }

    /**
     * Destroys a voice connection.
     *
     * @param guild - Guild.
     */
    public void destroy(Guild guild) {
        if (!hasPlayer(guild.getIdLong())) {
            return;
        }
        AudioManager audioManager = guild.getAudioManager();
        audioManager.closeAudioConnection();
        audioManager.setSendingHandler(null);

        AudioPlayer player = this.playerMap.get(guild.getIdLong());
        player.destroy();
        this.playerMap.remove(guild.getIdLong());

        PlaybackListener listener = this.listenerMap.get(guild.getIdLong());
        player.removeListener(listener);
        this.listenerMap.remove(guild.getIdLong());
    }

    /**
     * Starts playing a track in the specified guild.
     *
     * @param guild      - Guild.
     * @param audioTrack - Audio Track.
     * @return If playback has started.
     */
    public boolean play(Guild guild, AudioTrack audioTrack) {
        if (!isConnected(guild)) {
            return false;
        }
        AudioPlayer player = this.createPlayer(guild.getIdLong());
        player.playTrack(audioTrack);
        return true;
    }

    /**
     * Checks if the specified guild has a currently-open voice connection.
     *
     * @param guild - Guild.
     * @return If there is an active voice connection.
     */
    public boolean isConnected(Guild guild) {
        if (!hasPlayer(guild.getIdLong())) {
            return false;
        }
        return guild.getAudioManager().isConnected();
    }

    /**
     * Creates an AudioPlayer if needed for this guild.
     *
     * @param guild - Guild.
     * @return AudioPlayer.
     */
    private AudioPlayer createPlayer(long guild) {
        if (hasPlayer(guild)) {
            return this.playerMap.get(guild);
        }
        PlaybackListener listener = new PlaybackListener(guild, this);
        listenerMap.put(guild, listener);

        AudioPlayer player = this.playerManager.createPlayer();
        player.addListener(listener);

        this.playerMap.put(guild, player);
        return player;
    }

    private boolean hasPlayer(long guild) {
        return this.playerMap.containsKey(guild);
    }

    public void onStarted(long guildId, AudioTrack track) {
        Guild guild = this.shardManager.getGuildById(guildId);
        if (guild == null) {
            return;
        }
        Optional<PlayList> oPlayList = playlists.findActive(guildId);
        if (oPlayList.isEmpty()) {
            // No playlist active
            return;
        }
        PlayListCursor cursor = this.profiles.get(guild).getCursor();
        if (cursor == null) {
            return;
        }
        this.msg.sendMusic(guildId, i18n.get(guild, "commands.music.play.playing", cursor.getPosition(), track.getInfo().title));
    }

    public void onFinished(long guildId, AudioTrackEndReason reason) {
        Guild guild = this.shardManager.getGuildById(guildId);
        if (guild == null) {
            return;
        }
        if (reason != AudioTrackEndReason.REPLACED) {
            // Ignored because was replaced
            return;
        }
        if (!reason.mayStartNext) {
            // Internal error?
            this.msg.sendMusic(guildId, i18n.get(guild, "commands.music.play.failed_internal"));
            return;
        }
        Optional<PlayList> oPlayList = playlists.findActive(guildId);
        if (oPlayList.isEmpty()) {
            // No playlist active
            this.msg.sendMusic(guildId, i18n.get(guild, "commands.playlist.no_active"));
            return;
        }
        Song song = playlists.findNextSong(oPlayList.get(), guildId);
        if (song == null) {
            // No songs left
            this.msg.sendMusic(guildId, i18n.get(guild, "commands.music.play.failed_songs"));
            return;
        }
        if (!playlists.updateCursor(oPlayList.get(), song, guildId)) {
            // Failed to update cursor
            this.msg.sendMusic(guildId, i18n.get(guild, "commands.music.play.failed_internal"));
            return;
        }
        AudioTrack track = songs.toAudioTrack(song);
        if (!this.play(guild, track)) {
            // Failed to start playing (No voice connection)
            msg.sendMusic(guildId, i18n.get(guild, "commands.music.play.failed_voice"));
        }
    }

}
