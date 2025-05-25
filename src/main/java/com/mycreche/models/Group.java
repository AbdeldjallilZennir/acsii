package com.mycreche.models;

import java.util.List;

public class Group {
    private int id;
    private String name;
    private int teacherId; // only store the teacher's user ID

    // Optional: hold children in memory
    private List<Child> children;

    public Group(int id, String name, int teacherId) {
        this.id = id;
        this.name = name;
        this.teacherId = teacherId;
    }

    // Getters & Setters
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getTeacherId() {
        return teacherId;
    }

    public List<Child> getChildren() {
        return children;
    }

    public void setChildren(List<Child> children) {
        this.children = children;
    }

    @Override
    public String toString() {
        return name; // Or return something more descriptive like: id + " - " + name;
    }

}