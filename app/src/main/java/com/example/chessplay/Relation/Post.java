package com.example.chessplay.Relation;

import com.example.chessplay.User;

import cn.bmob.v3.BmobObject;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.datatype.BmobRelation;


public class Post extends BmobObject {
    private String title;
    private String content;


    private User author;


    private String name;

    private BmobFile image;

    private BmobRelation likes;

    private int likesNum;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public User getAuthor() {
        return author;
    }

    public String getName() {
        return name;
    }

    public void setName(User author) {
        name = author.getUsername();
        this.name = name;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public BmobFile getImage() {
        return image;
    }

    public void setImage(BmobFile image) {
        this.image = image;
    }

    public BmobRelation getLikes() {
        return likes;
    }

    public void setLikes(BmobRelation likes) {
        this.likes = likes;
    }


}
