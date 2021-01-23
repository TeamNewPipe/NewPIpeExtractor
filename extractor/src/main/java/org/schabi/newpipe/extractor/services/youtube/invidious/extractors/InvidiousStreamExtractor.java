package org.schabi.newpipe.extractor.services.youtube.invidious.extractors;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import org.schabi.newpipe.extractor.MediaFormat;
import org.schabi.newpipe.extractor.MetaInfo;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.downloader.Response;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.LinkHandler;
import org.schabi.newpipe.extractor.localization.DateWrapper;
import org.schabi.newpipe.extractor.services.youtube.ItagItem;
import org.schabi.newpipe.extractor.services.youtube.invidious.InvidiousParsingHelper;
import org.schabi.newpipe.extractor.stream.*;
import org.schabi.newpipe.extractor.utils.JsonUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.*;

import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.fixThumbnailUrl;
import static org.schabi.newpipe.extractor.utils.Utils.isBlank;

/*
 * Copyright (C) 2020 Team NewPipe <tnp@newpipe.schabi.org>
 * InvidiousStreamExtractor.java is part of NewPipe Extractor.
 *
 * NewPipe Extractor is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * NewPipe Extractor is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with NewPipe Extractor.  If not, see <https://www.gnu.org/licenses/>.
 */

public class InvidiousStreamExtractor extends StreamExtractor {

    private JsonObject json;
    private String baseUrl;

    public InvidiousStreamExtractor(StreamingService service, LinkHandler linkHandler) {
        super(service, linkHandler);
        baseUrl = service.getInstance().getUrl();
    }

    @Nullable
    @Override
    public String getTextualUploadDate() {
        return json.getString("publishedText");
        // Depends on instance localization
    }

    @Nullable
    @Override
    public DateWrapper getUploadDate() {
        return InvidiousParsingHelper.getUploadDateFromEpochTime(json.getNumber("published").longValue());
    }

    @Nonnull
    @Override
    public String getThumbnailUrl() {
        final JsonArray thumbnail = json.getArray("authorThumbnails");
        final String url = thumbnail.getObject(0).getString("url");
        return fixThumbnailUrl(url);
    }

    @Nonnull
    @Override
    public Description getDescription() {
        final String descriptionHtml = json.getString("descriptionHtml");
        if (!isBlank(descriptionHtml) || descriptionHtml.equals("<p></p>")) {
            return new Description(descriptionHtml, Description.HTML);
        }

        return new Description(json.getString("description"), Description.PLAIN_TEXT);
    }

    @Override
    public int getAgeLimit() {
        final boolean isFamilyFriendly = json.getBoolean("isFamilyFriendly");
        return isFamilyFriendly ? NO_AGE_LIMIT : 18;
    }

    @Override
    public long getLength() {
        return json.getNumber("lengthSeconds").longValue();
    }

    @Override
    public long getTimeStamp() throws ParsingException {
        return getTimestampSeconds("((#|&|\\?)t=\\d{0,3}h?\\d{0,3}m?\\d{1,3}s?)");
        // from YouTubeStreamExtractor
    }

    @Override
    public long getViewCount() {
        return json.getNumber("viewCount").longValue();
    }

    @Override
    public long getLikeCount() {
        return json.getNumber("likeCount").longValue();
    }

    @Override
    public long getDislikeCount() {
        return json.getNumber("dislikeCount").longValue();
    }

    @Nonnull
    @Override
    public String getUploaderUrl() {
        return baseUrl + json.getString("authorUrl");
    }

    @Nonnull
    @Override
    public String getUploaderName() {
        return json.getString("author");
    }

    @Nonnull
    @Override
    public String getUploaderAvatarUrl() {
        final JsonArray avatars = json.getArray("authorThumbnails");
        final String url = avatars.getObject(0).getString("url");
        return fixThumbnailUrl(url);
    }

    @Nonnull
    @Override
    public String getSubChannelUrl() {
        return "";
    }

    @Nonnull
    @Override
    public String getSubChannelName() {
        return "";
    }

    @Nonnull
    @Override
    public String getSubChannelAvatarUrl() {
        return "";
    }

    @Nonnull
    @Override
    public String getDashMpdUrl() {
        return baseUrl + json.getString("dashUrl");
    }

    @Nonnull
    @Override
    public String getHlsUrl() {
        final String hlsUrl = json.getString("hlsUrl");
        return hlsUrl != null ? hlsUrl : "";
    }

    @Override
    public List<AudioStream> getAudioStreams() throws ExtractionException {
        List<AudioStream> audioStreams = new ArrayList<>();
        try {
            for (Map.Entry<String, ItagItem> entry : getItags("adaptiveFormats", ItagItem.ItagType.AUDIO).entrySet()) {
                ItagItem itag = entry.getValue();

                AudioStream audioStream = new AudioStream(entry.getKey(), itag.getMediaFormat(), itag.avgBitrate);
                if (!Stream.containSimilarStream(audioStream, audioStreams)) {
                    audioStreams.add(audioStream);
                }
            }
        } catch (Exception e) {
            throw new ParsingException("Could not get audio streams", e);
        }

        return audioStreams;
    }

