package com.example.chessplay;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.UpdateListener;

public class ForgetActivity extends AppCompatActivity {
    private EditText EmailAddress;
    Button btn1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget);
        EmailAddress = (EditText) findViewById(R.id.email_forget);
        btn1 = findViewById(R.id.yanzheng);
        Button btn2 = findViewById(R.id.fanhuidenglu);
        Bmob.initialize(this, "9a47c96e0d475ffa21405bf5d2bd3a1d");
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = EmailAddress.getText().toString();
                BmobUser.resetPasswordByEmail(email, new UpdateListener() {

                    @Override
                    public void done(BmobException e) {
                        if (e == null) {
                            showMsg("Reset the password successfully. Please go to the email box to reset the password");
                            Intent Intent = new Intent(ForgetActivity.this, LoginActivity.class);
                            startActivity(Intent);
                        } else {
                            Log.e("BMOB", e.toString());
                            showMsg(e.getMessage());
                        }
                    }
                });
            }
        });
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ForgetActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }
    private void showMsg(String msg) {
        Toast.makeText(ForgetActivity.this, msg, Toast.LENGTH_LONG).show();
    }
}