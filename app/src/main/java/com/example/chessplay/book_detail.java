package com.example.chessplay;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class book_detail extends AppCompatActivity {


    private ImageView mImageView;
    private ImageView back;
    private TextView name;
    private TextView author;
    private TextView content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
        setContentView(R.layout.activity_book_detail);

        initView();
        back = (ImageView) findViewById(R.id.back);
        back.setOnClickListener(view -> {
            onBackPressed();
        });

    }

    private void initView() {
        mImageView = (ImageView) findViewById(R.id.fruit_image);
        name = findViewById(R.id.name);
        author = findViewById(R.id.author);
        content = findViewById(R.id.content);
        Intent intent = getIntent();
        RecBook book = (RecBook) intent.getSerializableExtra("book");
        mImageView.setImageResource(book.getImageId());
        ImageViewUtil.matchAll(this, mImageView);
        name.setText(book.getTitle());
        author.setText(book.getAuthor());
        content.setText(book.getContent());


    }
}