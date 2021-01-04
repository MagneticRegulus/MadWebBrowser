package com.maddie.madweb;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Controller controller;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.controller = new Controller(this);
        loadPreferences();

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onResume() {
        super.onResume();
        loadPreferences();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBackPressed() {
        if (controller.getIsBrowser()) {
            controller.goBack();
        } else {
            controller.setMainView(controller.getCurrentUrl());
        }

    }

    @Override
    protected void onStop() {
        savePreferences();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        savePreferences();
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(controller.getToggle().onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    protected void loadPreferences() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences("WebPref", 0); // 0 - for private mode

        if (pref.contains("url")) {
            controller.setMainView(pref.getString("url", null));
        } else {
            controller.setMainView(null);
        }
        if (pref.contains("homepage")) {
            Controller.setHomepage(pref.getString("homepage", null));
        }
        if (pref.contains("back")) {
            controller.setBackHistoryAsDeque(getHistoryList("back"));
        }
        if (pref.contains("forward")) {
            controller.setForwardHistoryAsDeque(getHistoryList("forward"));
        }
        if (pref.contains("bookmarks")) {
            controller.setBookmarksAsSet(getBookmarkList());
        }
    }

    protected void savePreferences()  {
        SharedPreferences pref = getApplicationContext().getSharedPreferences("WebPref", 0); // 0 - for private mode
        SharedPreferences.Editor editor = pref.edit();

        editor.putString("url", controller.getCurrentUrl());
        editor.putString("homepage", Controller.getHomepage());
        setList(editor,"back", controller.getBackHistoryAsList());
        setList(editor,"forward", controller.getForwardHistoryAsList());
        setList(editor,"bookmarks", controller.getBookmarksAsList());
        editor.commit();
        Log.d("SAVE", "Preferences saved.");

    }

    public <T> void setList(SharedPreferences.Editor editor, String key, List<T> list) {
        Gson gson = new Gson();
        String json = gson.toJson(list);
        set(editor, key, json);
    }

    public List<HistoryItem> getHistoryList(String key) {
        SharedPreferences pref = getApplicationContext().getSharedPreferences("WebPref", 0);
        List<HistoryItem> history = new ArrayList<>();
        String serializedObject = pref.getString(key, null);
        if (serializedObject != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<List<HistoryItem>>(){}.getType();
            history = gson.fromJson(serializedObject, type);
        }
        return history;
    }

    public List<Bookmark> getBookmarkList() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences("WebPref", 0);
        List<Bookmark> bookmarks = new ArrayList<>();
        String serializedObject = pref.getString("bookmarks", null);
        if (serializedObject != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<List<Bookmark>>(){}.getType();
            bookmarks = gson.fromJson(serializedObject, type);
        }
        return bookmarks;
    }

    public void set(SharedPreferences.Editor editor, String key, String value) {
        editor.putString(key, value);
    }
}