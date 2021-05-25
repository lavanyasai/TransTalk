package com.project.android.transtalk.models;

public class Chats {

    Boolean seen;
    Long timestamp;

    public Chats() {

    }

    public Chats(Boolean seen, Long timestamp) {
        this.seen = seen;
        this.timestamp = timestamp;
    }

    public Boolean getSeen() {
        return seen;
    }

    public void setSeen(Boolean seen) {
        this.seen = seen;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}
