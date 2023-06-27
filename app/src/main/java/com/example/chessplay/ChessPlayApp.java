package com.example.chessplay;

import android.app.Application;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public class ChessPlayApp extends Application {
    private static Context appContext;
    private static Toast toast;

    public ChessPlayApp() {
        super();
        appContext = this;
    }

    public static Context getContext() {
        return appContext;
    }

    public static void toast(CharSequence text, int duration) {
        if (toast != null) {
            toast.cancel();
            toast = null;
        }
        toast = Toast.makeText(appContext, text, duration);
        toast.show();
    }
    private BookDb db;

    @Override
    public void onCreate() {
        super.onCreate();

        db = new BookDb(this);
    }

    public static BookDb getDB(Context context) {
        return ((ChessPlayApp)context.getApplicationContext()).db;
    }


    @Override
    public void onTerminate() {
        if (db!=null) db.close();

        super.onTerminate();
    }
}
