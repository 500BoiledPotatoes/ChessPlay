package com.example.chessplay;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;

public class RegisterActivity extends AppCompatActivity {
    private EditText userName, pwd, pwdCon, email;
    private Button btn, btn2;
    private boolean[] judge = {false, false, false, false};
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        if(actionBar!=null){
            actionBar.hide();
        }
        setContentView(R.layout.activity_register);
        Bmob.initialize(this, "9a47c96e0d475ffa21405bf5d2bd3a1d");
        userName = findViewById(R.id.regis_user_name);
        pwd = findViewById(R.id.reg_emailAdress);
        btn = findViewById(R.id.reg_back);
        btn2 = findViewById(R.id.reg_sign_up);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });


        init();

        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                User user = new User();
                if (!pwd.getText().toString().equals(pwdCon.getText().toString())) {
                    showMsg("password confirmation should be the same");
                    judge[0] = false;
                } else if (pwd.getText().toString().length() < 8) {
                    showMsg("the password should be more than 8 characters");
                    judge[0] = false;
                } else {
                    user.setPassword(pwd.getText().toString());
                    judge[0] = true;
                }

                if (userName.getText().toString().length() < 1) {
                    showMsg("user name shouldn't be empty");
                    judge[1] = false;
                } else if (userName.getText().toString().length() > 20) {
                    showMsg("user name is too long");
                    judge[1] = false;
                } else {
                    user.setUsername(userName.getText().toString());
                    judge[1] = true;
                }

                if (!isEmail(email.getText().toString())) {
                    showMsg("wrong email format");
                    judge[2] = false;
                } else {
                    user.setEmail(email.getText().toString());
                    judge[2] = true;
                }
                if (judge[0] && judge[1] && judge[2]) {
                    user.signUp(new SaveListener<User>() {
                        @Override
                        public void done(User user, BmobException e) {
                            if (e == null) {
                                showMsg("please sign in your email to validate");
                                Intent intent2 = new Intent(RegisterActivity.this, LoginActivity.class);
                                intent2.putExtra("Email", email.getText().toString());
                                emailVerify(email.getText().toString());
                                startActivity(intent2);
                            } else {
                                showMsg("fail register" + e.getMessage());
                            }
                        }
                    });

                }
            }
        });
    }
    private void emailVerify(String email) {
        final String Email = email;
        BmobUser.requestEmailVerify(Email, new UpdateListener() {
            @Override
            public void done(BmobException e) {
                if (e == null) {
                } else {
                }
            }
        });
    }



    private void showMsg(String msg) {
        Toast.makeText(RegisterActivity.this, msg, Toast.LENGTH_LONG).show();
    }

    private static boolean isEmail(String email) {
        String str = "^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$";
        Pattern p = Pattern.compile(str);
        Matcher m = p.matcher(email);
        return m.matches();
    }

    private void init() {
        userName = findViewById(R.id.regis_user_name);
        pwd = findViewById(R.id.reg_password);
        email = findViewById(R.id.reg_emailAdress);
        pwdCon = findViewById(R.id.reg_confirm);
        judge[0] = false;
        judge[1] = false;
        judge[2] = false;
        judge[3] = false;
        user = new User();
    }
}
