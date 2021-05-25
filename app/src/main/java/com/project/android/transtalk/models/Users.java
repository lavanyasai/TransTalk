package com.project.android.transtalk.models;

public class Users {

    private String Name;

    private String Email;

    private String Status;

    private String ImageUrl;

    private String ThumbUrl;

    private String Key;

    public Users() {

    }

    public Users(String Name, String Email, String Status, String ImageUrl, String ThumbUrl,String Key) {
        this.Name = Name;
        this.Email = Email;
        this.Status = Status;
        this.ImageUrl = ImageUrl;
        this.ThumbUrl = ThumbUrl;
        this.Key = Key;
    }

    public String getName() {
        return this.Name;
    }

    public void setName(String Name) {
        this.Name = Name;
    }

    public String getEmail() {
        return this.Email;
    }

    public void setEmail(String Email) {
        this.Email = Email;
    }

    public String getStatus() {
        return this.Status;
    }

    public void setStatus(String Status) {
        this.Status = Status;
    }

    public String getImageUrl() {
        return this.ImageUrl;
    }

    public void setImageUrl(String ImageUrl) {
        this.ImageUrl = ImageUrl;
    }

    public String getThumbUrl() {
        return this.ThumbUrl;
    }

    public void setThumbUrl(String ThumbUrl) {
        this.ThumbUrl = ThumbUrl;
    }

    public String getKey() {
        return this.Key;
    }

    public void setKey(String Key) {
        this.Key = Key;
    }

}

