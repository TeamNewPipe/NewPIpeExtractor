package org.schabi.newpipe.extractor.services.youtube;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.schabi.newpipe.Downloader;
import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.search.SearchEngine;
import org.schabi.newpipe.extractor.search.SearchResult;

import static org.junit.Assert.*;
import static org.schabi.newpipe.extractor.ExtractorAsserts.assertIsValidUrl;
import static org.schabi.newpipe.extractor.ServiceList.YouTube;


/*
 * Created by Christian Schabesberger on 29.12.15.
 *
 * Copyright (C) Christian Schabesberger 2015 <chris.schabesberger@mailbox.org>
 * YoutubeSearchEngineStreamTest.java is part of NewPipe.
 *
 * NewPipe is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * NewPipe is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with NewPipe.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Test for {@link SearchEngine}
 */
public class YoutubeSearchEngineStreamTest {
    private static SearchResult result;

    @BeforeClass
    public static void setUp() throws Exception {
        NewPipe.init(Downloader.getInstance());
        SearchEngine engine = YouTube.getService().getSearchEngine();

        // Youtube will suggest "results" instead of "rsults",
        // keep in mind that the suggestions can change by country (the parameter "de")
        result = engine.search("abc", 0, "de", SearchEngine.Filter.STREAM)
                .getSearchResult();
    }

    @Test
    public void testResultList() {
        assertFalse(result.resultList.isEmpty());
        for(InfoItem item: result.getResults()) {
            assertIsValidUrl(item.url);
        }
    }

    @Test
    public void testResultsItemType() {
        for (InfoItem infoItem : result.resultList) {
            assertEquals(InfoItem.InfoType.STREAM, infoItem.info_type);
        }
    }

    @Test
    public void testResultErrors() {
        assertNotNull(result.errors);
        if (!result.errors.isEmpty()) for (Throwable error : result.errors) error.printStackTrace();
        assertTrue(result.errors.isEmpty());
    }

    @Ignore
    @Test
    public void testSuggestion() {
        //todo write a real test
        assertTrue(result.suggestion != null);
    }
}
