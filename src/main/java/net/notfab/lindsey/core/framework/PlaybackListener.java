package net.notfab.lindsey.core.framework;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import lombok.extern.slf4j.Slf4j;
import net.notfab.lindsey.core.service.AudioService;

@Slf4j
public class PlaybackListener extends AudioEventAdapter {

    private final AudioService service;
    private final long guild;

    public PlaybackListener(long guild, AudioService service) {
        this.guild = guild;
        this.service = service;
    }

    @Override
    public void onTrackStart(AudioPlayer player, AudioTrack track) {
        this.service.onStarted(this.guild, track);
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack oldTrack, AudioTrackEndReason endReason) {
        this.service.onFinished(this.guild, endReason);
    }

    @Override
    public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception) {
        log.error("Track exception [" + track.getInfo().identifier + "]", exception);
        this.service.onFinished(this.guild, AudioTrackEndReason.LOAD_FAILED);
    }

    @Override
    public void onTrackStuck(AudioPlayer player, AudioTrack track, long thresholdMs) {
        log.warn("Stuck [" + track.getInfo().identifier + "/" + track.getInfo().title + "] for " + thresholdMs);
        this.service.onFinished(this.guild, AudioTrackEndReason.LOAD_FAILED);
    }

}
