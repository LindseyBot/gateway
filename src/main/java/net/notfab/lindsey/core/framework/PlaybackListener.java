package net.notfab.lindsey.core.framework;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import net.notfab.lindsey.core.service.AudioService;

public class PlaybackListener extends AudioEventAdapter {

    private final AudioService service;
    private final long guild;

    public PlaybackListener(long guild, AudioService service) {
        this.guild = guild;
        this.service = service;
    }

    @Override
    public void onTrackStart(AudioPlayer player, AudioTrack track) {
        // Track was started
        System.out.println("Started playing " + track.getInfo().title);
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        if (endReason.mayStartNext) {
            System.out.println("May start next");
        } else {
            System.out.println("May not start next");
        }
    }

    @Override
    public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception) {
        // Ignored?
    }

    @Override
    public void onTrackStuck(AudioPlayer player, AudioTrack track, long thresholdMs) {
        // Replace
        System.out.println("Stuck " + track.getInfo().title + " for " + thresholdMs);
    }

}
