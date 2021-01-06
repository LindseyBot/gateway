package net.notfab.lindsey.core.framework.extractors;

import net.notfab.lindsey.core.framework.models.SongSource;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class YTExtractor implements Extractor {

    private final String BASE_REGEX = "(?:https?://|)(?:www\\.|m\\.|music\\.|)youtu(?:be\\.com|\\.be)";
    private final String VIDEO_REGEX = "/(?:embed/|watch/|watch\\?v=|)([a-zA-Z0-9_-]{11})";
    private final String PLAYLIST_REGEX = "/(?:playlist(?:\\?list=|/)/?)?((?:PL|LL|FL|UU)[a-zA-Z0-9_-]+)";

    private final Pattern VIDEO_PATTERN = Pattern.compile("^" + BASE_REGEX + VIDEO_REGEX);
    private final Pattern PLAYLIST_PATTERN = Pattern.compile("^" + BASE_REGEX + PLAYLIST_REGEX);

    @Override
    public boolean isSupported(String url) {
        return VIDEO_PATTERN.matcher(url).find() || PLAYLIST_PATTERN.matcher(url).find();
    }

    @Override
    public String extract(String url) {
        Matcher matcher = VIDEO_PATTERN.matcher(url);
        if (matcher.find()) {
            return matcher.group(1);
        }
        matcher = PLAYLIST_PATTERN.matcher(url);
        if (matcher.find()) {
            return matcher.group(1);
        }
        throw new IllegalArgumentException("URL Not supported");
    }

    @Override
    public boolean isPlaylist(String url) {
        return PLAYLIST_PATTERN.matcher(url).find();
    }

    @Override
    public SongSource getSourceName() {
        return SongSource.Youtube;
    }

}
