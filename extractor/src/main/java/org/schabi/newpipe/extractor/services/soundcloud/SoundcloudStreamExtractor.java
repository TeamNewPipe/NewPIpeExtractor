package org.schabi.newpipe.extractor.services.soundcloud;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;

import org.schabi.newpipe.extractor.Image;
import org.schabi.newpipe.extractor.MediaFormat;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.exceptions.ContentNotAvailableException;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.LinkHandler;
import org.schabi.newpipe.extractor.localization.DateWrapper;
import org.schabi.newpipe.extractor.stream.AudioStream;
import org.schabi.newpipe.extractor.stream.Description;
import org.schabi.newpipe.extractor.stream.StreamExtractor;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;
import org.schabi.newpipe.extractor.stream.StreamInfoItemsCollector;
import org.schabi.newpipe.extractor.stream.StreamType;
import org.schabi.newpipe.extractor.stream.SubtitlesStream;
import org.schabi.newpipe.extractor.stream.VideoStream;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nonnull;

public class SoundcloudStreamExtractor extends StreamExtractor {
    private JsonObject track;

    public SoundcloudStreamExtractor(StreamingService service, LinkHandler linkHandler) {
        super(service, linkHandler);
    }

    @Override
    public void onFetchPage(@Nonnull Downloader downloader) throws IOException, ExtractionException {
        track = SoundcloudParsingHelper.resolveFor(downloader, getOriginalUrl());

        String policy = track.getString("policy", "");
        if (!policy.equals("ALLOW") && !policy.equals("MONETIZE")) {
            throw new ContentNotAvailableException("Content not available: policy " + policy);
        }
    }

    @Nonnull
    @Override
    public String getId() {
        return track.getInt("id") + "";
    }

    @Nonnull
    @Override
    public String getName() {
        return track.getString("title");
    }

    @Nonnull
    @Override
    public String getTextualUploadDate() {
        return track.getString("created_at");
    }

    @Nonnull
    @Override
    public DateWrapper getUploadDate() throws ParsingException {
        return new DateWrapper(SoundcloudParsingHelper.parseDate(getTextualUploadDate()));
    }

    @Nonnull
    @Override
    public List<Image> getThumbnails() {
        List<Image> images = new ArrayList<>();

        String artworkUrl = track.getString("artwork_url", "");
        if (artworkUrl.isEmpty()) {
            artworkUrl = track.getObject("user").getString("avatar_url", "");
        }

        images.add(new Image(artworkUrl, Image.MEDIUM, Image.MEDIUM));
        images.add(new Image(artworkUrl.replace("large.jpg", "small.jpg"), Image.LOW, Image.LOW));
        images.add(new Image(artworkUrl.replace("large.jpg", "crop.jpg"), Image.HIGH, Image.HIGH));

        return images;
    }

    @Override
    public Description getDescription() {
        return new Description(track.getString("description"), Description.PLAIN_TEXT);
    }

    @Override
    public int getAgeLimit() {
        return NO_AGE_LIMIT;
    }

    @Override
    public long getLength() {
        return track.getNumber("duration", 0).longValue() / 1000L;
    }

    @Override
    public long getTimeStamp() throws ParsingException {
        return getTimestampSeconds("(#t=\\d{0,3}h?\\d{0,3}m?\\d{1,3}s?)");
    }

    @Override
    public long getViewCount() {
        return track.getNumber("playback_count", 0).longValue();
    }

    @Override
    public long getLikeCount() {
        return track.getNumber("favoritings_count", -1).longValue();
    }

    @Override
    public long getDislikeCount() {
        return -1;
    }

    @Nonnull
    @Override
    public String getUploaderUrl() {
        return SoundcloudParsingHelper.getUploaderUrl(track);
    }

    @Nonnull
    @Override
    public String getUploaderName() {
        return SoundcloudParsingHelper.getUploaderName(track);
    }

    @Nonnull
    @Override
    public List<Image> getUploaderAvatars() {
        List<Image> images = new ArrayList<>();

        String avatarUrl = SoundcloudParsingHelper.getAvatarUrl(track);

        images.add(new Image(avatarUrl, Image.MEDIUM, Image.MEDIUM));
        images.add(new Image(avatarUrl.replace("large.jpg", "small.jpg"), Image.LOW, Image.LOW));
        images.add(new Image(avatarUrl.replace("large.jpg", "crop.jpg"), Image.HIGH, Image.HIGH));

        return images;
    }

