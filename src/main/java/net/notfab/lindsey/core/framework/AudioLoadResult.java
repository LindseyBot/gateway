package net.notfab.lindsey.core.framework;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AudioLoadResult implements AudioLoadResultHandler {

    private final List<AudioTrack> trackList = new ArrayList<>();
    private FriendlyException exception;
    private boolean cached;

    public boolean isFailure() {
        return this.exception != null;
    }

    @Override
    public void trackLoaded(AudioTrack track) {
        this.trackList.add(track);
    }

    @Override
    public void playlistLoaded(AudioPlaylist playlist) {
        this.trackList.addAll(playlist.getTracks());
    }

    @Override
    public void noMatches() {
        this.exception = new FriendlyException("No results found", FriendlyException.Severity.COMMON, new IllegalStateException());
    }

    @Override
    public void loadFailed(FriendlyException exception) {
        this.exception = exception;
    }

    public boolean isPlaylist() {
        return this.trackList.size() > 1;
    }

}
