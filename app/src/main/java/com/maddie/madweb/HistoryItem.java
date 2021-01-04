package com.maddie.madweb;

public class HistoryItem {

    private String title;
    private String url;

    public HistoryItem(String t, String u) {
        this.title = t;
        this.url = u;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
