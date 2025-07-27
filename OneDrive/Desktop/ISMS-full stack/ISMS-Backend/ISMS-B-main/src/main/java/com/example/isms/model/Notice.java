package com.example.isms.model;


public class Notice {
    private String noticeId;
    private String postedOn;
    private String attention;
    private String title;
    private String content;
    private String postedBy;
    private String attachment;

    public Notice() {}

    public Notice(String noticeId, String postedOn, String attention, String content, String title, String postedBy, String attachment) {
        this.noticeId = noticeId;
        this.postedOn = postedOn;
        this.attention = attention;
        this.content = content;
        this.title = title;
        this.postedBy = postedBy;
        this.attachment = attachment;
    }

    public String getNoticeId() {
        return noticeId;
    }

    public void setNoticeId(String noticeId) {
        this.noticeId = noticeId;
    }

    public String getPostedOn() {
        return postedOn;
    }

    public void setPostedOn(String postedOn) {
        this.postedOn = postedOn;
    }

    public String getAttention() {
        return attention;
    }

    public void setAttention(String attention) {
        this.attention = attention;
    }

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

    public String getPostedBy() {
        return postedBy;
    }

    public void setPostedBy(String postedBy) {
        this.postedBy = postedBy;
    }

    public String getAttachment() {
        return attachment;
    }

    public void setAttachment(String attachment) {
        this.attachment = attachment;
    }
}

