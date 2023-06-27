package com.example.chessplay;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;


public class GameActivity extends AndroidApplication {
    boolean mainIsCreated = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);


        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();

        initialize(new ChessGame(true, GameActivity.this), config);


    }

    @Override
    public void onBackPressed() {
        if (!mainIsCreated) {
            mainIsCreated = true;
            startActivity(new Intent(GameActivity.this, MainActivity.class));
        }
        finish();
    }

    public void backToMenu() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }
}