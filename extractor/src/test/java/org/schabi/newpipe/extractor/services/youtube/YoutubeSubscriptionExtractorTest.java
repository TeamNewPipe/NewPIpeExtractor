package org.schabi.newpipe.extractor.services.youtube;

import org.junit.BeforeClass;
import org.junit.Test;
import org.schabi.newpipe.DownloaderTestImpl;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.ServiceList;
import org.schabi.newpipe.extractor.linkhandler.LinkHandlerFactory;
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeSubscriptionExtractor;
import org.schabi.newpipe.extractor.subscription.SubscriptionExtractor;
import org.schabi.newpipe.extractor.subscription.SubscriptionItem;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test for {@link YoutubeSubscriptionExtractor}
 */
public class YoutubeSubscriptionExtractorTest {
    private static YoutubeSubscriptionExtractor subscriptionExtractor;
    private static LinkHandlerFactory urlHandler;

    @BeforeClass
    public static void setupClass() {
        NewPipe.init(DownloaderTestImpl.getInstance());
        subscriptionExtractor = new YoutubeSubscriptionExtractor(ServiceList.YOUTUBE);
        urlHandler = ServiceList.YOUTUBE.getChannelLHFactory();
    }

    @Test
    public void testFromInputStream() throws Exception {
        File testFile = new File("extractor/src/test/resources/youtube_export_test.xml");
        if (!testFile.exists()) {
            testFile = new File("src/test/resources/youtube_export_test.xml");
        }

        final List<SubscriptionItem> subscriptionItems = subscriptionExtractor.fromInputStream(new FileInputStream(testFile));
        assertEquals("List doesn't have exactly 8 items (had " + subscriptionItems.size() + ")", 8, subscriptionItems.size());

        for (final SubscriptionItem item : subscriptionItems) {
            assertNotNull(item.getName());
            assertNotNull(item.getUrl());
            assertTrue(urlHandler.acceptUrl(item.getUrl()));
            assertNotEquals(item.getServiceId(), -1);
        }
    }

    @Test
    public void testEmptySourceException() throws Exception {
        final String emptySource = "<opml version=\"1.1\"><body>" +
                "<outline text=\"Testing\" title=\"123\" />" +
                "</body></opml>";

        final List<SubscriptionItem> items = subscriptionExtractor.fromInputStream(new ByteArrayInputStream(emptySource.getBytes("UTF-8")));
        assertTrue(items.isEmpty());
    }

    @Test
    public void testSubscriptionWithEmptyTitleInSource() throws Exception {
        final String channelId = "AA0AaAa0AaaaAAAAAA0aa0AA";
        final String source = "<opml version=\"1.1\"><body><outline text=\"YouTube Subscriptions\" title=\"YouTube Subscriptions\">" +
                "<outline text=\"\" title=\"\" type=\"rss\" xmlUrl=\"https://www.youtube.com/feeds/videos.xml?channel_id=" + channelId + "\" />" +
                "</outline></body></opml>";

        final List<SubscriptionItem> items = subscriptionExtractor.fromInputStream(new ByteArrayInputStream(source.getBytes("UTF-8")));
        assertEquals("List doesn't have exactly 1 item (had " + items.size() + ")", 1, items.size());
        assertTrue("Item does not have an empty title (had \"" + items.get(0).getName() + "\")", items.get(0).getName().isEmpty());
        assertTrue("Item does not have the right channel id \"" + channelId + "\" (the whole url is \"" + items.get(0).getUrl() + "\")", items.get(0).getUrl().endsWith(channelId));
    }

    @Test
    public void testSubscriptionWithInvalidUrlInSource() throws Exception {
        final String source = "<opml version=\"1.1\"><body><outline text=\"YouTube Subscriptions\" title=\"YouTube Subscriptions\">" +
                "<outline text=\"invalid\" title=\"url\" type=\"rss\" xmlUrl=\"https://www.youtube.com/feeds/videos.xml?channel_not_id=|||||||\"/>" +
                "<outline text=\"fail\" title=\"fail\" type=\"rss\" xmlUgrl=\"invalidTag\"/>" +
                "<outline text=\"invalid\" title=\"url\" type=\"rss\" xmlUrl=\"\"/>" +
                "<outline text=\"\" title=\"\" type=\"rss\" xmlUrl=\"\"/>" +
                "</outline></body></opml>";

        final List<SubscriptionItem> items = subscriptionExtractor.fromInputStream(new ByteArrayInputStream(source.getBytes("UTF-8")));
        assertTrue(items.isEmpty());
    }

    @Test
    public void testInvalidSourceException() {
        final List<String> invalidList = Arrays.asList(
                "<xml><notvalid></notvalid></xml>",
                "<opml><notvalid></notvalid></opml>",
                "<opml><body></body></opml>",
                "",
                null,
                "\uD83D\uDC28\uD83D\uDC28\uD83D\uDC28",
                "gibberish");

        for (final String invalidContent : invalidList) {
            try {
                if (invalidContent != null) {
                    final byte[] bytes = invalidContent.getBytes("UTF-8");
                    subscriptionExtractor.fromInputStream(new ByteArrayInputStream(bytes));
                    fail("Extracting from \"" + invalidContent + "\" didn't throw an exception");
                } else {
                    subscriptionExtractor.fromInputStream(null);
                    fail("Extracting from null String didn't throw an exception");
                }
            } catch (Exception e) {
                // System.out.println(" -> " + e);
                final boolean isExpectedException = e instanceof SubscriptionExtractor.InvalidSourceException;
                assertTrue("\"" + e.getClass().getSimpleName() + "\" is not the expected exception", isExpectedException);
            }
        }
    }
}
