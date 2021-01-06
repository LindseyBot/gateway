package net.notfab.lindsey.core.framework.extractors;

import net.notfab.lindsey.core.framework.models.SongSource;

public interface Extractor {

    boolean isSupported(String url);

    String extract(String url);

    boolean isPlaylist(String url);

    SongSource getSourceName();

}
