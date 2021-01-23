package org.schabi.newpipe.extractor.services.youtube.stream;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.schabi.newpipe.downloader.DownloaderTestImpl;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.services.DefaultStreamExtractorTest;
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeStreamLinkHandlerFactory;
import org.schabi.newpipe.extractor.stream.StreamExtractor;
import org.schabi.newpipe.extractor.stream.StreamType;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Nullable;

import static org.schabi.newpipe.extractor.ServiceList.YouTube;

/**
 * Test for {@link YoutubeStreamLinkHandlerFactory}
 */
public class YoutubeStreamExtractorControversialTest extends DefaultStreamExtractorTest {
    private static final String ID = "T4XJQO3qol8";
    private static final String URL = YoutubeStreamExtractorDefaultTest.BASE_URL + ID;
    private static StreamExtractor extractor;

    @BeforeClass
    public static void setUp() throws Exception {
        NewPipe.init(DownloaderTestImpl.getInstance());
        extractor = YouTube.getStreamExtractor(URL);
        extractor.fetchPage();
    }

    @Override public StreamExtractor extractor() { return extractor; }
    @Override public StreamingService expectedService() { return YouTube; }
    @Override public String expectedName() { return "Burning Everyone's Koran"; }
    @Override public String expectedId() { return ID; }
    @Override public String expectedUrlContains() { return URL; }
    @Override public String expectedOriginalUrlContains() { return URL; }

    @Override public StreamType expectedStreamType() { return StreamType.VIDEO_STREAM; }
    @Override public String expectedUploaderName() { return "Amazing Atheist"; }
    @Override public String expectedUploaderUrl() { return "https://www.youtube.com/channel/UCjNxszyFPasDdRoD9J6X-sw"; }
    @Override public List<String> expectedDescriptionContains() {
        return Arrays.asList("http://www.huffingtonpost.com/2010/09/09/obama-gma-interview-quran_n_710282.html",
                "freedom");
    }
    @Override public long expectedLength() { return 219; }
    @Override public long expectedViewCountAtLeast() { return 285000; }
    @Nullable @Override public String expectedUploadDate() { return "2010-09-09 00:00:00.000"; }
    @Nullable @Override public String expectedTextualUploadDate() { return "2010-09-09"; }
    @Override public long expectedLikeCountAtLeast() { return 13300; }
    @Override public long expectedDislikeCountAtLeast() { return 2600; }

    @Override
    @Test
    @Ignore("TODO fix")
    public void testErrorMessage() throws Exception {
        super.testErrorMessage();
    }
}
