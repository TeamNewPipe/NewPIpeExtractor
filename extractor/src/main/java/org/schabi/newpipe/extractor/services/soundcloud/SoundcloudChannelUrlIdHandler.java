package org.schabi.newpipe.extractor.services.soundcloud;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.schabi.newpipe.extractor.ListUrlIdHandler;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.UrlIdHandler;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.utils.Parser;
import org.schabi.newpipe.extractor.utils.Utils;

import static org.schabi.newpipe.extractor.utils.Utils.replaceHttpWithHttps;

public class SoundcloudChannelUrlIdHandler extends ListUrlIdHandler {
    private static final SoundcloudChannelUrlIdHandler instance = new SoundcloudChannelUrlIdHandler();
    private final String URL_PATTERN = "^https?://(www\\.|m\\.)?soundcloud.com/[0-9a-z_-]+" +
            "(/((tracks|albums|sets|reposts|followers|following)/?)?)?([#?].*)?$";

    public static SoundcloudChannelUrlIdHandler getInstance() {
        return instance;
    }

    @Override
    public String getUrl(String id, String[] contentFilter, String sortFilter) throws ParsingException {
        try {
            return SoundcloudParsingHelper.resolveUrlWithEmbedPlayer("https://api.soundcloud.com/users/" + id);
        } catch (Exception e) {
            throw new ParsingException(e.getMessage(), e);
        }
    }

    @Override
    public String getId(String url) throws ParsingException {
        Utils.checkUrl(URL_PATTERN, url);

        try {
            return SoundcloudParsingHelper.resolveIdWithEmbedPlayer(url);
        } catch (Exception e) {
            throw new ParsingException(e.getMessage(), e);
        }
    }

    @Override
    public String cleanUrl(String complexUrl) throws ParsingException {
        Utils.checkUrl(URL_PATTERN, complexUrl);

        try {
            Element ogElement = Jsoup.parse(NewPipe.getDownloader().download(complexUrl))
                    .select("meta[property=og:url]").first();

            return replaceHttpWithHttps(ogElement.attr("content"));
        } catch (Exception e) {
            throw new ParsingException(e.getMessage(), e);
        }
    }

    @Override
    public boolean acceptUrl(String url) {
        return Parser.isMatch(URL_PATTERN, url.toLowerCase());
    }
}
