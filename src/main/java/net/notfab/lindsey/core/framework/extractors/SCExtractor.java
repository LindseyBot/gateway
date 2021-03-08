package net.notfab.lindsey.core.framework.extractors;

import net.notfab.lindsey.shared.enums.SongSource;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class SCExtractor implements Extractor {

    private final Pattern pattern = Pattern.compile("^https?://(soundcloud\\.com|snd\\.sc)/(.*)$");

    @Override
    public boolean isSupported(String url) {
        return pattern.matcher(url).find();
    }

    @Override
    public String extract(String url) {
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            return matcher.group(2);
        }
        throw new IllegalArgumentException("URL Not supported");
    }

    @Override
    public boolean isPlaylist(String url) {
        return false;
    }

    @Override
    public SongSource getSourceName() {
        return SongSource.SoundCloud;
    }

    @Override
    public String getUrl(String code) {
        return getSourceName().getBaseURL() + code;
    }

}
