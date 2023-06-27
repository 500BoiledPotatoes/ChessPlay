package com.example.chessplay.Relation;

import static com.example.chessplay.R.id.tv_author;
import static com.example.chessplay.R.id.tv_describe;
import static com.example.chessplay.R.id.tv_time;
import static com.example.chessplay.R.id.tv_title;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.example.chessplay.R;
import com.example.chessplay.User;
import com.example.chessplay.adapter.BaseAdapterHelper;
import com.example.chessplay.adapter.QuickAdapter;
import com.example.chessplay.config.Constants;

import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobPointer;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;

public class DetailActivity extends BaseActivity implements View.OnClickListener{
    TextView post_name;
    TextView post_content, post_author;
    ListView listview;
    protected QuickAdapter<Comment> CommentAdapter;
    TextView tv_no;
    TextView post;
    Button btn_add;
    Post perPost;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void setContentView() {
        setContentView(R.layout.activity_detail);
    }

    @Override
    public void initViews() {
        post_name = (TextView) findViewById(R.id.name);
        post_content = (TextView) findViewById(R.id.content);
        post_author = (TextView) findViewById(tv_author);
        listview = (ListView) findViewById(R.id.list_comment);
        tv_no = (TextView) findViewById(R.id.tv_no);
        btn_add = (Button) findViewById(R.id.btn_add_comment);
        post = (TextView) findViewById(R.id.post);
        post.setTag("Post");
    }


    @Override
    public void initListeners() {
        btn_add.setOnClickListener(this);
    }

    @Override
    public void initData() {
        Intent intent = this.getIntent();
        perPost = (Post) intent.getSerializableExtra("post");
        post_name.setText(perPost.getTitle());
        post_author.setText(perPost.getAuthor().getUsername());
        post_content.setText(perPost.getContent());


        if (CommentAdapter == null) {
            CommentAdapter = new QuickAdapter<Comment>(this, R.layout.item_list1) {


                @Override
                protected void convert(BaseAdapterHelper helper, Comment comment) {
                    helper.setText(tv_describe, comment.getContent())
                            .setText(tv_time, comment.getCreatedAt())
                            .setText(tv_author, String.valueOf(comment.getAuthor().getObjectId()));
                }
            };
        }
        listview.setAdapter(CommentAdapter);

        queryComments();
    }

    private void showView() {
        listview.setVisibility(View.VISIBLE);
    }

    private void showErrorView(int tag) {
        listview.setVisibility(View.GONE);

    }

    private void queryComments() {
        showView();
        Intent intent = this.getIntent();
        perPost = (Post) intent.getSerializableExtra("post");
        BmobQuery<Comment> query = new BmobQuery<Comment>();

        query.addWhereEqualTo("post",new BmobPointer(perPost) );
        query.include("user,post.author");
        query.order("-updatedAt");
        query.findObjects(new FindListener<Comment>() {

            @Override
            public void done(List<Comment> comments, BmobException e) {
                CommentAdapter.clear();
                if (e == null) {
                    Log.d("dddd", String.valueOf(new BmobPointer(perPost)));
                    CommentAdapter.notifyDataSetChanged();
                    CommentAdapter.addAll(comments);
                    listview.setAdapter(CommentAdapter);
                    if (CommentAdapter.isEmpty()) {
                        showErrorView(0);

                    }
                } else {
                    showErrorView(0);
                    Log.e(TAG, e.toString());
                }

            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v == btn_add) {
            Intent intent = new Intent(this, AddCommentActivity.class);
            intent.putExtra("from", post.getTag().toString());
            Bundle bundle = new Bundle();
            bundle.putSerializable("post", perPost);
            intent.putExtras(bundle);
            startActivityForResult(intent, Constants.REQUESTCODE_ADD);
    }
}
}