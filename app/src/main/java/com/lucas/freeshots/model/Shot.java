package com.lucas.freeshots.model;


import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import hugo.weaving.DebugLog;

/**
 * 此类属性的名称要与服务器返回的字段名称一致，
 * 所以命名不符合Java编程规范。
 */
public class Shot implements Serializable {
    public int id;
    public String title;
    public String description;
    public int width;
    public int height;

    /*
     * The normal image is typically 400x300, but may be smaller if created before October 4th, 2012.
     * The width and height provide the size of the normal image.
     *
     * The hidpi image may or may not be present, but will always be 800x600.
     *
     * The teaser image is typically 200x150, but may be smaller if created before October 4th, 2012.
     *
     * If the animated attribute of the shot is true,
     * the highest resolution image available (hidpi or normal) will be animated (smaller images will be stills).
     */
    public Images images;

    public int views_count;
    public int likes_count;
    public int comments_count;
    public int attachments_count;
    public int rebounds_count;
    public int buckets_count;

    public String created_at;
    public String updated_at;

    public String html_url;
    public String attachments_url;
    public String buckets_url;
    public String comments_url;
    public String likes_url;
    public String projects_url;
    public String rebounds_url;

    public boolean animated;

    public List<String> tags = new ArrayList<>();

    public User user;
    public Team team;

    @Override
    public boolean equals(Object o) {
        return o != null && o instanceof Shot && (this == o || id == ((Shot) o).id);
    }

    @Override
    public int hashCode() {
        return ((Integer) id).hashCode();
    }

    //    @Override
//    public String toString() {
//        // TODO: 项目太多了，写不过来，可否用反射的方法做？？？？？？？
//        String s =  String.format("{\n" +
//                "id : %d,\n" +
//                "title : %s,\n" +
//                "description : %s\n" +
//                "}\n",
//                id, title, description);
//
//        return s + "\n" + images.toString();
//    }

    public static class Images implements Serializable {
        public String hidpi;
        public String normal;
        public String teaser;

        @DebugLog
        public String getHeightImageUri() {
            return hidpi != null ? hidpi : normal != null ? normal : teaser;
        }

        @DebugLog
        public @NonNull String getType() {
            String name = getHeightImageUri();

            if(name != null) {
                int i = name.lastIndexOf('.');
                if(i != -1 && i + 1 < name.length()) {
                    return name.substring(i + 1);
                }
            }

            return "null";
        }
//        @Override
//        public String toString() {
//            return String.format("{\nhidpi : %s,\nnormal : %s,\nteaser : %s\n}\n",
//                    hidpi != null ? hidpi : "null",
//                    normal != null ? normal : "null",
//                    teaser != null ? teaser : "null");
//        }
    }
}
