package com.example.chessplay;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.LogInListener;

public class LoginActivity extends AppCompatActivity {
    private Button btn1, btn2, btn3;
    private EditText EmailAddress, password;
    private CheckBox VisibleOrNot;
    //private User user;
    ImageView imageView;
    TextView textView;
    int count = 0;

    @SuppressLint("ClickableViewAccessibility")
    private void showMsg(String msg) {
        Toast.makeText(LoginActivity.this, msg, Toast.LENGTH_LONG).show();
    }
    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bmob.initialize(this, "9a47c96e0d475ffa21405bf5d2bd3a1d");
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_login);
        imageView = findViewById(R.id.imageView);
        textView = findViewById(R.id.textView);
        btn1=(Button) findViewById(R.id.sign_in);
        EmailAddress=findViewById(R.id.emailAdress);
        password=findViewById(R.id.password);
        View forget_listener = findViewById(R.id.forget_password);
        //登录
        btn2=(Button)findViewById(R.id.sign_up);
        //注册
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
        forget_listener.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent Intent = new Intent(LoginActivity.this, ForgetActivity.class);
                startActivity(Intent);
            }
        });
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BmobQuery<User> UserAddress = new BmobQuery<>();
                UserAddress.addWhereEqualTo("email", EmailAddress.getText().toString());
                UserAddress.findObjects(new FindListener<User>() {
                    @Override
                    public void done(List<User> object, BmobException e) {
                        if (e == null) {
                            if (object.size()>0 && object.get(0).getEmailVerified() == true){
                                BmobUser.loginByAccount(EmailAddress.getText().toString(),password.getText().toString(), new LogInListener<User>() {
                                    @Override
                                    public void done(User user, BmobException e) {
                                        if (e == null) {
                                            Intent intent2 = new Intent(LoginActivity.this, MainActivity.class);
                                            intent2.putExtra("Email", EmailAddress.getText().toString());
                                            startActivity(intent2);
                                        } else {
                                            Log.e("BMOB", e.toString());
                                            showMsg("Login failure"+e.getMessage());
                                        }
                                    }
                                });
                            } else {
                                showMsg("Email not verified");
                            }
                        } else {
                            Log.e("BMOB", e.toString());
                            showMsg(e.toString());
                        }
                    }
                });

            }
        });




    }
}
