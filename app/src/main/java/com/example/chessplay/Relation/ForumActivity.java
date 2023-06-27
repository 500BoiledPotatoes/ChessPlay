package com.example.chessplay.Relation;

import static com.example.chessplay.R.id.tv_author;
import static com.example.chessplay.R.id.tv_describe;
import static com.example.chessplay.R.id.tv_time;
import static com.example.chessplay.R.id.tv_title;

import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chessplay.LoginActivity;
import com.example.chessplay.R;
import com.example.chessplay.User;
import com.example.chessplay.adapter.BaseAdapterHelper;
import com.example.chessplay.adapter.QuickAdapter;
import com.example.chessplay.base.EditPopupWindow;
import com.example.chessplay.config.Constants;
import com.example.chessplay.i.IPopupItemClick;

import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobPointer;
import cn.bmob.v3.datatype.BmobRelation;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.UpdateListener;

public class ForumActivity extends BaseActivity implements View.OnClickListener,
        IPopupItemClick, AdapterView.OnItemLongClickListener,AdapterView.OnItemClickListener {
    RelativeLayout layout_action;
    LinearLayout layout_all;
    TextView tv_post;
    ListView listview;
    Button btn_add;
    protected QuickAdapter<Post> PostAdapter;

    private Button layout_pubpost;
    private Button layout_perpost;

    int likesNum;
    PopupWindow morePop;
    LinearLayout layout_no;
    TextView tv_no;


    @Override
    public void setContentView() {
        setContentView(R.layout.activity_forum);

    }

    @Override
    public void initViews() {
        layout_no = (LinearLayout) findViewById(R.id.layout_no);
        tv_no = (TextView) findViewById(R.id.tv_no);

        layout_action = (RelativeLayout) findViewById(R.id.layout_action);
        layout_all = (LinearLayout) findViewById(R.id.layout_all);
        tv_post = (TextView) findViewById(R.id.tv_post);
        tv_post.setTag("My Post");
        listview = (ListView) findViewById(R.id.list_post);

        btn_add = (Button) findViewById(R.id.btn_add);

        initEditPop();
    }

    @Override
    public void initListeners() {
        listview.setOnItemLongClickListener(this);
        listview.setOnItemClickListener(this);
        btn_add.setOnClickListener(this);
        layout_all.setOnClickListener(this);

    }

    public void onClick(View v) {
        if (v == layout_all) {
            showListPop();
        } else if (v == btn_add) {
            Intent intent = new Intent(this, AddActivity.class);
            intent.putExtra("from", tv_post.getTag().toString());
            startActivityForResult(intent, Constants.REQUESTCODE_ADD);
        } else if (v == layout_perpost) {
            changeTextView(v);
            morePop.dismiss();
            queryPerposts();
        } else if (v == layout_pubpost) {
            changeTextView(v);
            morePop.dismiss();
            queryPubposts();
        }
    }

    @Override
    public void initData() {
        if (PostAdapter == null) {
            PostAdapter = new QuickAdapter<Post>(this, R.layout.item_list) {
                @Override
                protected void convert(BaseAdapterHelper helper, Post post) {

                    helper.setText(tv_title, post.getTitle())
                            .setText(tv_describe, post.getContent())
                            .setText(tv_time, post.getCreatedAt())
                            .setText(tv_author, post.getAuthor().getUsername());
                }
            };
        }
        listview.setAdapter(PostAdapter);

        queryPerposts();

    }


    private void changeTextView(View v) {
        if (v == layout_pubpost) {
            tv_post.setTag("Square");
            tv_post.setText("Square");
        } else {
            tv_post.setTag("My Post");
            tv_post.setText("My Post");
        }
    }


    @SuppressWarnings("deprecation")
    private void showListPop() {
        View view = LayoutInflater.from(this).inflate(R.layout.pop_post, null);
        layout_perpost = (Button) view.findViewById(R.id.layout_post);
        layout_perpost.setOnClickListener(this);
        layout_pubpost = (Button) view.findViewById(R.id.layout_pubpost);
        layout_pubpost.setOnClickListener(this);
        morePop = new PopupWindow(view, mScreenWidth, 600);

        morePop.setTouchInterceptor(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
                    morePop.dismiss();
                    return true;
                }
                return false;
            }
        });

        morePop.setWidth(WindowManager.LayoutParams.MATCH_PARENT);
        morePop.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        morePop.setTouchable(true);
        morePop.setFocusable(true);
        morePop.setOutsideTouchable(true);
        morePop.setBackgroundDrawable(new BitmapDrawable());
        morePop.setAnimationStyle(R.style.MenuPop);
        morePop.showAsDropDown(layout_action, 0, -dip2px(this, 2.0F));
    }

    private void initEditPop() {
        mPopupWindow = new EditPopupWindow(this, 200, 48);
        mPopupWindow.setOnPopupItemClickListner(this);
    }

    EditPopupWindow mPopupWindow;
    int position;

    @Override
    public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2,
                                   long arg3) {
        position = arg2;
        int[] location = new int[2];
        arg1.getLocationOnScreen(location);
        mPopupWindow.showAtLocation(arg1, Gravity.RIGHT | Gravity.TOP,
                location[0], getStateBar() + location[1]);
        return true;
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                            long arg3) {
        position = arg2;
        Post post = PostAdapter.getItem(position);
        int[] location = new int[2];
        arg1.getLocationOnScreen(location);
        Intent intent2 = new Intent(ForumActivity.this, DetailActivity.class);

        Bundle bundle = new Bundle();
        bundle.putSerializable("post", post);
        intent2.putExtras(bundle);
        startActivity(intent2);
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case Constants.REQUESTCODE_ADD:
                String tag = tv_post.getTag().toString();
                if (tag.equals("My Post")) {
                    queryPerposts();
                } else {
                    queryPubposts();
                }
                break;
        }
    }


    @Override
    public void onEdit(View v) {
            String tag = tv_post.getTag().toString();
            Intent intent = new Intent(this, AddActivity.class);
            String title = "";
            String describe = "";
            if (tag.equals("My Post")) {
                title = PostAdapter.getItem(position).getTitle();
                describe = PostAdapter.getItem(position).getContent();
                intent.putExtra("describe", describe);
                intent.putExtra("title", title);
                intent.putExtra("from", tag);
                startActivityForResult(intent, Constants.REQUESTCODE_ADD);
            }else{
                toast("Go to Personal Space Editor");
            }



    }

    @Override
    public void onDelete(View v) {
        String tag = tv_post.getTag().toString();
        if (tag.equals("My Post")) {
            deletePost();
        } else {
            toast("Go to Personal Space Editor");
        }
    }

    @Override
    public void onLikes(View v) {
        addLikes();
        queryMoreToMore();
    }


    private void queryPerposts() {
        showView();
        User user = BmobUser.getCurrentUser(User.class);
        BmobQuery<Post> query = new BmobQuery<Post>();
        query.addWhereEqualTo("author",user);
        query.order("-updatedAt");
        query.include("author");
        query.findObjects(new FindListener<Post>() {

            @Override
            public void done(List<Post> posts, BmobException e) {
                PostAdapter.clear();
                if (e == null) {
                    PostAdapter.notifyDataSetChanged();
//                    progress.setVisibility(View.GONE);
                    PostAdapter.addAll(posts);
                    listview.setAdapter(PostAdapter);
                    if (PostAdapter.isEmpty()){
                        showErrorView(0);
                    }
                } else {
                    showErrorView(0);
                    Log.e(TAG, e.toString());
                }

            }

        });
    }

    private void queryPubposts() {
        showView();
        BmobQuery<Post> query = new BmobQuery<Post>();
        query.order("-updatedAt");
        query.addWhereExists("author");
        query.include("author");
        query.findObjects(new FindListener<Post>() {

            @Override
            public void done(List<Post> posts, BmobException e) {
                PostAdapter.clear();
                if (e == null) {
                    PostAdapter.notifyDataSetChanged();
//                    progress.setVisibility(View.GONE);
                    PostAdapter.addAll(posts);
                    listview.setAdapter(PostAdapter);
                    if (PostAdapter.isEmpty()){
                        showErrorView(0);
                    }
                } else {
                    showErrorView(0);
                    Log.e(TAG, e.toString());
                }

            }

        });
        showView();
    }



    private void showView() {
        listview.setVisibility(View.VISIBLE);
        layout_no.setVisibility(View.GONE);
    }


    private void showErrorView(int tag) {
        listview.setVisibility(View.GONE);
        layout_no.setVisibility(View.VISIBLE);
        if (tag == 0) {
            tv_no.setText("No post at the moment");
        }
    }


    private void deletePost() {
        Post post = new Post();
        post.remove("author");
        post.setObjectId(PostAdapter.getItem(position).getObjectId());
        post.update(new UpdateListener() {
            @Override
            public void done(BmobException e) {
                PostAdapter.remove(position);
                toast("Successfully delete");
                }
        });
    }

    private void addLikes() {
        User user = BmobUser.getCurrentUser(User.class);
        Post post = new Post();
        post.setObjectId(PostAdapter.getItem(position).getObjectId());
        BmobRelation relation = new BmobRelation();
        relation.add(user);
        post.setLikes(relation);
        post.update(new UpdateListener() {
            @Override
            public void done(BmobException e) {
            }
        });
    }

    private void queryMoreToMore() {
        BmobQuery<User>  query  = new BmobQuery<>();
        Post post = new Post();
        post.setObjectId(PostAdapter.getItem(position).getObjectId());
        query.addWhereRelatedTo("likes",new BmobPointer(post));
        query.findObjects(new FindListener<User>() {
            @Override
            public void done(List<User> list, BmobException e) {
                if (e == null) {
                    toast("Number： " + list.size());
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