package com.example.chessplay.Relation;

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

public class AddActivity extends BaseActivity implements View.OnClickListener {

    EditText edit_title, edit_describe;
    Button btn_back, btn_true;

    TextView tv_add;
    String from = "";

    String old_title = "";
    String old_describe = "";
    String postId = "";
    @Override
    public void setContentView() {
        setContentView(R.layout.activity_add);
    }

    @Override
    public void initViews() {
        tv_add = (TextView) findViewById(R.id.tv_add);
        btn_back = (Button) findViewById(R.id.btn_back);
        btn_true = (Button) findViewById(R.id.btn_true);
        edit_describe = (EditText) findViewById(R.id.edit_describe);
        edit_title = (EditText) findViewById(R.id.edit_title);
    }

    @Override
    public void initListeners() {
        btn_back.setOnClickListener(this);
        btn_true.setOnClickListener(this);
    }

    @Override
    public void initData() {
        from = getIntent().getStringExtra("from");
        old_title = getIntent().getStringExtra("title");
        old_describe = getIntent().getStringExtra("describe");

        edit_title.setText(old_title);
        edit_describe.setText(old_describe);
        if (from.equals("My Post")) {

            tv_add.setText("Post");
        }else{
            tv_add.setText("Post");
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
    String title = "";
    String describe = "";

    private void addByType(){
        title = edit_title.getText().toString();
        describe = edit_describe.getText().toString();

        if(TextUtils.isEmpty(title)){
            ShowToast("Please fill in the title");
            return;
        }
        if(TextUtils.isEmpty(describe)){
            ShowToast("Please fill in the content");
            return;
        }
        if(from.equals("My Post")){
            addPerpost();
        }else{
            addPerpost();
        }
    }

    private void addPerpost(){
        User user = BmobUser.getCurrentUser(User.class);
        Post post = new Post();
        post.setContent(describe);
        post.setTitle(title);
        post.setAuthor(user);
        post.save(new SaveListener<String>() {
            @Override
            public void done(String s, BmobException e) {
                if (e == null) {
                    toast("Successfully added");
                    postId = s;
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