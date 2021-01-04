package com.maddie.madweb;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.Editable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.navigation.NavigationView;

import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Text;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class Controller {

    private boolean isBrowser;
    private ActionBarDrawerToggle toggle;
    private MainActivity theActivity;
    private WebView madWebView;
    private EditText navbar;
    private DrawerLayout drawer;
    private MadWebViewClient client;
    private HistoryCustomAdapter historyAdapter;
    private BookmarksCustomAdapter bookmarksAdapter;
    private ArrayDeque<HistoryItem> backHistory;
    private ArrayDeque<HistoryItem> forwardHistory;
    private HashSet<Bookmark> bookmarks;
    private static String homepage = "https://www.google.com";

    public Controller(MainActivity activity){
        this.theActivity = activity;
        this.backHistory = new ArrayDeque<>();
        this.forwardHistory = new ArrayDeque<>();
        this.bookmarks = new HashSet<>();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void setMainView(String currurl) {
        //AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        theActivity.setContentView(R.layout.activity_main);
        isBrowser = true;
        Log.d("SET", "Context View set");
        this.navbar = theActivity.findViewById(R.id.urlInput);

        setToggle();
        setDrawerActions();
        setToolbar();
        setRefreshOnSwipe();

        if (currurl == null) {
            setMadWebView(homepage);
        } else {
            setMadWebView(currurl);
        }

        Button goBtn = theActivity.findViewById(R.id.goButton);
        goBtn.setOnClickListener((View v) -> {
            loadInput();
        });

        navbar.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    loadInput();
                    return true;
                }
                return false;
            }
        });

    }

    public void loadInput() {
        String urlTxt = navbar.getText().toString();
        parseAndLoad(urlTxt);
        madWebView.requestFocus();
        closeKeyboard();
    }

    public void closeKeyboard() {
        // this will give us the view which is currently focus in this layout
        View view = theActivity.getCurrentFocus();

        // if nothing is currently focus then this will protect the app from crash
        if (view != null) {
            // now assign the system service to InputMethodManager
            InputMethodManager manager = (InputMethodManager)theActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
            manager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public void parseAndLoad(@NotNull String url) {
        String parsedUrl = "";
        if (!url.startsWith("http")) {
            parsedUrl = "http://" + url;
            load(parsedUrl);
        } else {
            load(url);
        }
    }

    public void load(String url) {
        madWebView.loadUrl(url);
        Log.d("URL", "Loaded pg: " + getCurrentUrl());
    }

    public void addToHistory() {
        if (!webHasNoPage()) {
            if (hasBackHist() && (backHistory.peek().getUrl().equals(getCurrentUrl())
                    || backHistory.peek().getTitle().equals(madWebView.getTitle()))) {
                Log.d("URL", "already on top of stack");
            } else {
                backHistory.push(new HistoryItem(madWebView.getTitle(), madWebView.getUrl()));
                navbar.setText(getCurrentUrl());
            }
        }
        navbar.setText(getCurrentUrl());
    }

    public boolean webHasNoPage() {
        return madWebView.getTitle().isEmpty() || madWebView.getTitle() == null
                || madWebView.getUrl().isEmpty() || madWebView.getUrl() == null;
    }

    public void goBack() {
        if (hasBackHist() && finishedLoading()) {
            goBackInHistory();
            drawer.closeDrawers();
            Log.d("URL", "went back"); //remove later
        } else if (!finishedLoading()) {
            displayToast("Wait for page to load");
        } else {
            displayToast("Can't go back");
        }
    }

    public void goForward() {
        if (hasForwardHist() && finishedLoading()) {
            displayToast(forwardHistory.peek().getTitle());
            goForwardInHistory();
            drawer.closeDrawers();
            Log.d("URL", "went forward"); //remove later
        } else if (!finishedLoading()) {
            displayToast("Wait for page to load");
        } else {
            displayToast("Can't go forward");
        }
    }

    public boolean finishedLoading() {
        return getCurrentUrl().startsWith("http");
    }

    public boolean hasBackHist() {
        return !backHistory.isEmpty();
    }

    public boolean hasForwardHist() {
        return !forwardHistory.isEmpty();
    }

    public void goBackInHistory() {
        forwardHistory.push(backHistory.pop());
        HistoryItem back = backHistory.pop();
        load(back.getUrl());
        displayToast(back.getTitle());
        Log.d("HIST", "Went back: " + backHistory.size() + " | " + forwardHistory.size());
    }

    public void goForwardInHistory() {
        //backHistory.push(new HistoryItem(madWebView.getTitle(), madWebView.getUrl()));
        HistoryItem forwd = forwardHistory.pop();
        load(forwd.getUrl());
        displayToast(forwd.getTitle());
        Log.d("HIST", "Went forward: " + backHistory.size() + " | " + forwardHistory.size());
    }

    //getters & setters

    public WebView getMadWebView() {
        return madWebView;
    }

    public void setMadWebView(String url) {
        navbar.setText(url);
        this.client = new MadWebViewClient(this);
        this.madWebView = theActivity.findViewById(R.id.madWeb);
        madWebView.setWebViewClient(client);
        WebSettings webSettings = madWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        //webSettings.setJavaScriptEnabled(true);
        parseAndLoad(url); //Set homepage!!! need to create method to set and save etc.
    }

    public String getCurrentUrl() {
        return madWebView.getUrl();
    }

    public void setToggle() {
        this.drawer = theActivity.findViewById(R.id.drawerLayout);
        this.toggle = new ActionBarDrawerToggle(theActivity, drawer, R.string.open, R.string.close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
    }

    public void displayToast(String msg) {
        Toast.makeText(theActivity.getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void setDrawerActions() {
        NavigationView menu = (NavigationView)theActivity.findViewById(R.id.navView);
        menu.setNavigationItemSelectedListener(menuItem -> {
            switch (menuItem.getItemId()) {
                case R.id.homeItem:
                    parseAndLoad(homepage);
                    drawer.closeDrawers();
                    return true;
                case R.id.backItem:
                    goBack();
                    return true;
                case R.id.forwardItem:
                    goForward();
                    return true;
                case R.id.historyItem:
                    setHistoryView();
                    return true;
                case R.id.bookmarksItem:
                    setBookmarksView();
                    return true;
                case R.id.settingsItem:
                    setSettingsView();
                    return true;
                case R.id.refreshItem:
                    displayToast("Refresh");
                    madWebView.reload();
                    drawer.closeDrawers();
                    return true;
                case R.id.closeItem:
                    drawer.closeDrawers();
                    return true;
                default:
                    return true;
            }
        });
    }

    public ActionBarDrawerToggle getToggle() {
        return toggle;
    }

    public static String getHomepage() {
        return homepage;
    }

    public static void setHomepage(String homepage) {
        Controller.homepage = homepage;
    }

    //might need to parameterize this for other toolbars?
    public void setToolbar() {
        Toolbar myToolbar = (Toolbar)theActivity.findViewById(R.id.toolbar);
        theActivity.setSupportActionBar(myToolbar);
        theActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public void setRefreshOnSwipe() {
        SwipeRefreshLayout refresh = theActivity.findViewById(R.id.web);
        refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                madWebView.reload();
                refresh.setRefreshing(false);
            }
        });
    }

    public boolean getIsBrowser() { return isBrowser; }

    public List<HistoryItem> getBackHistoryAsList() {
        List<HistoryItem> backList = new ArrayList<>();
        backList.addAll(backHistory);
        return backList;
    }

    public void setBackHistoryAsDeque(List<HistoryItem> backList) {
        ArrayDeque<HistoryItem> history = new ArrayDeque<>();

        for (int i = backList.size() - 1; i >= 0; i--) {
            history.push(backList.get(i));
        }

        this.backHistory = history;
    }

    public List<HistoryItem> getForwardHistoryAsList() {
        List<HistoryItem> forwdList = new ArrayList<>();
        forwdList.addAll(forwardHistory);
        return forwdList;
    }

    public void setForwardHistoryAsDeque(List<HistoryItem> forwdList) {
        ArrayDeque<HistoryItem> history = new ArrayDeque<>();

        for (int i = forwdList.size() - 1; i >= 0; i--) {
            history.push(forwdList.get(i));
        }

        this.forwardHistory = history;
    }

    public List<Bookmark> getBookmarksAsList() {
        List<Bookmark> bmList = new ArrayList<>();
        bmList.addAll(bookmarks);
        return bmList;
    }

    public void setBookmarksAsSet(List<Bookmark> bmList) {
        HashSet<Bookmark> bmSet = new HashSet<>();
        bmSet.addAll(bmList);
        this.bookmarks = bmSet;
    }

    //History View
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void setHistoryView() {
        theActivity.setContentView(R.layout.history_view);
        isBrowser = false;
        setHistoryListView();
        setClearHistory();
        setHistoryToolbar();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void setClearHistory() {
        ConstraintLayout clear = theActivity.findViewById(R.id.clearHistory);
        clear.setOnClickListener((View v) -> {
            backHistory.clear();
            forwardHistory.clear();
            setHistoryView();
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void setHistoryListView() {
        ListView histView = theActivity.findViewById(R.id.historyList);
        //HistoryItem current = new HistoryItem(madWebView.getTitle(), madWebView.getUrl());
        List<HistoryItem> history = new ArrayList<>();
        //history.add(current); //current page
        history.addAll(backHistory);

        historyAdapter = new HistoryCustomAdapter(theActivity, history);

        histView.setAdapter(historyAdapter);
        histView.setOnItemClickListener((parent, view, position, id) -> {
            //forwardHistory.push(current);
            for (int i = 0; i <= position; i++) {
                if (i == position) {
                    setMainView(backHistory.pop().getUrl());
                } else {
                    forwardHistory.push(backHistory.pop());
                }
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void setHistoryToolbar() {
        Toolbar histToolbar = (Toolbar)theActivity.findViewById(R.id.historyToolbar);
        theActivity.setSupportActionBar(histToolbar);
        theActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        histToolbar.setNavigationOnClickListener(v -> setMainView(getCurrentUrl()));
    }

    //Bookmarks View
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void setBookmarksView() {
        theActivity.setContentView(R.layout.bookmarks_view);
        isBrowser = false;
        setBookmarksListView();
        setAddRemoveBookmark();
        setClearBookmarks();
        setBookmarksToolbar();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void setClearBookmarks() {
        ConstraintLayout clear = theActivity.findViewById(R.id.clearBookmarks);
        clear.setOnClickListener((View v) -> {
            bookmarks.clear();
            setBookmarksView();
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void setAddRemoveBookmark() {
        ConstraintLayout addRemove = theActivity.findViewById(R.id.addBookmark);
        ImageView icon = theActivity.findViewById(R.id.bookmarkIcon);
        TextView text = theActivity.findViewById(R.id.addBookmarkTxt);

        //set the bookmark button display
        if (currUrlInBookmarks()) {
            icon.setImageDrawable(theActivity.getDrawable(R.drawable.baseline_bookmark_24));
            text.setText(theActivity.getText(R.string.remove_bookmark_text));
        } else {
            icon.setImageDrawable(theActivity.getDrawable(R.drawable.baseline_bookmark_border_24));
            text.setText(theActivity.getText(R.string.add_bookmark_text));
        }

        //add listener to button
        addRemove.setOnClickListener((View v) -> {
            if (currUrlInBookmarks()) {
                //remove
                bookmarks.remove(new Bookmark(madWebView.getTitle(), getCurrentUrl()));
                setBookmarksView();
            } else {
                bookmarks.add(new Bookmark(madWebView.getTitle(), getCurrentUrl()));
                setBookmarksView();
            }
        });
    }

    public boolean currUrlInBookmarks () {
        Bookmark temp = new Bookmark(madWebView.getTitle(), getCurrentUrl());
        return bookmarks.contains(temp);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void setBookmarksListView() {
        ListView bookView = theActivity.findViewById(R.id.bookmarkList);
        List<Bookmark> currBookmarks = new ArrayList<>();
        currBookmarks.addAll(bookmarks);

        bookmarksAdapter = new BookmarksCustomAdapter(theActivity, currBookmarks);

        bookView.setAdapter(bookmarksAdapter);
        bookView.setOnItemClickListener((parent, view, position, id) -> {
            setMainView(currBookmarks.get(position).getUrl());
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void setBookmarksToolbar() {
        Toolbar bookToolbar = (Toolbar)theActivity.findViewById(R.id.bookmarksToolbar);
        theActivity.setSupportActionBar(bookToolbar);
        theActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        bookToolbar.setNavigationOnClickListener(v -> setMainView(getCurrentUrl()));
    }

    //Settings View
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void setSettingsView() {
        theActivity.setContentView(R.layout.settings_view);
        isBrowser = false;
        setHomepageUpdate();
        setHompageDisplay();
        setSettingsToolbar();
    }

    public void setHomepageUpdate() {
        ImageButton updateBtn = (ImageButton)theActivity.findViewById(R.id.homepageButton);
        EditText urlInput = (EditText)theActivity.findViewById(R.id.homepageTxtInput);
        urlInput.setText(getCurrentUrl());

        updateBtn.setOnClickListener((View v) -> {
            if (urlInput.getText().toString() != null && !urlInput.getText().toString().isEmpty()
                    && !urlInput.getText().toString().equals("")) {
                Controller.setHomepage(urlInput.getText().toString());
                urlInput.setText("");
                setHompageDisplay();
            }
            closeKeyboard();
        });
    }

    public void setHompageDisplay() {
        TextView hpText = theActivity.findViewById(R.id.displayHomepageTxt);
        hpText.setText(homepage);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void setSettingsToolbar() {
        Toolbar settingsToolbar = (Toolbar)theActivity.findViewById(R.id.settingsToolbar);
        theActivity.setSupportActionBar(settingsToolbar);
        theActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        settingsToolbar.setNavigationOnClickListener(v -> setMainView(getCurrentUrl()));
    }
}
