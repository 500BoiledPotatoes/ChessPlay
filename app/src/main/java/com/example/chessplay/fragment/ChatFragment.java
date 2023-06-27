package com.example.chessplay.fragment;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.chessplay.BookListActivity;
import com.example.chessplay.R;
import com.example.chessplay.RecommendActivity;
import com.example.chessplay.Relation.ForumActivity;


public class ChatFragment extends Fragment {
    private ImageView forum;
    private ImageView read;
    private ImageView recommend;
    public ChatFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        forum = view.findViewById(R.id.forum);
        forum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent Intent = new Intent(getActivity(), ForumActivity.class);
                startActivity(Intent);
            }
        });
        read = view.findViewById(R.id.read);
        read.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent Intent = new Intent(getContext(), BookListActivity.class);
                startActivity(Intent);
            }
        });
        recommend = view.findViewById(R.id.recommend);
        recommend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent Intent = new Intent(getContext(), RecommendActivity.class);
                startActivity(Intent);
            }
        });
        return view;

    }


}
