package com.example.chessplay;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class ImageViewUtil {
    public static void matchAll(Context context, ImageView imageView) {
        int width, height;
        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(metrics);
        int sWidth = metrics.widthPixels;
        int sHeight = metrics.heightPixels;

        Drawable drawable = imageView.getDrawable();
        int dWidth = drawable.getIntrinsicWidth();
        int dHeight = drawable.getIntrinsicHeight();


        float sScale = (float) sWidth / sHeight;

        float dScale = (float) dWidth / dHeight;

        float scale = 1.0f;
        if (sScale > dScale) {
            scale = (float) dHeight / sHeight;
            height = sHeight;
            width = (int) (dWidth * scale);
        } else if (sScale < dScale) {
            scale = (float) dWidth / sWidth;
            width = sWidth;
            height = (int) (dHeight / scale);
        } else {
            width = sWidth;
            height = sHeight;
        }
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(width, height);
        imageView.setLayoutParams(params);
    }
}