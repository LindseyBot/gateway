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
import net.notfab.lindsey.shared.entities.playlist.Song;
import net.notfab.lindsey.shared.enums.SongSource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
public class SongService {

    private final AudioPlayerManager playerManager;
    private final List<Extractor> extractors;

    private final YoutubeAudioSourceManager youtube;
    private final SoundCloudAudioSourceManager soundCloud;

    public SongService(AudioPlayerManager playerManager, List<Extractor> extractors,
                       YoutubeAudioSourceManager youtube,
                       SoundCloudAudioSourceManager soundCloud) {
        this.playerManager = playerManager;
        this.extractors = extractors;
        this.youtube = youtube;
        this.soundCloud = soundCloud;
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

    public String normalize(String url) {
        for (Extractor extractor : this.extractors) {
            if (!extractor.isSupported(url) || extractor.isPlaylist(url)) {
                continue;
            }
            String identifier = extractor.extract(url);
            return extractor.getSourceName().getBaseURL() + identifier;
        }
        return url;
    }

    public Song createSong(AudioTrack track) {
        return this.createSong(track.getInfo());
    }

    public Song createSong(AudioTrackInfo info) {
        for (Extractor extractor : this.extractors) {
            if (!extractor.isSupported(info.uri) || extractor.isPlaylist(info.uri)) {
                continue;
            }
            Song song = new Song();
            song.setName(info.title);
            song.setAuthor(info.author);
            song.setSource(extractor.getSourceName());
            song.setStream(info.isStream);
            song.setLength(info.length);

            String identifier = extractor.extract(info.uri);
            song.setIdentifier(identifier);
            String url = extractor.getSourceName().getBaseURL() + identifier;
            song.setUrl(url);
            return song;
        }
        return null;
    }

    public AudioTrack toAudioTrack(Song song) {
        AudioTrackInfo info = AudioTrackInfoBuilder.empty()
            .setTitle(song.getName())
            .setAuthor(song.getAuthor())
            .setLength(song.getLength())
            .setIdentifier(song.getIdentifier())
            .setIsStream(song.isStream())
            .setUri(song.getUrl())
            .build();
        if (song.getSource() == SongSource.Youtube) {
            // Inject cache here
            return new YoutubeAudioTrack(info, youtube);
        } else if (song.getSource() == SongSource.SoundCloud) {
            // Inject cache here
            return new SoundCloudAudioTrack(info, soundCloud);
        } else {
            return this.loadTrack(song.getUrl()).getTrackList().get(0);
        }
    }

}
