package net.notfab.lindsey.core.framework.extractors;

import net.notfab.lindsey.shared.enums.SongSource;

public interface Extractor {

    boolean isSupported(String url);

    String extract(String url);

    boolean isPlaylist(String url);

    SongSource getSourceName();

    String getUrl(String code);

}
