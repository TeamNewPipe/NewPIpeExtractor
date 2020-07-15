package org.schabi.newpipe.extractor.services.youtube.search;

import org.junit.Test;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.schabi.newpipe.extractor.ServiceList.YOUTUBE;
import static org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeSearchQueryHandlerFactory.CHANNELS;
import static org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeSearchQueryHandlerFactory.MUSIC_SONGS;
import static org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeSearchQueryHandlerFactory.PLAYLISTS;
import static org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeSearchQueryHandlerFactory.VIDEOS;

public class YoutubeSearchQHTest {
    @Test
    public void testRegularValues() throws Exception {
        assertEquals("https://www.youtube.com/results?search_query=asdf",
                YOUTUBE.getSearchQHFactory().fromQuery("asdf").getUrl());
        assertEquals("https://www.youtube.com/results?search_query=hans",
                YOUTUBE.getSearchQHFactory().fromQuery("hans").getUrl());
        assertEquals("https://www.youtube.com/results?search_query=Poifj%26jaijf",
                YOUTUBE.getSearchQHFactory().fromQuery("Poifj&jaijf").getUrl());
        assertEquals("https://www.youtube.com/results?search_query=G%C3%BCl%C3%BCm",
                YOUTUBE.getSearchQHFactory().fromQuery("Gülüm").getUrl());
        assertEquals("https://www.youtube.com/results?search_query=%3Fj%24%29H%C2%A7B",
                YOUTUBE.getSearchQHFactory().fromQuery("?j$)H§B").getUrl());

        assertEquals("https://music.youtube.com/search?q=asdf",
                YOUTUBE.getSearchQHFactory().fromQuery("asdf", singletonList(MUSIC_SONGS), "").getUrl());
        assertEquals("https://music.youtube.com/search?q=hans",
                YOUTUBE.getSearchQHFactory().fromQuery("hans", singletonList(MUSIC_SONGS), "").getUrl());
        assertEquals("https://music.youtube.com/search?q=Poifj%26jaijf",
                YOUTUBE.getSearchQHFactory().fromQuery("Poifj&jaijf", singletonList(MUSIC_SONGS), "").getUrl());
        assertEquals("https://music.youtube.com/search?q=G%C3%BCl%C3%BCm",
                YOUTUBE.getSearchQHFactory().fromQuery("Gülüm", singletonList(MUSIC_SONGS), "").getUrl());
        assertEquals("https://music.youtube.com/search?q=%3Fj%24%29H%C2%A7B",
                YOUTUBE.getSearchQHFactory().fromQuery("?j$)H§B", singletonList(MUSIC_SONGS), "").getUrl());
    }

    @Test
    public void testGetContentFilter() throws Exception {
        assertEquals(VIDEOS, YOUTUBE.getSearchQHFactory()
                .fromQuery("", singletonList(VIDEOS), "").getContentFilters().get(0));
        assertEquals(CHANNELS, YOUTUBE.getSearchQHFactory()
                .fromQuery("asdf", singletonList(CHANNELS), "").getContentFilters().get(0));

        assertEquals(MUSIC_SONGS, YOUTUBE.getSearchQHFactory()
                .fromQuery("asdf", singletonList(MUSIC_SONGS), "").getContentFilters().get(0));
    }

    @Test
    public void testWithContentfilter() throws Exception {
        assertEquals("https://www.youtube.com/results?search_query=asdf&sp=EgIQAQ%253D%253D",
                YOUTUBE.getSearchQHFactory().fromQuery("asdf", singletonList(VIDEOS), "")
                        .getUrl());
        assertEquals("https://www.youtube.com/results?search_query=asdf&sp=EgIQAg%253D%253D",
                YOUTUBE.getSearchQHFactory().fromQuery("asdf", singletonList(CHANNELS), "")
                        .getUrl());
        assertEquals("https://www.youtube.com/results?search_query=asdf&sp=EgIQAw%253D%253D",
                YOUTUBE.getSearchQHFactory().fromQuery("asdf", singletonList(PLAYLISTS), "")
                        .getUrl());
        assertEquals("https://www.youtube.com/results?search_query=asdf",
                YOUTUBE.getSearchQHFactory().fromQuery("asdf", singletonList("fjiijie"), "")
                        .getUrl());

        assertEquals("https://music.youtube.com/search?q=asdf", YOUTUBE.getSearchQHFactory()
                .fromQuery("asdf", singletonList(MUSIC_SONGS), "").getUrl());
    }

    @Test
    public void testGetAvailableContentFilter() {
        final String[] contentFilter = YOUTUBE.getSearchQHFactory().getAvailableContentFilter();
        assertEquals(8, contentFilter.length);
        assertEquals("all", contentFilter[0]);
        assertEquals("videos", contentFilter[1]);
        assertEquals("channels", contentFilter[2]);
        assertEquals("playlists", contentFilter[3]);
        assertEquals("music_songs", contentFilter[4]);
        assertEquals("music_videos", contentFilter[5]);
        assertEquals("music_albums", contentFilter[6]);
        assertEquals("music_playlists", contentFilter[7]);
    }

    @Test
    public void testGetAvailableSortFilter() {
        final String[] contentFilter = YOUTUBE.getSearchQHFactory().getAvailableSortFilter();
        assertEquals(0, contentFilter.length);
    }
}
