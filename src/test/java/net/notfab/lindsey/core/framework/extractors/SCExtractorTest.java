package net.notfab.lindsey.core.framework.extractors;

import net.notfab.lindsey.shared.enums.SongSource;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SCExtractorTest {

    @Test
    void isSupported() {
        SCExtractor extractor = new SCExtractor();
        assertTrue(extractor.isSupported("https://soundcloud.com/nocopyrightsounds/cartoon-why-we-lose-feat-coleman-trapp-ncs-release"));
        assertTrue(extractor.isSupported("https://snd.sc/nocopyrightsounds/cartoon-why-we-lose-feat-coleman-trapp-ncs-release"));
    }

    @Test
    void extract() {
        SCExtractor extractor = new SCExtractor();
        assertEquals("nocopyrightsounds/test", extractor.extract("https://soundcloud.com/nocopyrightsounds/test"));
        assertEquals("nocopyrightsounds/test", extractor.extract("https://snd.sc/nocopyrightsounds/test"));
    }

    @Test
    void isPlaylist() {
        SCExtractor extractor = new SCExtractor();
        assertFalse(extractor.isPlaylist("https://soundcloud.com/nocopyrightsounds/test"));
        assertFalse(extractor.isPlaylist("https://snd.sc/nocopyrightsounds/test"));
    }

    @Test
    void getSourceName() {
        SCExtractor extractor = new SCExtractor();
        assertEquals(SongSource.SoundCloud, extractor.getSourceName());
    }

}
