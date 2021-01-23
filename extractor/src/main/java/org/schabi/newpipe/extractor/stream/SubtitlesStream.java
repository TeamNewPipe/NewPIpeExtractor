package org.schabi.newpipe.extractor.stream;

import org.schabi.newpipe.extractor.MediaFormat;

import java.io.Serializable;
import java.util.Locale;

public class SubtitlesStream extends Stream implements Serializable {
    private final MediaFormat format;
    private final Locale locale;
    private final boolean autoGenerated;
    private final String code;

    public SubtitlesStream(MediaFormat format, String languageCode, String url, boolean autoGenerated) {
        super(url, format);

        /*
        * Locale.forLanguageTag only for API >= 21
        * Locale.Builder only for API >= 21
        * Country codes doesn't work well without
        */
        final String[] splits = languageCode.split("-");
        switch (splits.length) {
            default:
                this.locale = new Locale(splits[0]);
                break;
            case 3:
                this.locale = new Locale(splits[0], splits[1], splits[2]);// complex variants doesn't work!
                break;
            case 2:
                this.locale = new Locale(splits[0], splits[1]);
                break;
        }
        this.code = languageCode;
        this.format = format;
        this.autoGenerated = autoGenerated;
    }

    public String getExtension() {
        return format.suffix;
    }

    public boolean isAutoGenerated() {
        return autoGenerated;
    }

    @Override
    public boolean equalStats(Stream cmp) {
        return super.equalStats(cmp) &&
                cmp instanceof SubtitlesStream &&
                code.equals(((SubtitlesStream) cmp).code) &&
                autoGenerated == ((SubtitlesStream) cmp).autoGenerated;
    }

    public String getDisplayLanguageName() {
        return locale.getDisplayName(locale);
    }

    public String getLanguageTag() {
        return code;
    }

    public Locale getLocale() {
        return locale;
    }

}
