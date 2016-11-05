package com.projects.ahmedtarek.iguide;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Ahmed Tarek on 11/5/2016.
 */
@IgnoreExtraProperties
public class Post {
    private String title;
    private String data;

    public Post() {
    }

    public Post(String title, String data) {
        this.title = title;
        this.data = data;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    @Exclude
    public Map<String, String> toMap() {
        Map<String, String> map = new HashMap<>();
        map.put("title", title);
        map.put("data", data);
        return map;
    }
}
