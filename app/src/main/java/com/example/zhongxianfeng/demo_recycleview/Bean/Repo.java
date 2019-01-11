package com.example.zhongxianfeng.demo_recycleview.Bean;

public class Repo {
    private long id;
    private String full_name;
    private int watchers;
    public void setId(long id) {
        this.id = id;
    }
    public long getId() {
        return id;
    }

    public int getWatchers() {
        return watchers;
    }

    public void setWatchers(int watchers) {
        this.watchers = watchers;
    }

    public String getFull_name() {
        return full_name;
    }

    public void setFull_name(String full_name) {
        this.full_name = full_name;
    }
}
