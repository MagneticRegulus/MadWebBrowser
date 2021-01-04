package com.maddie.madweb;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.util.Objects;

public class Bookmark {

    private String title;
    private String url;

    public Bookmark(String t, String u) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Bookmark bookmark = (Bookmark) o;
        return title.equals(bookmark.title) &&
                url.equals(bookmark.url);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public int hashCode() {
        return Objects.hash(title, url);
    }
}