    @Nonnull
    @Override
    public String getDashMpdUrl() {
        return "";
    }

    @Nonnull
    @Override
    public String getHlsUrl() throws ParsingException {
        return "";
    }

    @Override
    public List<AudioStream> getAudioStreams() throws IOException, ExtractionException {
        List<AudioStream> audioStreams = new ArrayList<>();
        Downloader dl = NewPipe.getDownloader();

        String apiUrl = "https://api-v2.soundcloud.com/tracks/" + urlEncode(getId())
                + "?client_id=" + urlEncode(SoundcloudParsingHelper.clientId());

        String response = dl.get(apiUrl, getExtractorLocalization()).responseBody();
        JsonObject responseObject;
        try {
            responseObject = JsonParser.object().from(response);
        } catch (JsonParserException e) {
            throw new ParsingException("Could not parse json response", e);
        }

        // Streams can be streamable and downloadable - or explicitly not.
        // For playing the track, it is only necessary to have a streamable track.
        // If this is not the case, this track might not be published yet.
        if (!responseObject.getBoolean("streamable")) return audioStreams;

        try {
            JsonArray transcodings = responseObject.getObject("media").getArray("transcodings");

            // get information about what stream formats are available
            for (Object transcoding : transcodings) {

                JsonObject t = (JsonObject) transcoding;
                String url = t.getString("url");

                if (url != null && !url.isEmpty()) {

                    // We can only play the mp3 format, but not handle m3u playlists / streams.
                    // what about Opus?
                    if (t.getString("preset").contains("mp3")
                            && t.getObject("format").getString("protocol").equals("progressive")) {
                        // This url points to the endpoint which generates a unique and short living url to the stream.
                        // TODO: move this to a separate method to generate valid urls when needed (e.g. resuming a paused stream)
                        url += "?client_id=" + SoundcloudParsingHelper.clientId();
                        String res = dl.get(url).responseBody();

                        try {
                            JsonObject mp3UrlObject = JsonParser.object().from(res);
                            // Links in this file are also only valid for a short period.
                            audioStreams.add(new AudioStream(mp3UrlObject.getString("url"),
                                    MediaFormat.MP3, 128));
                        } catch (JsonParserException e) {
                            throw new ParsingException("Could not parse streamable url", e);
                        }
                    }
                }
            }

        } catch (NullPointerException e) {
            throw new ExtractionException("Could not get SoundCloud's track audio url", e);
        }

        return audioStreams;
    }

    private static String urlEncode(String value) {
        try {
            return URLEncoder.encode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public List<VideoStream> getVideoStreams() throws IOException, ExtractionException {
        return null;
    }

    @Override
    public List<VideoStream> getVideoOnlyStreams() throws IOException, ExtractionException {
        return null;
    }

    @Override
    @Nonnull
    public List<SubtitlesStream> getSubtitlesDefault() throws IOException, ExtractionException {
        return Collections.emptyList();
    }

    @Override
    @Nonnull
    public List<SubtitlesStream> getSubtitles(MediaFormat format) throws IOException, ExtractionException {
        return Collections.emptyList();
    }

    @Override
    public StreamType getStreamType() {
        return StreamType.AUDIO_STREAM;
    }

    @Override
    public StreamInfoItem getNextStream() throws IOException, ExtractionException {
        return null;
    }

    @Override
    public StreamInfoItemsCollector getRelatedStreams() throws IOException, ExtractionException {
        StreamInfoItemsCollector collector = new StreamInfoItemsCollector(getServiceId());

        String apiUrl = "https://api-v2.soundcloud.com/tracks/" + urlEncode(getId()) + "/related"
                + "?client_id=" + urlEncode(SoundcloudParsingHelper.clientId());

        SoundcloudParsingHelper.getStreamsFromApi(collector, apiUrl);
        return collector;
    }

    @Override
    public String getErrorMessage() {
        return null;
    }

    @Override
    public String getHost() throws ParsingException {
        return "";
    }

    @Override
    public String getPrivacy() throws ParsingException {
        return "";
    }

    @Override
    public String getCategory() throws ParsingException {
        return "";
    }

    @Override
    public String getLicence() throws ParsingException {
        return "";
    }

    @Override
    public Locale getLanguageInfo() throws ParsingException {
        return null;
    }

    @Nonnull
    @Override
    public List<String> getTags() throws ParsingException {
        return new ArrayList<>();
    }

    @Nonnull
    @Override
    public String getSupportInfo() throws ParsingException {
        return "";
    }
}
