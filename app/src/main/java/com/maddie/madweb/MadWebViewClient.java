package com.maddie.madweb;

import android.graphics.Bitmap;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;

import java.util.ArrayDeque;

public class MadWebViewClient extends WebViewClient {

    private Controller controller;

    public MadWebViewClient(Controller controller) {
        super();
        this.controller = controller;
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);
        Log.d("URL", "Loading... : " + url);
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        controller.addToHistory();
        Log.d("URL", "Loading... Finish: " + url);
    }

    @Override
    public void onLoadResource(WebView view, String url) {
        super.onLoadResource(view, url);
    }
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        Log.d("URL", "You clicked on a link: " + url);
        controller.addToHistory();
        return super.shouldOverrideUrlLoading(view, url);
    }
}
