package net.notfab.lindsey.core.service;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.managers.AudioManager;
import net.notfab.lindsey.core.framework.AudioPlayerSendHandler;
import net.notfab.lindsey.core.framework.PlaybackListener;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AudioService {

    private final AudioPlayerManager playerManager;
    private final Map<Long, AudioPlayer> playerMap = new HashMap<>();
    private final Map<Long, PlaybackListener> listenerMap = new HashMap<>();
    private final PlayListService playlists;

    public AudioService(AudioPlayerManager playerManager, PlayListService playlists) {
        this.playerManager = playerManager;
        this.playlists = playlists;
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
        PlaybackListener listener = new PlaybackListener(guild, this, this.playlists);
        listenerMap.put(guild, listener);

        AudioPlayer player = this.playerManager.createPlayer();
        player.addListener(listener);

        this.playerMap.put(guild, player);
        return player;
    }

    private boolean hasPlayer(long guild) {
        return this.playerMap.containsKey(guild);
    }

}
