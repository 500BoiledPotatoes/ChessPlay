package com.example.chessplay;

import android.content.Context;
import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class LinearSpacingItemDecoration extends RecyclerView.ItemDecoration {

    private int spacing;

    public LinearSpacingItemDecoration(Context context, int spacings) {
        spacing = spacings;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        outRect.top = spacing;
        outRect.left = spacing;
        outRect.right = spacing;
//        outRect.bottom = spacing;
    }
}