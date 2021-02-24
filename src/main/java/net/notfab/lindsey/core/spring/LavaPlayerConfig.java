package net.notfab.lindsey.core.spring;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class LavaPlayerConfig {

    @Bean
    public AudioPlayerManager audioPlayerManager(List<AudioSourceManager> managers) {
        AudioPlayerManager playerManager = new DefaultAudioPlayerManager();
        for (AudioSourceManager manager : managers) {
            playerManager.registerSourceManager(manager);
        }
        return playerManager;
    }

    @Bean
    protected YoutubeAudioSourceManager youtubeAudioSourceManager() {
        return new YoutubeAudioSourceManager();
    }

    @Bean
    protected HttpAudioSourceManager httpAudioSourceManager() {
        return new HttpAudioSourceManager();
    }

    @Bean
    protected SoundCloudAudioSourceManager soundCloudAudioSourceManager() {
        return SoundCloudAudioSourceManager.createDefault();
    }

}
