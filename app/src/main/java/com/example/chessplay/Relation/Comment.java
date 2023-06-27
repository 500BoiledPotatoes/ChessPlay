package com.example.chessplay.Relation;

import com.example.chessplay.User;

import cn.bmob.v3.BmobObject;
import com.example.chessplay.Relation.Post;



public class Comment extends BmobObject {
    private String content;

    private User author;

    private Post post;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public  Post getPost() {
        return post;
    }

    public void setPost( Post post) {
        this.post = post;
    }


}
