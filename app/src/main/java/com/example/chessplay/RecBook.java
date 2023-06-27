package com.example.chessplay;

import java.io.Serializable;

public class RecBook implements Serializable {

    private String Title;
    private String Author;
    private String content;
    private int imageId;

    public RecBook(String Title, String Author, String content,int imageId) {
        this.Title = Title;
        this.Author = Author;
        this.content = content;
        this.imageId = imageId;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public String getAuthor() {
        return Author;
    }

    public void setAuthor(String author) {
        Author = author;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
    public int getImageId() {
        return imageId;
    }

    public void setImageId(int imageId) {
        this.imageId = imageId;
    }
}
