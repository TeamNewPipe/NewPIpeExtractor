package org.schabi.newpipe.extractor.services.media_ccc;

import org.junit.BeforeClass;
import org.junit.Test;
import org.schabi.newpipe.DownloaderTestImpl;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.channel.ChannelExtractor;
import org.schabi.newpipe.extractor.services.media_ccc.extractors.MediaCCCConferenceExtractor;

import static junit.framework.TestCase.assertEquals;
import static org.schabi.newpipe.extractor.ServiceList.MediaCCC;

/**
 * Test {@link MediaCCCConferenceExtractor}
 */
public class MediaCCCConferenceExtractorTest {
    private static ChannelExtractor extractor;

    @BeforeClass
    public static void setUpClass() throws Exception {
        NewPipe.init(DownloaderTestImpl.getInstance());
        extractor = MediaCCC.getChannelExtractor("https://api.media.ccc.de/public/conferences/froscon2017");
        extractor.fetchPage();
    }

    @Test
    public void testName() throws Exception {
        assertEquals("FrOSCon 2017", extractor.getName());
    }

    @Test
    public void testGetUrl() throws Exception {
        assertEquals("https://api.media.ccc.de/public/conferences/froscon2017", extractor.getUrl());
    }

    @Test
    public void testGetOriginalUrl() throws Exception {
        assertEquals("https://media.ccc.de/c/froscon2017", extractor.getOriginalUrl());
    }

    @Test
    public void testGetThumbnails() throws Exception {
        assertEquals("https://static.media.ccc.de/media/events/froscon/2017/logo.png", extractor.getAvatars().get(0).getUrl());
    }

    @Test
    public void testGetInitalPage() throws Exception {
        assertEquals(97, extractor.getInitialPage().getItems().size());
    }
}
