package com.example.chessplay;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.example.chessplay.Relation.Post;

import org.lwjgl.Sys;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;

public class RankActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rank);
        BmobQuery<User> eq1 = new BmobQuery<>();
        eq1.addWhereGreaterThan("wLose", 0);//年龄<=29

        BmobQuery<User> eq2 = new BmobQuery<User>();
        eq2.addWhereGreaterThan("bLose", 0);//年龄>=6


        BmobQuery<User> eq3 = new BmobQuery<User>();
        eq3.addWhereGreaterThan("bwin", 0);
        BmobQuery<User> eq4 = new BmobQuery<User>();
        eq4.addWhereGreaterThan("wwin", 0);

        List<BmobQuery<User>> andQuerys = new ArrayList<BmobQuery<User>>();
        andQuerys.add(eq1);
        andQuerys.add(eq2);
        andQuerys.add(eq3);
        andQuerys.add(eq4);
        BmobQuery<User> query = new BmobQuery<User>();
        query.or(andQuerys);
        query.findObjects(new FindListener<User>() {

            @Override
            public void done(List<User> users, BmobException e) {
                if (e==null){
                    Collections.sort(users, new PersonComparator());
                    init(users);
                }else {
                    Log.e(TAG, e.toString());

                }
            }

        });

    }


    class PersonComparator implements Comparator<User> {
        @Override
        public int compare(User p1, User p2) {
            return (int) ((int) ((float)(p2.getWwin()+p2.getBwin())/(p2.getwLose()+p2.getbLose()+p2.getWwin()+p2.getBwin()))*100 - ((float)(p1.getWwin()+p1.getBwin())/(p1.getwLose()+p1.getbLose()+p1.getWwin()+p1.getBwin()))*100);
        }
    }

    public void init(List<User> data) {
        RecyclerView recyclerView = findViewById(R.id.rv1);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        RankAdapter rankAdapter = new RankAdapter(data, this);
        recyclerView.setAdapter(rankAdapter);
        recyclerView.addItemDecoration(new LinearSpacingItemDecoration(this, 20));
        rankAdapter.setOnRecyclerItemClickListener(new RankAdapter.OnRecyclerItemClickListener() {
            @Override
            public void onRecyclerItemClick(int position) {
                Intent intent = new Intent(RankActivity.this, PlayerActivity.class);
                intent.putExtra("position", position);
                Bundle bundle = new Bundle();
                bundle.putSerializable("user", data.get(position));
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

    }

}