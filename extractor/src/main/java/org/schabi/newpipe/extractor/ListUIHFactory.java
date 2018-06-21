package org.schabi.newpipe.extractor;

import org.schabi.newpipe.extractor.exceptions.ParsingException;

import java.util.ArrayList;
import java.util.List;

public abstract class ListUIHFactory extends UIHFactory {

    protected List<String> contentFilter = new ArrayList<>(0);
    protected String sortFilter = "";

    public ListUIHFactory setQuery(String id,
                                   List<String> contentFilter,
                                   String sortFilter) throws ParsingException {
        setId(id);
        this.contentFilter = contentFilter;
        this.sortFilter = sortFilter;
        return this;
    }


    public ListUIHFactory setQuery(String id) throws ParsingException {
        setQuery(id, new ArrayList<String>(), "");
        return this;
    }

    public ListUIHFactory setUrl(String url) throws ParsingException {
        return (ListUIHFactory) super.setUrl(url);
    }

    public ListUIHFactory setId(String id) throws ParsingException {
        return (ListUIHFactory) super.setId(id);
    }

    /**
     * Will returns content filter the corresponding extractor can handle like "channels", "videos", "music", etc.
     *
     * @return filter that can be applied when building a query for getting a list
     */
    public String[] getAvailableContentFilter() {
        return new String[0];
    }

    /**
     * Will returns sort filter the corresponding extractor can handle like "A-Z", "oldest first", "size", etc.
     *
     * @return filter that can be applied when building a query for getting a list
     */
    public String[] getAvailableSortFilter() {
        return new String[0];
    }


    public List<String> getContentFilter() {
        return contentFilter;
    }

    public String getSortFilter() {
        return sortFilter;
    }
}
