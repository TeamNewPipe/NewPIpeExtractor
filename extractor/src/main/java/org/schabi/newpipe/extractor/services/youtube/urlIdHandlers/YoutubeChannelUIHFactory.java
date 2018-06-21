package org.schabi.newpipe.extractor.services.youtube.urlIdHandlers;

import org.schabi.newpipe.extractor.ListUIHFactory;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.utils.Parser;

/*
 * Created by Christian Schabesberger on 25.07.16.
 *
 * Copyright (C) Christian Schabesberger 2016 <chris.schabesberger@mailbox.org>
 * YoutubeChannelUIHFactory.java is part of NewPipe.
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

public class YoutubeChannelUIHFactory extends ListUIHFactory {

    private static final YoutubeChannelUIHFactory instance = new YoutubeChannelUIHFactory();
    private static final String ID_PATTERN = "/(user/[A-Za-z0-9_-]*|channel/[A-Za-z0-9_-]*)";

    public static YoutubeChannelUIHFactory getInstance() {
        return instance;
    }

    @Override
    public String onGetIdFromUrl(String url) throws ParsingException {
        return Parser.matchGroup1(ID_PATTERN, url);
    }

    @Override
    public String getUrl() {
        return "https://www.youtube.com/" + id;
    }

    @Override
    public boolean onAcceptUrl(String url) {
        return (url.contains("youtube") || url.contains("youtu.be") || url.contains("hooktube.com"))
                && (url.contains("/user/") || url.contains("/channel/"));
    }
}
