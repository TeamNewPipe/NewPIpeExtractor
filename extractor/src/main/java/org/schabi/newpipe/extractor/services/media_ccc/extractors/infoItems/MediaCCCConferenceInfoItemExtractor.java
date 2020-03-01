package org.schabi.newpipe.extractor.services.media_ccc.extractors.infoItems;

import com.grack.nanojson.JsonObject;

import org.schabi.newpipe.extractor.Image;
import org.schabi.newpipe.extractor.channel.ChannelInfoItemExtractor;
import org.schabi.newpipe.extractor.exceptions.ParsingException;

import java.util.ArrayList;
import java.util.List;

public class MediaCCCConferenceInfoItemExtractor implements ChannelInfoItemExtractor {

    JsonObject conference;

    public MediaCCCConferenceInfoItemExtractor(JsonObject conference) {
        this.conference = conference;
    }

    @Override
    public String getDescription() throws ParsingException {
        return "";
    }

    @Override
    public long getSubscriberCount() throws ParsingException {
        return -1;
    }

    @Override
    public long getStreamCount() throws ParsingException {
        return -1;
    }

    @Override
    public String getName() throws ParsingException {
        return conference.getString("title");
    }

    @Override
    public String getUrl() throws ParsingException {
        return conference.getString("url");
    }

    @Override
    public List<Image> getThumbnails() throws ParsingException {
        List<Image> images = new ArrayList<>();
        images.add(new Image(conference.getString("logo_url"), Image.HIGH, Image.HIGH));
        return images;
    }
}
