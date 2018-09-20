package com.component.router.enums;

/**
 * Created by fox.hu on 2018/9/20.
 */

public enum NodeType {
    ACTIVITY(0, "android.app.Activity"),
    INVALID(-1,"invalid node type, currently only activity allowed");

    int id;
    String className;

    NodeType(int id, String className) {
        this.id = id;
        this.className = className;
    }

    public int getId() {
        return id;
    }

    public NodeType setId(int id) {
        this.id = id;
        return this;
    }

    public String getClassName() {
        return className;
    }

    public NodeType setClassName(String className) {
        this.className = className;
        return this;
    }

}
