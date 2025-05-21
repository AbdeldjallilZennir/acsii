package com.mycreche.models;

import java.time.LocalDate;

public class Child {
    private int id;
    private String fullName;
    private LocalDate birthDate;
    private String gender;
    private LocalDate enrollmentDate;
    private String groupName;
    private String allergies;
    private String medicalNotes;
    private String dietRestrictions;
    private String primaryContactName;
    private String relationship;
    private String phoneNumber;
    private String email;
    private String address;
    private String emergencyContact;
    private String authorizedPickup;
    private String specialInstructions;
    private boolean photoPermission;
    private String status;



    // Getters and Setters
    public String getStatus() {return status;}
    public void setStatus(String status) {this.status = status;}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public LocalDate getEnrollmentDate() { return enrollmentDate; }
    public void setEnrollmentDate(LocalDate enrollmentDate) { this.enrollmentDate = enrollmentDate; }

    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }

    public String getAllergies() { return allergies; }
    public void setAllergies(String allergies) { this.allergies = allergies; }

    public String getMedicalNotes() { return medicalNotes; }
    public void setMedicalNotes(String medicalNotes) { this.medicalNotes = medicalNotes; }

    public String getDietRestrictions() { return dietRestrictions; }
    public void setDietRestrictions(String dietRestrictions) { this.dietRestrictions = dietRestrictions; }

    public String getPrimaryContactName() { return primaryContactName; }
    public void setPrimaryContactName(String primaryContactName) { this.primaryContactName = primaryContactName; }

    public String getRelationship() { return relationship; }
    public void setRelationship(String relationship) { this.relationship = relationship; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getEmergencyContact() { return emergencyContact; }
    public void setEmergencyContact(String emergencyContact) { this.emergencyContact = emergencyContact; }

    public String getAuthorizedPickup() { return authorizedPickup; }
    public void setAuthorizedPickup(String authorizedPickup) { this.authorizedPickup = authorizedPickup; }

    public String getSpecialInstructions() { return specialInstructions; }
    public void setSpecialInstructions(String specialInstructions) { this.specialInstructions = specialInstructions; }

    public boolean isPhotoPermission() { return photoPermission; }
    public void setPhotoPermission(boolean photoPermission) { this.photoPermission = photoPermission; }
}