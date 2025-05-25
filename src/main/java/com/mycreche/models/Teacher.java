package com.mycreche.models;

import java.util.List;

public class Teacher {
    private int id;
    private String FirstName;
    private String LastName;
    private List<Group> groups;

    public Teacher() {
    
    }

    

    // Getters & Setters

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getFirstName() { return FirstName; }
    public void setFirstName(String FirstName) { this.FirstName = FirstName; }

    public String getLastName() { return LastName; }
    public void setLastName(String LastName) { this.LastName = LastName; }
    
    public List<Group> getGroups() {
        return groups;
    }

    public void setGroups(List<Group> groups) {
        this.groups = groups;
    }

    @Override
    public String toString() {
        return FirstName+" "+LastName;  // Display teacher's name in ComboBox
    }

}