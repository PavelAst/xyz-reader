package com.example.xyzreader.remote;

import android.util.Log;

import java.net.MalformedURLException;
import java.net.URL;

public class Config {

    public static final URL BASE_URL;
    private static String TAG = Config.class.toString();
    private static final String ARTICLE_URL = "https://go.udacity.com/xyz-reader-json";
    private static final String ARTICLE_2_URL = "https://raw.githubusercontent.com/TNTest/xyzreader/master/data.json";

    static {
        URL url = null;
        try {
            url = new URL(ARTICLE_URL);
        } catch (MalformedURLException e) {
            // DONE: throw a real error
            Log.e(TAG, "Please check your internet connection.");
            throw new IllegalStateException(e);
        }

        BASE_URL = url;
    }
}
