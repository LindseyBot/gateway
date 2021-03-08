package net.notfab.lindsey.core.service;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioTrack;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioTrack;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import com.sedmelluq.discord.lavaplayer.track.info.AudioTrackInfoBuilder;
import net.notfab.lindsey.core.framework.AudioLoadResult;
import net.notfab.lindsey.core.framework.extractors.Extractor;
import net.notfab.lindsey.shared.entities.music.Track;
import net.notfab.lindsey.shared.enums.SongSource;
import net.notfab.lindsey.shared.repositories.sql.TrackRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
public class TrackService {

    private final AudioPlayerManager playerManager;
    private final List<Extractor> extractors;

    private final YoutubeAudioSourceManager youtube;
    private final SoundCloudAudioSourceManager soundCloud;

    private final TrackRepository repository;

    public TrackService(AudioPlayerManager playerManager, List<Extractor> extractors,
                        YoutubeAudioSourceManager youtube,
                        SoundCloudAudioSourceManager soundCloud, TrackRepository repository) {
        this.playerManager = playerManager;
        this.extractors = extractors;
        this.youtube = youtube;
        this.soundCloud = soundCloud;
        this.repository = repository;
    }

    public AudioLoadResult loadTrack(String url) {
        AudioLoadResult result = this.loadFromCache(url);
        if (result.isCached()) {
            return result;
        }
        FriendlyException blacklistError = this.isBlacklisted(url);
        if (blacklistError != null) {
            result.setException(blacklistError);
            return result;
        }
        try {
            playerManager.loadItem(url, result)
                .get(5, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            result.setException(new FriendlyException("Timed out", FriendlyException.Severity.SUSPICIOUS, new IllegalStateException()));
        }
        if (result.isFailure()) {
            this.addToBlacklist(url, result.getException());
        } else {
            this.addToCache(url, result);
        }
        return result;
    }

    public AudioLoadResult search(String name) {
        AudioLoadResult result = new AudioLoadResult();
        result.setException(new FriendlyException("Not Implemented", FriendlyException.Severity.FAULT, new IllegalStateException()));
        return result;
    }

    private AudioLoadResult loadFromCache(String url) {
        // TODO: Implement caching loading strategy
        AudioLoadResult result = new AudioLoadResult();
        result.setCached(false);
        return new AudioLoadResult();
    }

    private void addToCache(String url, AudioLoadResult result) {
        // TODO: Implement caching strategy
    }

    private FriendlyException isBlacklisted(String url) {
        // TODO: Implement track blacklisting
        return null;
    }

    private void addToBlacklist(String url, FriendlyException exception) {
        // TODO: Implement blacklisting
    }

    // ---

    public String extract(String url) {
        for (Extractor extractor : this.extractors) {
            if (!extractor.isSupported(url) || extractor.isPlaylist(url)) {
                continue;
            }
            return extractor.extract(url);
        }
        return null;
    }

    public Track create(AudioTrack track) {
        return this.create(track.getInfo());
    }

    public Track create(AudioTrackInfo info) {
        for (Extractor extractor : this.extractors) {
            if (!extractor.isSupported(info.uri) || extractor.isPlaylist(info.uri)) {
                continue;
            }
            String code = extractor.extract(info.uri);

            Optional<Track> oTrack = this.repository.findById(code);
            if (oTrack.isPresent()) {
                return oTrack.get();
            }
            Track track = new Track();
            track.setCode(code);
            track.setTitle(info.title);
            track.setAuthor(info.author);
            track.setSource(extractor.getSourceName());
            track.setStream(info.isStream);
            track.setDuration(info.length);
            track.setCached(false);

            return this.repository.save(track);
        }
        return null;
    }

    public AudioTrack toAudioTrack(Track track) {
        AudioTrackInfo info = AudioTrackInfoBuilder.empty()
            .setTitle(track.getTitle())
            .setAuthor(track.getAuthor())
            .setLength(track.getDuration())
            .setIdentifier(track.getCode())
            .setIsStream(track.isStream())
            .setUri(this.getUrl(track))
            .build();
        if (track.getSource() == SongSource.Youtube) {
            return new YoutubeAudioTrack(info, youtube);
        } else if (track.getSource() == SongSource.SoundCloud) {
            return new SoundCloudAudioTrack(info, soundCloud);
        } else {
            return this.loadTrack(info.uri).getTrackList().get(0);
        }
    }

    private String getUrl(Track track) {
        // TODO: cache
        for (Extractor extractor : this.extractors) {
            if (extractor.getSourceName() != track.getSource()) {
                continue;
            }
            return extractor.getUrl(track.getCode());
        }
        return track.getCode();
    }

}
