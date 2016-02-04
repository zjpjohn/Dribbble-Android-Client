package com.lucas.freeshots.model;


import java.io.Serializable;

/**
 * 此类属性的名称要与服务器返回的字段名称一致，
 * 所以命名不符合Java编程规范。
 */
public class Bucket implements Serializable {
    public int id;
    public String name;
    public String description;
    public int shots_count;
    public String created_at;
    public String updated_at;
}
