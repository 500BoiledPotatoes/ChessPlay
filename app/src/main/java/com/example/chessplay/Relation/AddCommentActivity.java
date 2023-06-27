package com.example.chessplay.Relation;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chessplay.R;
import com.example.chessplay.User;

import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;

public class AddCommentActivity extends BaseActivity implements View.OnClickListener {
    EditText edit_describe;
    Button btn_back, btn_true;

    TextView tv_add;
    String from = "";

    String old_describe = "";
    String commentId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void setContentView() {
        setContentView(R.layout.activity_add_comment);
    }

    @Override
    public void initViews() {
        tv_add = (TextView) findViewById(R.id.tv_add);
        btn_back = (Button) findViewById(R.id.btn_back);
        btn_true = (Button) findViewById(R.id.btn_true);
        edit_describe = (EditText) findViewById(R.id.edit_describe);

    }

    @Override
    public void initListeners() {
        btn_back.setOnClickListener(this);
        btn_true.setOnClickListener(this);
    }
    String describe = "";

    @Override
    public void initData() {
        from = getIntent().getStringExtra("from");
        old_describe = getIntent().getStringExtra("describe");

        edit_describe.setText(old_describe);

        if (from.equals("Post")) {
            tv_add.setText("Comment");
        }else{
            tv_add.setText("Comment");
        }
    }

    @Override
    public void onClick(View v) {
        if (v == btn_true) {
            addByType();
        } else if (v == btn_back) {
            finish();
        }
    }

    private void addByType(){
        describe = edit_describe.getText().toString();

        if(TextUtils.isEmpty(describe)){
            ShowToast("Please fill in the content");
            return;
        }
        if(from.equals("Comment")){
            addComment();
        }else{
            addComment();
        }
    }

    private void addComment(){
        User user = BmobUser.getCurrentUser(User.class);
        Intent intent = this.getIntent();
        Post post = (Post) intent.getSerializableExtra("post");
        Comment comment = new Comment();
        comment.setContent(describe);
        comment.setAuthor(user);
        comment.setPost(post);
        comment.save(new SaveListener<String>() {
            @Override
            public void done(String s, BmobException e) {
                if (e == null) {
                    toast("Successfully added");
                    commentId = s;
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Log.e(TAG, e.toString());
                }
            }
        });
    }


    public void toast(String string) {
        Toast.makeText(getApplicationContext(), string, Toast.LENGTH_LONG).show();
    }
}