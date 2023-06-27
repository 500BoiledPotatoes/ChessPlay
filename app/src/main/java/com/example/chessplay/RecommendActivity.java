package com.example.chessplay;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobUser;

public class RecommendActivity extends AppCompatActivity {
    HashMap<String,String> books = new HashMap<>();
    HashMap<String,String> author = new HashMap<>();
    HashMap<String,String> content = new HashMap<>();
    private List<RecBook> data = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recommand);
        books.put("start","Chess Opening Essentials");
        books.put("start1","Modern Chess Openings");

        author.put("start","Djuric, Stefan/ Komarov, Dimitri/ Pantaleoni, Claudio");
        author.put("start1","Firmian, Nick de ");
        content.put("start","Volume 2 of an accessible primer and reference book about chess openings. Provides a solid foundation to build your opening repertoire on. Explains what you should be trying to achieve, with clear indications for further study. Beginning and improving players should get a copy of this book before they buy Any other chess opening book.");
        content.put("start1","Modern Chess Openings is the best and most trusted tool for serious chess players on the market. First published over a half-century ago, this is a completely revised and updated edition of the book that has been the standard English language reference on chess openings. An invaluable resource for club and tournament players, it now includes information on recent matches and the most up-to-date theory on chess openings.\n" +
                "\n" +
                "Modern Chess Openings is ideal for intermediate players ready to elevate their game to the next level or International Grandmasters who want to stay on top of recent chess innovations.");

        books.put("mid","Chess Middle Game");
        books.put("mid1","Chess Middle Game Guideliness");
        author.put("mid","Euwe/ Kramer");
        author.put("mid1","Yuri Averbach");
        content.put("mid","How to improve a player's ability to judge the position and plan during the middle game. The middle game is the most confusing stage of a chess player's game. After leaving the encyclopedia of openings and stalemates, each move is a true expression of a player's ability to create a new position.\n");
        content.put("mid1","The main point of \"Chess Middlegame Guide\": It goes without saying that the middlegame is the most difficult and complex, but also the most attractive and interesting phase of chess. There are quite a few research works on the middlegame, and a large number of textbooks have studied the middlegame containing thousands of instructive examples, but as we feel, the plethora of books intimidates chess lovers and discourages not only players who wish to improve, but also those who simply want to get the satisfaction of playing chess. Therefore, I wanted to write in a few pages the middle game \"Chess Middle Game Guide\", which contains the most important and necessary elements that can help a player to independently solve many problems that arise during the battle. As I have pointed out, the middle game is the most complex phase of chess, and many of its problems can be solved in a variety of different ways. In my opinion, the aim of the game is to kill the opponent's king, which requires mastering the coordination of offensive pieces, so special attention should be paid to the coordination of pieces and their activities.\n" +
                "\n" +
                "Translated with www.DeepL.com/Translator (free version)");

        books.put("end","Chess Stump Book");
        books.put("end1","Chess stump game essentials ");
        author.put("end","Jeremy Silman");
        author.put("end1","Xie jun");
        content.put("start1","The book focuses on the study of chess stalemates, with a light-hearted and humorous writing style and rigorous and comprehensive content. The book progressively ranks the difficulty of the games from beginner to master, and explains in detail some special situations. The author also removes some difficult but uncommon types of stalemates, which are explained from a practical point of view. Each chapter is followed by a corresponding exercise, which is followed by a detailed explanation. At the end of the book, the authors highlight several famous chess grandmasters, each of whom is followed by a number of classic games.\n");
        content.put("start1","The classic games of Anand, Topalov, Kasparov and other top masters are all at your fingertips, and Mr. Xie introduces the King's Pawn stalemate, Rook's Pawn stalemate, and the Bridge method one by one to illustrate the definite examples. You need to have a certain foundation to read");

        books.put("mid","Chess Middle Game");
        books.put("mid1","Chess Middle Game Guideliness");
        author.put("mid","Euwe/ Kramer");
        author.put("mid1","Yuri Averbach");
        content.put("mid","How to improve a player's ability to judge the position and plan during the middle game. The middle game is the most confusing stage of a chess player's game. After leaving the encyclopedia of openings and stalemates, each move is a true expression of a player's ability to create a new position.\n");
        content.put("mid1","The main point of \"Chess Middlegame Guide\": It goes without saying that the middlegame is the most difficult and complex, but also the most attractive and interesting phase of chess. There are quite a few research works on the middlegame, and a large number of textbooks have studied the middlegame containing thousands of instructive examples, but as we feel, the plethora of books intimidates chess lovers and discourages not only players who wish to improve, but also those who simply want to get the satisfaction of playing chess. Therefore, I wanted to write in a few pages the middle game \"Chess Middle Game Guide\", which contains the most important and necessary elements that can help a player to independently solve many problems that arise during the battle. As I have pointed out, the middle game is the most complex phase of chess, and many of its problems can be solved in a variety of different ways. In my opinion, the aim of the game is to kill the opponent's king, which requires mastering the coordination of offensive pieces, so special attention should be paid to the coordination of pieces and their activities.\n");

        books.put("black","Chess Openings for Black, Explained");
        books.put("white","Chess Openings for White, Explained");
        author.put("black","Lev Alburt / Roman Dzindzichashvil / Eugene Perelshteyn");
        author.put("white"," Alburt, Lev; Dzindzichashvili, Roman; Perelshteyn, Eugene");
        content.put("black","Chess Openings for Black, Explained gives you a complete repertoire of carefully selected, interrelated openings. Three-time U.S. champion and master teacher Lev Alburt, along with his grandmaster co-authors, provides everything you need to know to defend with confidence against each and every one of White's first moves. This second edition is fully updated to reflect new developments in chess during the last three years, thoroughly grounding the player in the grandmaster-openings of modern chess, teaching you the opening that scores highest against White on a master level. Fully illustrated with two-color chess diagrams throughout.");
        content.put("white","Every chess player needs a set of openings he can trust. Use Bobby Fischer's favourite first move, 1. e2-e4, to begin your games as White - and know the ideas and moves that follow, no matter how Black defends! White moves first in chess and can, if he's well prepared, immediately put the pressure on Black. In this highly praised volume, three leading grandmasters logically explain a system of carefully selected and interrelated openings, covering all responses by Black! The second edition is completely updated with the newest games and ideas.");
        User user = BmobUser.getCurrentUser(User.class);
        float sWinRate = (float) user.getsWin()/(user.getsWin()+ user.getsLose());
        float mWinRate = (float) user.getmWin()/(user.getmWin()+ user.getmLose());
        float eWinRate = (float) user.geteWin()/(user.geteWin()+ user.geteLose());
        float bWinRate = (float) user.getBwin()/(user.getBwin()+ user.getbLose());
        float wWinRate = (float) user.getWwin()/(user.getWwin()+ user.getwLose());
        recommendOpeningBook(sWinRate,mWinRate,eWinRate,bWinRate,wWinRate);
        RecyclerView recyclerView = findViewById(R.id.rv);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        MyAdapter myAdapter = new MyAdapter(data, this);
        recyclerView.setAdapter(myAdapter);
        recyclerView.addItemDecoration(new LinearSpacingItemDecoration(this,20));

        myAdapter.setOnRecyclerItemClickListener(new MyAdapter.OnRecyclerItemClickListener() {
            @Override
            public void onRecyclerItemClick(int position) {
                Intent intent = new Intent(RecommendActivity.this, book_detail.class);
                intent.putExtra("position",position);
                Bundle bundle = new Bundle();
                bundle.putSerializable("book", data.get(position));
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
    }

    private void recommendOpeningBook(float sWinRate, float mWinRate, float eWinRate, float bWinRate, float wWinRate){
        if (sWinRate <= 0.5){
            RecBook start = new RecBook(books.get("start"), author.get("start"), content.get("start"),R.drawable.djuric);
            data.add(start);
            RecBook start1 = new RecBook(books.get("start1"), author.get("start1"), content.get("start1"),R.drawable.firmian);
            data.add(start1);
        }
        if (mWinRate <= 0.5){
            RecBook mid= new RecBook(books.get("mid"), author.get("start"), content.get("start"),R.drawable.euwe);
            data.add(mid);
            RecBook mid1 = new RecBook(books.get("mid1"), author.get("start1"), content.get("start1"),R.drawable.yuriaverbach);
            data.add(mid1);
        }if (eWinRate <= 0.5){
            RecBook end = new RecBook(books.get("end"), author.get("start"), content.get("start"),R.drawable.jeremy);
            data.add(end);
            RecBook end1 = new RecBook(books.get("end1"), author.get("start1"), content.get("start1"),R.drawable.xiejun);
            data.add(end1);
        }if (bWinRate <= 0.5){
            RecBook black = new RecBook(books.get("black"), author.get("start"), content.get("start"),R.drawable.lev);
            data.add(black);
        }if (wWinRate <= 0.5){
            RecBook white = new RecBook(books.get("white"), author.get("start"), content.get("start"),R.drawable.alburt);
            data.add(white);
        }
    }
}