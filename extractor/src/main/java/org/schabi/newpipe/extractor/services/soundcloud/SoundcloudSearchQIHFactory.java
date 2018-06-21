package org.schabi.newpipe.extractor.services.soundcloud;

import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException;
import org.schabi.newpipe.extractor.search.SearchQIHFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class SoundcloudSearchQIHFactory extends SearchQIHFactory {
    public static final String CHARSET_UTF_8 = "UTF-8";

    public static final String TRACKS = "tracks";
    public static final String USERS = "users";
    public static final String PLAYLIST = "playlist";
    public static final String ANY = "any";

    public static final int ITEMS_PER_PAGE = 10;

    @Override
    public String getUrl() throws ParsingException {
        try {
            String url = "https://api-v2.soundcloud.com/search";

            if(getContentFilter().size() > 0) {
                switch (getContentFilter().get(0)) {
                    case TRACKS:
                        url += "/tracks";
                        break;
                    case USERS:
                        url += "/users";
                        break;
                    case PLAYLIST:
                        url += "/playlists";
                        break;
                    case ANY:
                    default:
                        break;
                }
            }

            return url + "?q=" + URLEncoder.encode(id, CHARSET_UTF_8)
                    + "&client_id=" + SoundcloudParsingHelper.clientId()
                    + "&limit=" + ITEMS_PER_PAGE
                    + "&offset=0";

        } catch (UnsupportedEncodingException e) {
            throw new ParsingException("Could not encode query", e);
        } catch (IOException e) {
            throw new ParsingException("Could not get client id", e);
        } catch (ReCaptchaException e) {
            throw new ParsingException("ReCaptcha required", e);
        }
    }

    @Override
    public String[] getAvailableContentFilter() {
        return new String[] {
            TRACKS,
            USERS,
            PLAYLIST,
            ANY};
    }
}
