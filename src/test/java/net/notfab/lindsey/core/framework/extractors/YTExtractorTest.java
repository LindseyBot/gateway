package net.notfab.lindsey.core.framework.extractors;

import net.notfab.lindsey.core.framework.models.SongSource;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class YTExtractorTest {

    @Test
    void isSupported() {
        YTExtractor extractor = new YTExtractor();

        // HTTPS
        assertTrue(extractor.isSupported("https://www.youtube.com/watch?v=J2X5mJ3HDYE"));
        assertTrue(extractor.isSupported("https://www.youtube.com/watch/J2X5mJ3HDYE"));
        assertTrue(extractor.isSupported("https://www.youtube.com/embed/J2X5mJ3HDYE"));
        // HTTP
        assertTrue(extractor.isSupported("http://www.youtube.com/watch?v=J2X5mJ3HDYE"));
        assertTrue(extractor.isSupported("http://www.youtube.com/watch/J2X5mJ3HDYE"));
        assertTrue(extractor.isSupported("http://www.youtube.com/embed/J2X5mJ3HDYE"));
        // WWW
        assertTrue(extractor.isSupported("www.youtube.com/watch?v=J2X5mJ3HDYE"));
        assertTrue(extractor.isSupported("www.youtube.com/watch/J2X5mJ3HDYE"));
        assertTrue(extractor.isSupported("www.youtube.com/embed/J2X5mJ3HDYE"));
        // Short-URL
        assertTrue(extractor.isSupported("https://youtu.be/J2X5mJ3HDYE"));
        assertTrue(extractor.isSupported("http://youtu.be/J2X5mJ3HDYE"));

        // Youtube Music
        assertTrue(extractor.isSupported("https://m.youtube.com/watch?v=J2X5mJ3HDYE"));
        assertTrue(extractor.isSupported("http://m.youtube.com/watch?v=J2X5mJ3HDYE"));
        assertTrue(extractor.isSupported("m.youtube.com/watch?v=J2X5mJ3HDYE"));
        assertTrue(extractor.isSupported("https://music.youtube.com/watch?v=J2X5mJ3HDYE"));
        assertTrue(extractor.isSupported("http://music.youtube.com/watch?v=J2X5mJ3HDYE"));
        assertTrue(extractor.isSupported("music.youtube.com/watch?v=J2X5mJ3HDYE"));

        // PlayList
        assertTrue(extractor.isSupported("https://www.youtube.com/playlist?list=PLm5kZdW495u6Ox7Oq9OB-YSlPq-FFdd4a"));
    }

    @Test
    void extract() {
        YTExtractor extractor = new YTExtractor();

        // HTTPS
        assertEquals("J2X5mJ3HDYE", extractor.extract("https://www.youtube.com/watch?v=J2X5mJ3HDYE"));
        assertEquals("J2X5mJ3HDYE", extractor.extract("https://www.youtube.com/watch/J2X5mJ3HDYE"));
        assertEquals("J2X5mJ3HDYE", extractor.extract("https://www.youtube.com/embed/J2X5mJ3HDYE"));
        // HTTP
        assertEquals("J2X5mJ3HDYE", extractor.extract("http://www.youtube.com/watch?v=J2X5mJ3HDYE"));
        assertEquals("J2X5mJ3HDYE", extractor.extract("http://www.youtube.com/watch/J2X5mJ3HDYE"));
        assertEquals("J2X5mJ3HDYE", extractor.extract("http://www.youtube.com/embed/J2X5mJ3HDYE"));
        // WWW
        assertEquals("J2X5mJ3HDYE", extractor.extract("www.youtube.com/watch?v=J2X5mJ3HDYE"));
        assertEquals("J2X5mJ3HDYE", extractor.extract("www.youtube.com/watch/J2X5mJ3HDYE"));
        assertEquals("J2X5mJ3HDYE", extractor.extract("www.youtube.com/embed/J2X5mJ3HDYE"));
        // Short-URL
        assertEquals("J2X5mJ3HDYE", extractor.extract("https://youtu.be/J2X5mJ3HDYE"));
        assertEquals("J2X5mJ3HDYE", extractor.extract("http://youtu.be/J2X5mJ3HDYE"));

        // Youtube Music
        assertEquals("J2X5mJ3HDYE", extractor.extract("https://m.youtube.com/watch?v=J2X5mJ3HDYE"));
        assertEquals("J2X5mJ3HDYE", extractor.extract("http://m.youtube.com/watch?v=J2X5mJ3HDYE"));
        assertEquals("J2X5mJ3HDYE", extractor.extract("m.youtube.com/watch?v=J2X5mJ3HDYE"));
        assertEquals("J2X5mJ3HDYE", extractor.extract("https://music.youtube.com/watch?v=J2X5mJ3HDYE"));
        assertEquals("J2X5mJ3HDYE", extractor.extract("http://music.youtube.com/watch?v=J2X5mJ3HDYE"));
        assertEquals("J2X5mJ3HDYE", extractor.extract("music.youtube.com/watch?v=J2X5mJ3HDYE"));

        // PlayList
        assertEquals("PLm5kZdW495u6Ox7Oq9OB-YSlPq-FFdd4a",
            extractor.extract("https://www.youtube.com/playlist?list=PLm5kZdW495u6Ox7Oq9OB-YSlPq-FFdd4a"));
    }

    @Test
    void isPlaylist() {
        YTExtractor extractor = new YTExtractor();

        assertFalse(extractor.isPlaylist("https://www.youtube.com/watch?v=J2X5mJ3HDYE"));
        assertTrue(extractor.isPlaylist("https://www.youtube.com/playlist?list=PLm5kZdW495u6Ox7Oq9OB-YSlPq-FFdd4a"));
    }

    @Test
    void getSourceName() {
        YTExtractor extractor = new YTExtractor();
        assertEquals(SongSource.Youtube, extractor.getSourceName());
    }

}