    @Override
    public List<VideoStream> getVideoStreams() throws ExtractionException {
        List<VideoStream> videoStreams = new ArrayList<>();
        try {
            for (Map.Entry<String, ItagItem> entry : getItags("formatStreams", ItagItem.ItagType.VIDEO).entrySet()) {
                ItagItem itag = entry.getValue();

                VideoStream videoStream = new VideoStream(entry.getKey(), itag.getMediaFormat(), itag.resolutionString);
                if (!Stream.containSimilarStream(videoStream, videoStreams)) {
                    videoStreams.add(videoStream);
                }
            }
        } catch (Exception e) {
            throw new ParsingException("Could not get video streams", e);
        }

        return videoStreams;
    }

    @Override
    public List<VideoStream> getVideoOnlyStreams() throws ExtractionException {
        List<VideoStream> videoOnlyStreams = new ArrayList<>();
        try {
            for (Map.Entry<String, ItagItem> entry : getItags("adaptiveFormats", ItagItem.ItagType.VIDEO_ONLY).entrySet()) {
                ItagItem itag = entry.getValue();

                VideoStream videoStream = new VideoStream(entry.getKey(), itag.getMediaFormat(), itag.resolutionString, true);
                if (!Stream.containSimilarStream(videoStream, videoOnlyStreams)) {
                    videoOnlyStreams.add(videoStream);
                }
            }
        } catch (Exception e) {
            throw new ParsingException("Could not get video only streams", e);
        }

        return videoOnlyStreams;
    }

    @Nonnull
    @Override
    public List<SubtitlesStream> getSubtitlesDefault() {
        return getSubtitles(MediaFormat.VTT);
    }

    @Nonnull
    @Override
    public List<SubtitlesStream> getSubtitles(MediaFormat format) {
        final JsonArray captions = json.getArray("captions");
        List<SubtitlesStream> subtitles = new ArrayList<>(captions.size());
        for (Object o : captions) {
            final JsonObject obj = (JsonObject) o;
            final String languageCode = obj.getString("languageCode");
            subtitles.add(new SubtitlesStream(format, languageCode, baseUrl + obj.getString("url"), languageCode.contains("(auto-generated)")));
        }

        return subtitles;
    }

    @Override
    public StreamType getStreamType() {
        if (json.getBoolean("liveNow")) {
            return StreamType.LIVE_STREAM;
        } else {
            return StreamType.VIDEO_STREAM;
        }
    }

    @Override
    public StreamInfoItemsCollector getRelatedStreams() throws ExtractionException {
        try {
            final StreamInfoItemsCollector collector = new StreamInfoItemsCollector(getServiceId());
            final JsonArray relatedStreams = json.getArray("recommendedVideos");
            for (Object o : relatedStreams) {
                collector.commit(new InvidiousStreamInfoItemExtractor((JsonObject) o, baseUrl));
            }
            return collector;
        } catch (Exception e) {
            throw new ParsingException("Could not get related videos", e);
        }
    }

    @Override
    public String getErrorMessage() {
        return null;
    }

    @Nonnull
    @Override
    public String getHost() {
        return "";
    }

    @Nonnull
    @Override
    public String getPrivacy() {
        return json.getBoolean("isListed") ? "Public" : "Unlisted";
    }

    @Nonnull
    @Override
    public String getCategory() {
        return json.getString("genre");
    }

    @Nonnull
    @Override
    public String getLicence() {
        return "";
    }

    @Nullable
    @Override
    public Locale getLanguageInfo() {
        return null;
    }

    @Nonnull
    @Override
    public List<String> getTags() {
        return JsonUtils.getListStringFromJsonArray(json.getArray("keywords"));
    }

    @Nonnull
    @Override
    public String getSupportInfo() {
        return "";
    }

    @Nonnull
    @Override
    public List<StreamSegment> getStreamSegments() throws ParsingException {
        return null;
    }

    @Nonnull
    @Override
    public List<MetaInfo> getMetaInfo() throws ParsingException {
        return null;
    }

    @Override
    public void onFetchPage(@Nonnull Downloader downloader) throws IOException, ExtractionException {
        final String apiUrl = baseUrl + "/api/v1/videos/" + getId() +
                "?region=" + getExtractorContentCountry().getCountryCode();

        final Response response = downloader.get(apiUrl);

        json = InvidiousParsingHelper.getValidJsonObjectFromResponse(response, apiUrl);
    }

    @Nonnull
    @Override
    public String getName() throws ParsingException {
        return json.getString("title");
    }

    private Map<String, ItagItem> getItags(final String streamingDataKey, ItagItem.ItagType itagTypeWanted) throws ParsingException {
        final JsonArray formats = json.getArray(streamingDataKey);

        Map<String, ItagItem> urlAndItags = new LinkedHashMap<>();
        for (Object o : formats) {
            JsonObject formatData = (JsonObject) o;
            int itag = Integer.parseInt(formatData.getString("itag"));

            if (ItagItem.isSupported(itag)) {
                ItagItem itagItem = ItagItem.getItag(itag);
                if (itagItem.itagType == itagTypeWanted) {
                    urlAndItags.put(formatData.getString("url"), itagItem);
                }
            }
        }

        return urlAndItags;
    }
}
