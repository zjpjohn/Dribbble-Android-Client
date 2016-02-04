package com.lucas.freeshots.model;


import java.io.Serializable;

/**
 * 此类属性的名称要与服务器返回的字段名称一致，
 * 所以命名不符合Java编程规范。
 */
public class User implements Serializable {
    public int id;
    public String name;
    public String html_url;
    public String avatar_url;
    public String bio;
    public String location;

    public Links links;

    public int buckets_count;
    public int comments_received_count;
    public int followers_count;
    public int followings_count;
    public int likes_count;
    public int likes_received_count;
    public int projects_count;
    public int rebounds_received_count;
    public int shots_count;
    public int teams_count;

    public boolean can_upload_shot;
    public String type;
    public boolean pro;

    public String buckets_url;
    public String followers_url;
    public String following_url;
    public String likes_url;
    public String shots_url;
    public String teams_url;
    public String created_at;
    public String updated_at;
}
