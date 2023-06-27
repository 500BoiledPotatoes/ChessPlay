package com.example.chessplay;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.chessplay.circleprogress.WaveProgress;
import com.github.abel533.echarts.Legend;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.bmob.v3.BmobUser;

public class PlayerActivity extends AppCompatActivity {
    private ImageView head;
    private TextView username;
    private TextView email;
    private EchartView lineChart;
    private EchartView pieChart;
    private WaveProgress mWaveProgress;
    private User user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        Intent intent = getIntent();
        user = (User) intent.getSerializableExtra("user");
        head = findViewById(R.id.h_head);
        username = findViewById(R.id.user_name);
        email = findViewById(R.id.email);
        username.setText(user.getUsername());
        email.setText(user.getEmail());
        mWaveProgress = (WaveProgress) findViewById(R.id.wave_progress_bar);
        mWaveProgress.setValue((float) (Math.floor(user.getWwin() + user.getBwin()) / (user.getwLose() + user.getbLose() + user.getWwin() + user.getBwin())) * 100 + 1);
        if ((float) (user.getWwin() + user.getBwin()) / (user.getwLose() + user.getbLose() + user.getWwin() + user.getBwin()) <= 0.2) {
            head.setImageResource(R.drawable.pawn);
        } else if ((float) (user.getWwin() + user.getBwin()) / (user.getwLose() + user.getbLose() + user.getWwin() + user.getBwin()) > 0.2 && (user.getWwin() + user.getBwin()) / (user.getwLose() + user.getbLose() + user.getWwin() + user.getBwin()) <= 0.4) {
            head.setImageResource(R.drawable.knight);
        } else if ((float) (user.getWwin() + user.getBwin()) / (user.getwLose() + user.getbLose() + user.getWwin() + user.getBwin()) > 0.4 && (user.getWwin() + user.getBwin()) / (user.getwLose() + user.getbLose() + user.getWwin() + user.getBwin()) <= 0.6) {
            head.setImageResource(R.drawable.bishop);
        } else if ((float) (user.getWwin() + user.getBwin()) / (user.getwLose() + user.getbLose() + user.getWwin() + user.getBwin()) > 0.6 && (user.getWwin() + user.getBwin()) / (user.getwLose() + user.getbLose() + user.getWwin() + user.getBwin()) <= 0.8) {
            head.setImageResource(R.drawable.queen);
        } else if ((float) (user.getWwin() + user.getBwin()) / (user.getwLose() + user.getbLose() + user.getWwin() + user.getBwin()) > 0.8 && (user.getWwin() + user.getBwin()) / (user.getwLose() + user.getbLose() + user.getWwin() + user.getBwin()) <= 1.0) {
            head.setImageResource(R.drawable.king);
        }

    }
}