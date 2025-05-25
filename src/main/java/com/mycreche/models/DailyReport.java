package com.mycreche.models;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;

import com.mycreche.utils.Database;

import java.time.LocalDateTime;

public class DailyReport {
    private int id;
    private int childId;
    private int parentId;  // Added parent ID
    private int teacherId;
    private String teacherName;
    private LocalDate reportDate;
    
    // Child arrival/departure info
    private LocalTime arrivalTime;
    private LocalTime departureTime;
    
    // Meals
    private String breakfastStatus;
    private String lunchStatus;
    private String snackStatus;
    
    // Sleep
    private String sleepQuality;
    private String sleepDuration;
    
    // Diaper/bathroom
    private int diaperChanges;
    private boolean pottyTrainingUsed;
    
    // Mood
    private String mood;
    
    // Activities
    private boolean activitiesArt;
    private boolean activitiesMusic;
    private boolean activitiesReading;
    private boolean activitiesOutdoor;
    private boolean activitiesSensory;
    private boolean activitiesFreePlay;
    private boolean activitiesGroup;
    
    // Learning highlights
    private String learningHighlights;
    
    // Notes and communication
    private String generalNotes;
    private String remindersRequests;
    
    // Health notes
    private boolean medicationGiven;
    private boolean incidentOccurred;
    private boolean noHealthConcerns;
    private String healthNotes;
    
    // Metadata
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Boolean seen;
    
    // Constructors
    public DailyReport() {}
    
    public DailyReport(int childId, LocalDate reportDate,Boolean seen) {
        this.childId = childId;
        this.reportDate = reportDate;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.seen = seen;
    }

    public String getTeacherName() {             
        return fetchTeacherNameFromDatabase();
    }
    
    private String fetchTeacherNameFromDatabase() {
        String sql = """
        SELECT t.full_name 
        FROM teachers t
        JOIN groups g ON g.teacher_id = t.id
        JOIN children c ON c.group_id = g.id
        JOIN daily_reports dr ON dr.child_id = c.id
        WHERE dr.id = ? AND c.parent_id = ?
        """;
            
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, this.id);
            // Note: You'll need to pass parent_id somehow, or modify this approach
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                this.teacherName = rs.getString("full_name");
                return this.teacherName;
            }
            
        } catch (SQLException e) {
            System.err.println("Error fetching teacher name: " + e.getMessage());
        }
        
        return "Unknown Teacher";
    }
    
    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public Boolean isSeen() { return seen; }
    public void setSeen(Boolean seen) { this.seen = seen; }

    public int getChildId() { return childId; }
    public void setChildId(int childId) { this.childId = childId; }
    
    public int getParentId() { return parentId; }
    public void setParentId(int parentId) { this.parentId = parentId; }
    
    public int getTeacherId() { return teacherId; }
    public void setTeacherId(int teacherId) { this.teacherId = teacherId; }
    
    public LocalDate getReportDate() { return reportDate; }
    public void setReportDate(LocalDate reportDate) { this.reportDate = reportDate; }
    
    public LocalTime getArrivalTime() { return arrivalTime; }
    public void setArrivalTime(LocalTime arrivalTime) { this.arrivalTime = arrivalTime; }
    
    public LocalTime getDepartureTime() { return departureTime; }
    public void setDepartureTime(LocalTime departureTime) { this.departureTime = departureTime; }
    
    public String getBreakfastStatus() { return breakfastStatus; }
    public void setBreakfastStatus(String breakfastStatus) { this.breakfastStatus = breakfastStatus; }
    
    public String getLunchStatus() { return lunchStatus; }
    public void setLunchStatus(String lunchStatus) { this.lunchStatus = lunchStatus; }
    
    public String getSnackStatus() { return snackStatus; }
    public void setSnackStatus(String snackStatus) { this.snackStatus = snackStatus; }
    
    public String getSleepQuality() { return sleepQuality; }
    public void setSleepQuality(String sleepQuality) { this.sleepQuality = sleepQuality; }
    
    public String getSleepDuration() { return sleepDuration; }
    public void setSleepDuration(String sleepDuration) { this.sleepDuration = sleepDuration; }
    
    public int getDiaperChanges() { return diaperChanges; }
    public void setDiaperChanges(int diaperChanges) { this.diaperChanges = diaperChanges; }
    
    public boolean isPottyTrainingUsed() { return pottyTrainingUsed; }
    public void setPottyTrainingUsed(boolean pottyTrainingUsed) { this.pottyTrainingUsed = pottyTrainingUsed; }
    
    public String getMood() { return mood; }
    public void setMood(String mood) { this.mood = mood; }
    
    public boolean isActivitiesArt() { return activitiesArt; }
    public void setActivitiesArt(boolean activitiesArt) { this.activitiesArt = activitiesArt; }
    
    public boolean isActivitiesMusic() { return activitiesMusic; }
    public void setActivitiesMusic(boolean activitiesMusic) { this.activitiesMusic = activitiesMusic; }
    
    public boolean isActivitiesReading() { return activitiesReading; }
    public void setActivitiesReading(boolean activitiesReading) { this.activitiesReading = activitiesReading; }
    
    public boolean isActivitiesOutdoor() { return activitiesOutdoor; }
    public void setActivitiesOutdoor(boolean activitiesOutdoor) { this.activitiesOutdoor = activitiesOutdoor; }
    
    public boolean isActivitiesSensory() { return activitiesSensory; }
    public void setActivitiesSensory(boolean activitiesSensory) { this.activitiesSensory = activitiesSensory; }
    
    public boolean isActivitiesFreePlay() { return activitiesFreePlay; }
    public void setActivitiesFreePlay(boolean activitiesFreePlay) { this.activitiesFreePlay = activitiesFreePlay; }
    
    public boolean isActivitiesGroup() { return activitiesGroup; }
    public void setActivitiesGroup(boolean activitiesGroup) { this.activitiesGroup = activitiesGroup; }
    
    public String getLearningHighlights() { return learningHighlights; }
    public void setLearningHighlights(String learningHighlights) { this.learningHighlights = learningHighlights; }
    
    public String getGeneralNotes() { return generalNotes; }
    public void setGeneralNotes(String generalNotes) { this.generalNotes = generalNotes; }
    
    public String getRemindersRequests() { return remindersRequests; }
    public void setRemindersRequests(String remindersRequests) { this.remindersRequests = remindersRequests; }
    
    public boolean isMedicationGiven() { return medicationGiven; }
    public void setMedicationGiven(boolean medicationGiven) { this.medicationGiven = medicationGiven; }
    
    public boolean isIncidentOccurred() { return incidentOccurred; }
    public void setIncidentOccurred(boolean incidentOccurred) { this.incidentOccurred = incidentOccurred; }
    
    public boolean isNoHealthConcerns() { return noHealthConcerns; }
    public void setNoHealthConcerns(boolean noHealthConcerns) { this.noHealthConcerns = noHealthConcerns; }
    
    public String getHealthNotes() { return healthNotes; }
    public void setHealthNotes(String healthNotes) { this.healthNotes = healthNotes; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}