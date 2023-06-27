package com.example.chessplay.fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.chessplay.ChessPlay;
import com.example.chessplay.ChessPlayApp;
import com.example.chessplay.GameActivity;
import com.example.chessplay.R;
import com.example.chessplay.RankActivity;


public class PlayFragment extends Fragment {
    private ImageView pvp;
    private ImageView game;
    private ImageView rank;
    public PlayFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_play, container, false);
        pvp =  view.findViewById(R.id.pvp);
        pvp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent Intent = new Intent(getActivity(), GameActivity.class);
                startActivity(Intent);
            }
        });

        game =  view.findViewById(R.id.Localgame);
        game.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent Intent = new Intent(getContext(), ChessPlay.class);
                startActivity(Intent);
            }
        });
        rank =  view.findViewById(R.id.rank);
        rank.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent Intent = new Intent(getContext(), RankActivity.class);
                startActivity(Intent);
            }
        });
        return view;




    }


}
