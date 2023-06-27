package com.example.chessplay.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.chessplay.EchartOptionUtil;
import com.example.chessplay.EchartView;
import com.example.chessplay.R;
import com.example.chessplay.User;
import com.example.chessplay.circleprogress.WaveProgress;
import com.github.abel533.echarts.axis.CategoryAxis;
import com.github.abel533.echarts.axis.ValueAxis;
import com.github.abel533.echarts.code.Trigger;
import com.github.abel533.echarts.json.GsonOption;
import com.github.abel533.echarts.series.Bar;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import cn.bmob.v3.BmobUser;

public class MyFragment extends Fragment {
    private Context mContext;
    private ImageView head;
    private TextView username;
    private TextView email;
    private EchartView lineChart;
    private EchartView pieChart;
    private EchartView barChart;
    private WaveProgress mWaveProgress;
    public MyFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my, container, false);
        mContext=getContext();
        head=view.findViewById(R.id.h_head);
        username = view.findViewById(R.id.user_name);
        email = view.findViewById(R.id.email);
//        lineChart = view.findViewById(R.id.lineChart);
        pieChart = view.findViewById(R.id.pieChart);
        barChart = view.findViewById(R.id.barChart);
        mWaveProgress = (WaveProgress) view.findViewById(R.id.wave_progress_bar);

        User user = BmobUser.getCurrentUser(User.class);
        if ((float)(user.getWwin()+user.getBwin())/(user.getwLose()+user.getbLose()+user.getWwin()+user.getBwin())<=0.2){
            head.setImageResource(R.drawable.pawn);
        }else if ((float)(user.getWwin()+user.getBwin())/(user.getwLose()+user.getbLose()+user.getWwin()+user.getBwin())>0.2 && (user.getWwin()+user.getBwin())/(user.getwLose()+user.getbLose()+user.getWwin()+user.getBwin())<=0.4){
            head.setImageResource(R.drawable.knight);
        }else if ((float)(user.getWwin()+user.getBwin())/(user.getwLose()+user.getbLose()+user.getWwin()+user.getBwin())>0.4 && (user.getWwin()+user.getBwin())/(user.getwLose()+user.getbLose()+user.getWwin()+user.getBwin())<=0.6){
            head.setImageResource(R.drawable.bishop);
        }else if ((float)(user.getWwin()+user.getBwin())/(user.getwLose()+user.getbLose()+user.getWwin()+user.getBwin())>0.6 && (user.getWwin()+user.getBwin())/(user.getwLose()+user.getbLose()+user.getWwin()+user.getBwin())<=0.8){
            head.setImageResource(R.drawable.queen);
        }else if ((float)(user.getWwin()+user.getBwin())/(user.getwLose()+user.getbLose()+user.getWwin()+user.getBwin())>0.8 && (user.getWwin()+user.getBwin())/(user.getwLose()+user.getbLose()+user.getWwin()+user.getBwin())<=1.0){
            head.setImageResource(R.drawable.king);
        }
        username.setText(user.getUsername());
        email.setText(user.getEmail());
        mWaveProgress.setValue( (float) (Math.floor(user.getWwin()+user.getBwin())/(user.getwLose()+user.getbLose()+user.getWwin()+user.getBwin()))*100 +1);

        pieChart.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                refreshPieChart();
            }
        });
        barChart.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                refreshBarChart();
            }
        });

        return view;
    }

    private void refreshPieChart(){
        User user = BmobUser.getCurrentUser(User.class);
        Integer black = user.getBwin();
        Integer white = user.getWwin();
        Integer mid = user.getmWin();
        Integer end = user.geteWin();
        DecimalFormat decimalFormat= new  DecimalFormat( "0.00" ); //构造方法的字符格式这里如果小数不足2位,会以0补足.
        List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
        HashMap hashMap1 = new HashMap();
        hashMap1.put("value", black);
        hashMap1.put("name", "Black: " +decimalFormat.format((float) black/(black+white+mid+end)));
        HashMap hashMap2 = new HashMap();
        hashMap2.put("value", white);
        hashMap2.put("name", "White: "+  decimalFormat.format((float) white/(black+white+mid+end)));
        HashMap hashMap3 = new HashMap();
        hashMap3.put("value", mid);
        hashMap3.put("name", "Middle game: " + decimalFormat.format((float) mid/(black+white+mid+end)));
        HashMap hashMap4 = new HashMap();
        hashMap4.put("value", end);
        hashMap4.put("name", "End game: " + decimalFormat.format((float) end/(black+white+mid+end)));
        data.add(hashMap3);
        data.add(hashMap1);
        data.add(hashMap2);

        data.add(hashMap4);

        pieChart.refreshEchartsWithOption(EchartOptionUtil.getPieChartOptions(data));
    }
    private void refreshBarChart(){
        User user = BmobUser.getCurrentUser(User.class);
        Integer kingStart = user.getKingStart();
        Integer queenStart = user.getQueenStart();
        Integer semiStart = user.getSemiClosedStart();
        Integer closedStart = user.getClosedStart();
        Object[] x = new Object[]{
                "KingStart", "QueenStart", "SemiClosedStart", "ClosedStart"
        };
        Object[] y = new Object[]{
                kingStart, queenStart, semiStart, closedStart
        };
        //刷新图标
        barChart.refreshEchartsWithOption(EchartOptionUtil.getBarChartOptions(x, y));
    }


}

