package com.example.chessplay;


import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.example.chessplay.fragment.MyFragmentPagerAdapter;


public class MainActivity extends AppCompatActivity implements RadioGroup.OnCheckedChangeListener,
        ViewPager.OnPageChangeListener {

    private RadioGroup rg_tab_bar;
    private RadioButton rb_play;
    private RadioButton rb_chat;
    private RadioButton rb_my;
    private ViewPager vpager;

    private MyFragmentPagerAdapter mAdapter;

    public static final int PAGE_ONE = 0;
    public static final int PAGE_TWO = 1;
    public static final int PAGE_THREE = 2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAdapter = new MyFragmentPagerAdapter(getSupportFragmentManager());
        bindViews();
        rb_play.setChecked(true);

    }
    private void bindViews() {
        rg_tab_bar = (RadioGroup) findViewById(R.id.rg_tab_bar);
        rb_play = (RadioButton) findViewById(R.id.rb_play);
        rb_chat = (RadioButton) findViewById(R.id.rb_forum);
        rb_my = (RadioButton) findViewById(R.id.rb_my);
        rg_tab_bar.setOnCheckedChangeListener(this);

        vpager = (ViewPager) findViewById(R.id.vpager);
        vpager.setAdapter(mAdapter);
        vpager.setCurrentItem(0);
        vpager.addOnPageChangeListener(this);
    }
    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {
        if (state == 2) {
            switch (vpager.getCurrentItem()) {
                case PAGE_ONE:
                    rb_play.setChecked(true);
                    break;
                case PAGE_TWO:
                    rb_chat.setChecked(true);
                    break;
                case PAGE_THREE:
                    rb_my.setChecked(true);
                    break;
            }
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.rb_play:
                vpager.setCurrentItem(PAGE_ONE);
                break;
            case R.id.rb_forum:
                vpager.setCurrentItem(PAGE_TWO);
                break;
            case R.id.rb_my:
                vpager.setCurrentItem(PAGE_THREE);
                break;
        }
    }

}
