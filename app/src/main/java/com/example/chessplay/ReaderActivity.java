package com.example.chessplay;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.example.chessplay.book.Book;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;


public class ReaderActivity extends Activity {

    public static final String READEREXITEDNORMALLY = "readerexitednormally";
    private static final String FULLSCREEN = "fullscreen";

    private Book book;

    private WebView webView;

    public static final String FILENAME = "filename";
    public static final String SCREEN_PAGING = "screenpaging";
    public static final String DRAG_SCROLL= "dragscroll";



    private final Object timerSync = new Object();
    private Timer timer;

    private TimerTask nowakeTask = null;
    private TimerTask scrollTask = null;

    private volatile int scrollDir;

    private final Handler handler = new Handler();

    private CheckBox fullscreenBox;

    private ProgressBar progressBar;

    private Point mScreenDim;

    private Throwable exception;

    private boolean hasLightSensor = false;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reader);
        final Intent intent = getIntent();

        ActionBar ab = getActionBar();
        if (ab!=null) ab.hide();
        Display display = getWindowManager().getDefaultDisplay();
        mScreenDim = new Point();
        display.getSize(mScreenDim);

        SensorManager sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        Sensor lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        if (lightSensor != null) {
            hasLightSensor = true;
        }


        webView = findViewById(R.id.page_view);

        webView.getSettings().setDefaultFontSize(18);
        webView.getSettings().setDefaultFixedFontSize(18);

        webView.setNetworkAvailable(false);

        WebSettings webSettings=webView.getSettings();
        webSettings.setAllowFileAccess(true);
        webSettings.setAllowContentAccess(true);
        final boolean drag_scroll = intent.getBooleanExtra(DRAG_SCROLL,true);

        if (intent.getBooleanExtra(SCREEN_PAGING,true)) webView.setOnTouchListener(new View.OnTouchListener() {
            float x,y;
            long time;
            final long TIMEALLOWED = 300;
            final int MINSWIPE = 150;
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                float diffx = 0;
                float diffy = 0;

                switch (motionEvent.getAction()) {

                    case MotionEvent.ACTION_UP:

                        if (drag_scroll) cancelScrollTask();
                        if (System.currentTimeMillis() - time >TIMEALLOWED) return false;

                        diffx = motionEvent.getX() - x;
                        diffy = motionEvent.getY() - y;
                        float absdiffx = Math.abs(diffx);
                        float absdiffy = Math.abs(diffy);


                        if ((absdiffx>absdiffy && diffx>MINSWIPE) || (absdiffy>absdiffx && diffy>MINSWIPE)) {
                            prevPage();
                        } else if ((absdiffx>absdiffy && diffx<-MINSWIPE) || (absdiffy>absdiffx && diffy<-MINSWIPE)) {
                            nextPage();
                        } else {
                            return false;
                        }


                    case MotionEvent.ACTION_DOWN:
                        if (drag_scroll) cancelScrollTask();
                        x = motionEvent.getX();
                        y = motionEvent.getY();
                        time = System.currentTimeMillis();
                        return false;

                        case MotionEvent.ACTION_MOVE:

                        if (drag_scroll) {
                            diffy = motionEvent.getY() - y;

                            if (Math.abs(diffy) > 30) {
                                if (System.currentTimeMillis() - time > TIMEALLOWED * 1.5) {
                                    scrollDir = (int) ((-diffy / webView.getHeight()) * webView.getSettings().getDefaultFontSize() * 5);
                                    startScrollTask();
                                    webView.clearMatches();
                                }
                            } else {
                                cancelScrollTask();
                            }
                        }
                        return true;
                }
                return true;
            }



        });

        webView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                handleLink(url);
                return true;
            }
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri uri = request.getUrl();
                if (uri.getScheme()!=null && uri.getScheme().equals("file")) {
                    handleLink(uri.toString());
                    return true;
                }
                return false;
            }


            public void onPageFinished(WebView view, String url) {
                try {
                    restoreScrollOffsetDelayed(100);
                } catch (Throwable t) {
                }
            }

        });


        progressBar = findViewById(R.id.progressBar);

        findViewById(R.id.prev_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                prevPage();
            }
        });

        findViewById(R.id.next_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nextPage();
            }
        });
        String filename = intent.getStringExtra(FILENAME);
        if (filename!=null) {
            if (getSharedPreferences(BookListActivity.prefname, Context.MODE_PRIVATE).edit().putBoolean(READEREXITEDNORMALLY, false).commit()) {
                loadFile(new File(filename));
            }
        }

    }

    @Override
    public void onBackPressed() {
        finish();
        Intent main = new Intent(this, BookListActivity.class);
        main.setAction(BookListActivity.ACTION_SHOW_LAST_STATUS);
        startActivity(main);
    }

    private void setFullscreenMode() {
        if (book!=null && book.hasDataDir()) {
            setFullscreen(book.getFlag(FULLSCREEN, true));
        }
    }

    private void setFullscreen(boolean full) {
        if (book!=null && book.hasDataDir()) book.setFlag(FULLSCREEN, full);

        fullscreenBox.setChecked(full);
    }



    private void startScrollTask() {
        synchronized (timerSync) {
            if (scrollTask == null) {
                scrollTask = new TimerTask() {
                    @Override
                    public void run() {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                webView.scrollBy(0, scrollDir);
                            }
                        });
                    }
                };
                try {
                    if (timer!=null) timer.schedule(scrollTask, 0, 100);
                } catch(IllegalStateException e) {
                }
            }
        }
    }

    private void cancelScrollTask() {
        if (scrollTask!=null) {
            scrollTask.cancel();
            scrollTask = null;
        }
    }

    private boolean isPagingDown;
    private boolean isPagingUp;

    private void prevPage() {
        isPagingDown = false;
        if(webView.canScrollVertically(-1)) {
            webView.pageUp(false);
        } else {
            isPagingUp = true;
            showUri(book.getPreviousSection());
        }

    }

    private void nextPage() {
        isPagingUp = false;
        if(webView.canScrollVertically(1)) {
            webView.pageDown(false);
        } else {
            isPagingDown = true;
            if (book!=null) showUri(book.getNextSection());


        }
    }

    private void saveScrollOffset() {
        webView.computeScroll();
        saveScrollOffset(webView.getScrollY());
    }

    private void saveScrollOffset(int offset) {
        if (book==null) return;
        book.setSectionOffset(offset);
    }

    private void restoreScrollOffsetDelayed(int delay) {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                restoreScrollOffset();
            }
        }, delay);
    }

    private void restoreScrollOffset() {
        if (book==null) return;
        int spos = book.getSectionOffset();
        webView.computeScroll();
        if (spos>=0) {
            webView.scrollTo(0, spos);
        } else if (isPagingUp){
            webView.pageDown(true);
        } else if (isPagingDown){
            webView.pageUp(true);
        }
        isPagingUp = false;
        isPagingDown = false;
    }

    private void loadFile(File file) {

        webView.loadData("Loading " + file.getPath(),"text/plain", "utf-8");

        new LoaderTask(this, file).execute();

    }


    private static class LoaderTask extends  AsyncTask<Void,Integer,Book>  {

        private final File file;
        private final WeakReference<ReaderActivity> ractref;

        LoaderTask(ReaderActivity ract, File file) {
            this.file = file;
            this.ractref = new WeakReference<>(ract);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            ReaderActivity ract = ractref.get();
            if (ract!=null) {
                ract.progressBar.setProgress(0);
                ract.progressBar.setVisibility(View.VISIBLE);

            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            ReaderActivity ract = ractref.get();
            if (ract!=null) {
                ract.progressBar.setProgress(values[0]);
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            ReaderActivity ract = ractref.get();
            if (ract!=null) {
                ract.progressBar.setVisibility(View.GONE);
            }
        }

        @Override
        protected Book doInBackground(Void... voids) {
            ReaderActivity ract = ractref.get();
            if (ract==null) return null;
            try {
                ract.book = Book.getBookHandler(ract, file.getPath());
                if (ract.book!=null) {
                    ract.book.load(file);
                    return ract.book;
                }

            } catch (Throwable e) {
                ract.exception = e;
            }
            return null;
        }

        @SuppressLint("ClickableViewAccessibility")
        @Override
        protected void onPostExecute(Book book) {

            ReaderActivity ract = ractref.get();
            if (ract==null) return;

            String badtext = ract.getString(R.string.book_bug);
            try {
                ract.progressBar.setVisibility(View.GONE);

                if (book==null && ract.exception!=null) {
                    ract.webView.setOnTouchListener(null);
                    ract.webView.setWebViewClient(null);
                    ract.webView.loadData(badtext + ract.exception.getLocalizedMessage(),"text/plain", "utf-8");
                    throw ract.exception;
                }
                if (book !=null && ract.book != null && ract.book.hasDataDir()) {
                    int fontsize = ract.book.getFontsize();
                    if (fontsize != -1) {
                        ract.setFontSize(fontsize);
                    }
                    Uri uri = ract.book.getCurrentSection();
                    if (uri != null) {
                        ract.showUri(uri);
                    } else {
                        Toast.makeText(ract, badtext + " (no sections)", Toast.LENGTH_LONG).show();
                    }
                    ract.setFullscreenMode();
                }
            } catch (Throwable e) {
//                Toast.makeText(ract, e.getMessage(), Toast.LENGTH_LONG).show();
//                Log.d("dddd", e.getMessage());
            }

        }
    }


    private void showUri(Uri uri) {
        if (uri !=null) {
            webView.loadUrl(uri.toString());
        }
    }

    private void handleLink(String clickedLink) {
        if (clickedLink!=null) {
            showUri(book.handleClickedLink(clickedLink));
        }

    }

    private void setFontSize(int size) {
        book.setFontsize(size);
        webView.getSettings().setDefaultFontSize(size);
        webView.getSettings().setDefaultFixedFontSize(size);
    }

    @Override
    protected void onResume() {
        super.onResume();

        synchronized (timerSync) {
            if (timer != null) {
                timer.cancel();
            }
            timer = new Timer();
        }
    }

    @Override
    protected void onPause() {
        synchronized (timerSync) {
            if (timer != null) {
                timer.cancel();
                timer.purge();
                timer = null;
            }
        }

        if (exception==null) {
            try {
                saveScrollOffset();
            } catch (Throwable t) {
            }
            getSharedPreferences(BookListActivity.prefname, Context.MODE_PRIVATE).edit().putBoolean(READEREXITEDNORMALLY, true).apply();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (timer!=null) {
            timer.cancel();
            timer.purge();
            timer = null;
        }
        super.onDestroy();
    }


}
