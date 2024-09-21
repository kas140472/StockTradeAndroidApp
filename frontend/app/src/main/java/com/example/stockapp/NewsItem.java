package com.example.stockapp;

import android.net.Uri;
import android.text.format.DateUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NewsItem {

    private long datetime;
    private String headline;
    private Uri image;
    private String url;

    private String summary;
    private String source;

    public NewsItem(String headline, String summary, String source, long datetime, Uri image, String url) {

        this.headline = headline;
        this.datetime = datetime;
        this.summary = summary;
        this.source = source;
        this.image = image;
        this.url = url;
    }

    public long getDatetime() {
        return datetime;
    }

    public String getFormattedDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
        return dateFormat.format(new Date(datetime * 1000));
    }
    public String getHeadline() {
        return headline;
    }

    public Uri getImage() {
        return image;
    }

    public String getUrl() {
        return url;
    }

    public String getSummary() {
        return summary;
    }

    public String getSource() {
        return source;
    }

}