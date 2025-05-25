package com.mycreche.models;

import java.time.LocalDateTime;

public class ParentNotification {
    private int id;
    private int parentId;
    private Integer childId; // Nullable - some notifications may not be child-specific
    private String type;
    private String title;
    private String message;
    private boolean isRead;
    private LocalDateTime createdAt;

    // Constructors
    public ParentNotification() {
        this.createdAt = LocalDateTime.now();
        this.isRead = false;
    }

    public ParentNotification(int parentId, String type, String title, String message) {
        this();
        this.parentId = parentId;
        this.type = type;
        this.title = title;
        this.message = message;
    }

    public ParentNotification(int id, int parentId, Integer childId, String type, 
                            String title, String message, boolean isRead, LocalDateTime createdAt) {
        this.id = id;
        this.parentId = parentId;
        this.childId = childId;
        this.type = type;
        this.title = title;
        this.message = message;
        this.isRead = isRead;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getParentId() {
        return parentId;
    }

    public void setParentId(int parentId) {
        this.parentId = parentId;
    }

    public Integer getChildId() {
        return childId;
    }

    public void setChildId(Integer childId) {
        this.childId = childId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // Utility methods
    public String getFormattedCreatedAt() {
        return createdAt.format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"));
    }

    public String getTypeDisplay() {
        if (type == null) return "General";
        
        switch (type.toLowerCase()) {
            case "attendance": return "Attendance";
            case "report": return "Daily Report";
            case "medical": return "Medical";
            case "announcement": return "Announcement";
            case "payment": return "Payment";
            case "emergency": return "Emergency";
            default: return "General";
        }
    }

    public String getPreview() {
        if (message == null || message.isEmpty()) {
            return "";
        }
        return message.length() > 100 ? message.substring(0, 100) + "..." : message;
    }

    @Override
    public String toString() {
        return String.format("ParentNotification{id=%d, type='%s', title='%s', isRead=%s, createdAt='%s'}", 
                           id, type, title, isRead, createdAt);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ParentNotification that = (ParentNotification) obj;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}