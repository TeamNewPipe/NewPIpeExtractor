package org.schabi.newpipe.extractor.services.youtube.extractors;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;

import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.localization.DateWrapper;
import org.schabi.newpipe.extractor.localization.TimeAgoParser;
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeParsingHelper;
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeStreamLinkHandlerFactory;
import org.schabi.newpipe.extractor.stream.StreamInfoItemExtractor;
import org.schabi.newpipe.extractor.stream.StreamType;
import org.schabi.newpipe.extractor.utils.Utils;

import javax.annotation.Nullable;

import static org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeParsingHelper.fixThumbnailUrl;
import static org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeParsingHelper.getTextFromObject;
import static org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeParsingHelper.getUrlFromNavigationEndpoint;

/*
 * Copyright (C) Christian Schabesberger 2016 <chris.schabesberger@mailbox.org>
 * YoutubeStreamInfoItemExtractor.java is part of NewPipe.
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

public class YoutubeStreamInfoItemExtractor implements StreamInfoItemExtractor {
    private static final YoutubeStreamLinkHandlerFactory youtubeStreamLinkHandler =
        YoutubeStreamLinkHandlerFactory.getInstance();

    private JsonObject videoInfo;
    private final TimeAgoParser timeAgoParser;

    /**
     * Creates an extractor of StreamInfoItems from a YouTube page.
     *
     * @param videoInfoItem The JSON page element
     * @param timeAgoParser A parser of the textual dates or {@code null}.
     */
    public YoutubeStreamInfoItemExtractor(JsonObject videoInfoItem, @Nullable TimeAgoParser timeAgoParser) {
        this.videoInfo = videoInfoItem;
        this.timeAgoParser = timeAgoParser;
    }

    @Override
    public StreamType getStreamType() {
        try {
            if (videoInfo.getArray("badges").getObject(0).getObject("metadataBadgeRenderer").getString("label").equals("LIVE NOW")) {
                return StreamType.LIVE_STREAM;
            }
        } catch (Exception ignored) {}
        return StreamType.VIDEO_STREAM;
    }

    @Override
    public boolean isAd() throws ParsingException {
        return isPremium() || getName().equals("[Private video]") || getName().equals("[Deleted video]");
    }

    @Override
    public String getId() throws ParsingException {
        return youtubeStreamLinkHandler.getId(getUrl());
    }

    @Override
    public String getUrl() throws ParsingException {
        try {
            String videoId = videoInfo.getString("videoId");
            return YoutubeStreamLinkHandlerFactory.getInstance().getUrl(videoId);
        } catch (Exception e) {
            throw new ParsingException("Could not get url", e);
        }
    }

    @Override
    public String getName() throws ParsingException {
        String name = getTextFromObject(videoInfo.getObject("title"));
        if (name != null && !name.isEmpty()) return name;
        throw new ParsingException("Could not get name");
    }

    @Override
    public long getDuration() throws ParsingException {
        if (getStreamType() == StreamType.LIVE_STREAM) return -1;

        String duration = null;

        try {
            duration = getTextFromObject(videoInfo.getObject("lengthText"));
        } catch (Exception ignored) {}

        if (duration == null) {
            try {
                for (Object thumbnailOverlay : videoInfo.getArray("thumbnailOverlays")) {
                    if (((JsonObject) thumbnailOverlay).getObject("thumbnailOverlayTimeStatusRenderer") != null) {
                        duration = getTextFromObject(((JsonObject) thumbnailOverlay)
                                .getObject("thumbnailOverlayTimeStatusRenderer").getObject("text"));
                    }
                }
            } catch (Exception ignored) {}

            if (duration == null) throw new ParsingException("Could not get duration");
        }

        return YoutubeParsingHelper.parseDurationString(duration);
    }

    @Override
    public String getUploaderName() throws ParsingException {
        String name = null;

        try {
            name = getTextFromObject(videoInfo.getObject("longBylineText"));
        } catch (Exception ignored) {}

        if (name == null) {
            try {
                name = getTextFromObject(videoInfo.getObject("ownerText"));
            } catch (Exception ignored) {}

            if (name == null) {
                try {
                    name = getTextFromObject(videoInfo.getObject("shortBylineText"));
                } catch (Exception ignored) {}

                if (name == null) throw new ParsingException("Could not get uploader name");
            }
        }

        return name;
    }

    @Override
    public String getUploaderUrl() throws ParsingException {
        String url = null;

        try {
            url = getUrlFromNavigationEndpoint(videoInfo.getObject("longBylineText")
                    .getArray("runs").getObject(0).getObject("navigationEndpoint"));
        } catch (Exception ignored) {}

        if (url == null) {
            try {
                url = getUrlFromNavigationEndpoint(videoInfo.getObject("ownerText")
                        .getArray("runs").getObject(0).getObject("navigationEndpoint"));
            } catch (Exception ignored) {}

            if (url == null) {
                try {
                    url = getUrlFromNavigationEndpoint(videoInfo.getObject("shortBylineText")
                            .getArray("runs").getObject(0).getObject("navigationEndpoint"));
                } catch (Exception ignored) {}

                if (url == null) throw new ParsingException("Could not get uploader url");
            }
        }

        return url;
    }

    @Nullable
    @Override
    public String getTextualUploadDate() {
        try {
            return getTextFromObject(videoInfo.getObject("publishedTimeText"));
        } catch (Exception e) {
            // upload date is not always available, e.g. in playlists
            return null;
        }
    }

    @Nullable
    @Override
    public DateWrapper getUploadDate() throws ParsingException {
        String textualUploadDate = getTextualUploadDate();
        if (timeAgoParser != null && textualUploadDate != null && !textualUploadDate.isEmpty()) {
            try {
                return timeAgoParser.parse(textualUploadDate);
            } catch (ParsingException e) {
                throw new ParsingException("Could not get upload date", e);
            }
        }
        return null;
    }

    @Override
    public long getViewCount() throws ParsingException {
        try {
            if (videoInfo.getObject("topStandaloneBadge") != null || isPremium()) {
                return -1;
            }
            String viewCount = getTextFromObject(videoInfo.getObject("viewCountText"));

            return Long.parseLong(Utils.removeNonDigitCharacters(viewCount));
        } catch (NumberFormatException e) {
            return -1;
        } catch (Exception e) {
            throw new ParsingException("Could not get view count", e);
        }
    }

    @Override
    public String getThumbnailUrl() throws ParsingException {
        try {
            // TODO: Don't simply get the first item, but look at all thumbnails and their resolution
            String url = videoInfo.getObject("thumbnail").getArray("thumbnails")
                    .getObject(0).getString("url");

            return fixThumbnailUrl(url);
        } catch (Exception e) {
            throw new ParsingException("Could not get thumbnail url", e);
        }
    }

    private boolean isPremium() {
        try {
            JsonArray badges = videoInfo.getArray("badges");
            for (Object badge : badges) {
                if (((JsonObject) badge).getObject("metadataBadgeRenderer").getString("label").equals("Premium")) {
                    return true;
                }
            }
        } catch (Exception ignored) {}
        return false;
    }
}
