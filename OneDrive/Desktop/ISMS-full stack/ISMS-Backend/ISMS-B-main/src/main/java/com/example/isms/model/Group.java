package com.example.isms.model;

public class Group {
    private String groupName;
    private String groupMail;

    public Group() {}

    public Group(String groupName, String groupMail) {
        this.groupName = groupName;
        this.groupMail = groupMail;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupMail() {
        return groupMail;
    }

    public void setGroupMail(String groupMail) {
        this.groupMail = groupMail;
    }
}

