package com.example.chessplay;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.drawable.Icon;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chessplay.book.Book;
import com.example.chessplay.book.BookMetadata;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class BookListActivity extends AppCompatActivity {

    private static final String SORTORDER_KEY = "sortorder";
    private static final String LASTSHOW_STATUS_KEY = "LastshowStatus";
    private static final String STARTWITH_KEY = "startwith";
    private static final String ENABLE_SCREEN_PAGE_KEY = "screenpaging";
    private static final String ENABLE_DRAG_SCROLL_KEY = "dragscroll";

    private static final int STARTLASTREAD = 1;
    private static final int STARTOPEN = 2;
    private static final int STARTALL = 3;

    private SharedPreferences data;

    private BookAdapter bookAdapter;

    private BookListAdderHandler viewAdder;
    private TextView tv;

    private BookDb db;
    private int recentread;
    private boolean showingSearch;

    private int showStatus = BookDb.STATUS_ANY;
    private static final String ACTION_SHOW_OPEN = "com.example.chessplay.SHOW_OPEN_BOOKS";
    private static final String ACTION_SHOW_UNREAD = "com.example.chessplay.SHOW_UNREAD_BOOKS";
    public static final String ACTION_SHOW_LAST_STATUS = "com.example.chessplay.SHOW_LAST_STATUS";

    public final static String prefname = "booklist";

    private boolean openLastread = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_list);

        tv = findViewById(R.id.progress_text);
        checkStorageAccess(false);

        data = getSharedPreferences(prefname, Context.MODE_PRIVATE);

        viewAdder = new BookListAdderHandler(this);

        if (!data.contains(SORTORDER_KEY)) {
            setSortOrder(SortOrder.Default);
        }

        db = ChessPlayApp.getDB(this);

        RecyclerView listHolder = findViewById(R.id.book_list_holder);
        listHolder.setLayoutManager(new LinearLayoutManager(this));
        listHolder.setItemAnimator(new DefaultItemAnimator());

        bookAdapter = new BookAdapter(this, db, new ArrayList<Integer>());
        bookAdapter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                readBook((int)view.getTag());
            }
        });
        bookAdapter.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                longClickBook(view);
                return false;
            }
        });

        listHolder.setAdapter(bookAdapter);

        processIntent(getIntent());

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        processIntent(intent);
    }

    private void processIntent(Intent intent) {

        recentread = db.getMostRecentlyRead();

        showStatus = BookDb.STATUS_ANY;

        openLastread = false;

        boolean hadSpecialOpen = false;
        if (intent != null) {
            if (intent.getAction() != null) {
                switch (intent.getAction()) {
                    case ACTION_SHOW_OPEN:
                        showStatus = BookDb.STATUS_STARTED;
                        hadSpecialOpen = true;
                        break;
                    case ACTION_SHOW_UNREAD:
                        showStatus = BookDb.STATUS_NONE;
                        hadSpecialOpen = true;
                        break;
                    case ACTION_SHOW_LAST_STATUS:
                        showStatus = data.getInt(LASTSHOW_STATUS_KEY, BookDb.STATUS_ANY);
                        hadSpecialOpen = true;
                        break;
                }

            }
        }
        if (!hadSpecialOpen){

            switch (data.getInt(STARTWITH_KEY, STARTLASTREAD)) {
                case STARTLASTREAD:
                    if (recentread!=-1 && data.getBoolean(ReaderActivity.READEREXITEDNORMALLY, true)) openLastread = true;
                    break;
                case STARTOPEN:
                    showStatus = BookDb.STATUS_STARTED; break;
                case STARTALL:
                    showStatus = BookDb.STATUS_ANY;
            }
        }


    }

    @Override
    protected void onResume() {
        super.onResume();
        if (openLastread) {
            openLastread = false;
            viewAdder.postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        BookDb.BookRecord book = db.getBookRecord(recentread);
                        getReader(book, true);
                        //finish();
                    } catch (Exception e) {
                        data.edit().putInt(STARTWITH_KEY, STARTALL).apply();
                    }
                }
            }, 200);
        } else {
            populateBooks(showStatus);
        }
    }

    @Override
    public void onBackPressed() {
        if (showingSearch || showStatus!=BookDb.STATUS_ANY) {
            setTitle(R.string.app_name);
            populateBooks();
            showingSearch = false;
        } else {
            super.onBackPressed();
        }
    }

    private void setSortOrder(SortOrder sortOrder) {
        data.edit().putString(SORTORDER_KEY,sortOrder.name()).apply();
    }

    @NonNull
    private SortOrder getSortOrder() {

        try {
            return SortOrder.valueOf(data.getString(SORTORDER_KEY, SortOrder.Default.name()));
        } catch (IllegalArgumentException e) {
            Log.e("Booklist", e.getMessage(), e);
            return SortOrder.Default;
        }
    }

    private void populateBooks() {
        populateBooks(BookDb.STATUS_ANY);
    }

    private void populateBooks(int status) {
        //Log.d("BOOKLIST", "populateBooks " + status);
        showStatus = status;
        data.edit().putInt(LASTSHOW_STATUS_KEY, showStatus).apply();

        boolean showRecent = false;
        int title = R.string.app_name;
        BookListActivity.this.setTitle(title);

        SortOrder sortorder = getSortOrder();
        final List<Integer> books = db.getBookIds(sortorder, status);
        populateBooks(books,  showRecent);

        invalidateOptionsMenu();
    }



    private void populateBooks(final List<Integer> books, boolean showRecent) {

        if (showRecent) {
            recentread = db.getMostRecentlyRead();
            if (recentread >= 0) {
                books.remove((Integer) recentread);
                books.add(0, (Integer)recentread);
            }
        }

        bookAdapter.setBooks(books);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        System.out.println("选项菜单布置完成");
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options, menu);
        return true;
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.menu_add).setVisible(!showingSearch);
        return true;
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int status = BookDb.STATUS_ANY;
        boolean pop = false;
        switch (item.getItemId()) {
            case R.id.menu_add:
                findFile();
                break;
            default:

                return super.onOptionsItemSelected(item);
        }


        final int statusf = status;
        if (pop) {
            viewAdder.postDelayed(new Runnable() {
                @Override
                public void run() {
                   populateBooks(statusf);
                    invalidateOptionsMenu();
                }
            }, 120);
        }

        invalidateOptionsMenu();
        return true;
    }


    public static String maxlen(String text, int maxlen) {
        if (text!=null && text.length() > maxlen) {
            int minus = text.length()>3?3:0;

            return text.substring(0, maxlen-minus) + "...";
        }
        return text;
    }

    private void readBook(final int bookid) {

        final BookDb.BookRecord book = db.getBookRecord(bookid);

        if (book!=null && book.filename!=null) {
            //data.edit().putString(LASTREAD_KEY, BOOK_PREFIX + book.id).apply();

            final long now = System.currentTimeMillis();
            db.updateLastRead(bookid, now);
            recentread = bookid;

            viewAdder.postDelayed(new Runnable() {
                @Override
                public void run() {

                    getReader(book,true);

                }
            }, 300);

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N_MR1) {

                try {

                    ShortcutManager shortcutManager = (ShortcutManager) getSystemService(Context.SHORTCUT_SERVICE);
                    if (shortcutManager!=null) {
                        Intent readBook = getReader(book,false);


                        ShortcutInfo readShortcut = new ShortcutInfo.Builder(this, "id1")
                                .setShortLabel(getString(R.string.shortcut_latest))
                                .setLongLabel(getString(R.string.shortcut_latest_title, maxlen(book.title, 24)))
                                .setIcon(Icon.createWithResource(BookListActivity.this, R.mipmap.ic_launcher_round))
                                .setIntent(readBook)
                                .build();



                        shortcutManager.setDynamicShortcuts(Collections.singletonList(readShortcut));
                    }
                } catch(Exception e) {
                    Log.e("Booky", e.getMessage(), e);
                }
            }


        }
    }

    private Intent getReader(BookDb.BookRecord book, boolean start) {
        Intent readBook = new Intent(BookListActivity.this, ReaderActivity.class);
        readBook.putExtra(ReaderActivity.FILENAME, book.filename);
        readBook.putExtra(ReaderActivity.SCREEN_PAGING, data.getBoolean(ENABLE_SCREEN_PAGE_KEY, true));
        readBook.putExtra(ReaderActivity.DRAG_SCROLL, data.getBoolean(ENABLE_DRAG_SCROLL_KEY, true));
        readBook.setAction(Intent.ACTION_VIEW);
        if (start) {
            bookAdapter.notifyItemIdChanged(book.id);
            startActivity(readBook);
        }
        return readBook;
    }


    private void removeBook(int bookid, boolean delete) {
        BookDb.BookRecord book = db.getBookRecord(bookid);
        if (book==null) {
            Toast.makeText(this, "Bug? The book doesn't seem to be in the database",Toast.LENGTH_LONG).show();
            return;
        }
        if (book.filename!=null && book.filename.length()>0) {
            Book.remove(this, new File(book.filename));
        }
        if (delete) {
            db.removeBook(bookid);
            if (bookAdapter!=null) bookAdapter.notifyItemIdRemoved(bookid);
        }
        recentread = db.getMostRecentlyRead();
    }

    private boolean addBook(String filename) {
        return addBook(filename, true, System.currentTimeMillis());
    }

    private boolean addBook(String filename, boolean showToastWarnings, long dateadded) {

        try {
            if (db.containsBook(filename)) {

                if (showToastWarnings) {
                    Toast.makeText(this, getString(R.string.already_added, new File(filename).getName()), Toast.LENGTH_SHORT).show();
                }
                return false;
            }

            BookMetadata metadata = Book.getBookMetaData(this, filename);

            if (metadata!=null) {

                return db.addBook(filename, metadata.getTitle(), metadata.getAuthor(), dateadded) > -1;

            } else if (showToastWarnings) {
                Toast.makeText(this,getString(R.string.coulndt_add_book, new File(filename).getName()),Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Log.e("BookList", "File: " + filename  + ", " + e.getMessage(), e);
        }
        return false;
    }

    private void findFile() {

        FsTools fsTools = new FsTools(this);

        if (checkStorageAccess(false)) {
            fsTools.selectExternalLocation(new FsTools.SelectionMadeListener() {
                @Override
                public void selected(File selection) {
                    addBook(selection.getPath());
                    populateBooks();

                }
            }, getString(R.string.find_book), false, Book.getFileExtensionRX());
        }
    }

    private void showProgress(int added) {

        if (tv.getVisibility() != View.VISIBLE) {
            tv.setVisibility(View.VISIBLE);
            tv.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return true;
                }
            });
        }
        if (added>0) {
            tv.setText(getString(R.string.added_numbooks, added));
        } else {
            tv.setText(R.string.loading);
        }
    }

    private void hideProgress() {
        tv.setVisibility(View.GONE);
    }



    private void longClickBook(final View view) {
        final int bookid = (int)view.getTag();
        PopupMenu menu = new PopupMenu(this, view);
        menu.getMenu().add(R.string.open_book).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                readBook(bookid);
                return false;
            }
        });
        menu.getMenu().add(R.string.remove_book).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                removeBook(bookid, true);
                return false;
            }
        });
        menu.show();
    }

    private boolean checkStorageAccess(boolean yay) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    yay? REQUEST_READ_EXTERNAL_STORAGE : REQUEST_READ_EXTERNAL_STORAGE_NOYAY);
            return false;
        }
        return true;
    }

    private static final int REQUEST_READ_EXTERNAL_STORAGE_NOYAY = 4333;
    private static final int REQUEST_READ_EXTERNAL_STORAGE = 4334;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean yay = true;
        switch (requestCode) {
            case REQUEST_READ_EXTERNAL_STORAGE_NOYAY:
                yay = false;
            case REQUEST_READ_EXTERNAL_STORAGE:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    if (yay) Toast.makeText(this, "Yay", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "Boo", Toast.LENGTH_LONG).show();
                }

        }
    }


    private static class BookListAdderHandler extends Handler {

        private static final int SHOW_PROGRESS = 1002;
        private static final int HIDE_PROGRESS = 1003;
        private final WeakReference<BookListActivity> weakReference;

        BookListAdderHandler(BookListActivity blInstance) {
            weakReference = new WeakReference<>(blInstance);
        }

        @Override
        public void handleMessage(Message msg) {
            BookListActivity blInstance = weakReference.get();
            if (blInstance != null) {
                switch (msg.arg1) {

                    case SHOW_PROGRESS:
                        blInstance.showProgress(msg.arg2);
                        break;
                    case HIDE_PROGRESS:
                        blInstance.hideProgress();
                        break;
                }
            }
        }
    }

}
