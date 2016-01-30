package com.lucas.freeshots.model;

import java.io.Serializable;

public class Comment implements Serializable {
    public int id;
    public String body;
    public int likes_count;
    public String likes_url;
    public String created_at;
    public String updated_at;

    public User user;
}
