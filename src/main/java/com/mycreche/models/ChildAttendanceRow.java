package com.mycreche.models;

import javafx.beans.property.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Period;

public class ChildAttendanceRow {
    private final int childId;
    private final StringProperty name;
    private final StringProperty age;
    private final StringProperty parent;
    private final ObjectProperty<LocalTime> arrivalTime;
    private final ObjectProperty<LocalTime> departureTime;
    private final BooleanProperty editable;

    public ChildAttendanceRow(int childId, String name, LocalDate birthDate, String parent, 
                            LocalTime arrivalTime, LocalTime departureTime) {
        this.childId = childId;
        this.name = new SimpleStringProperty(name);
        
        // Calculate age more precisely
        int years = Period.between(birthDate, LocalDate.now()).getYears();
        int months = Period.between(birthDate, LocalDate.now()).getMonths() % 12;
        String ageText = years > 0 ? years + " years" : months + " months";
        this.age = new SimpleStringProperty(ageText);
        
        this.parent = new SimpleStringProperty(parent);
        this.arrivalTime = new SimpleObjectProperty<>(arrivalTime);
        this.departureTime = new SimpleObjectProperty<>(departureTime);
        this.editable = new SimpleBooleanProperty(true);
    }

    // Property getters (for JavaFX binding)
    public int getChildId() { 
        return childId; 
    }
    
    public String getName() { 
        return name.get(); 
    }
    public StringProperty nameProperty() { 
        return name; 
    }
    public void setName(String name) { 
        this.name.set(name); 
    }
    
    public String getAge() { 
        return age.get(); 
    }
    public StringProperty ageProperty() { 
        return age; 
    }
    
    public String getParent() { 
        return parent.get(); 
    }
    public StringProperty parentProperty() { 
        return parent; 
    }
    public void setParent(String parent) { 
        this.parent.set(parent); 
    }
    
    public LocalTime getArrivalTime() { 
        return arrivalTime.get(); 
    }
    public ObjectProperty<LocalTime> arrivalTimeProperty() { 
        return arrivalTime; 
    }
    public void setArrivalTime(LocalTime arrivalTime) { 
        this.arrivalTime.set(arrivalTime); 
    }
    
    public LocalTime getDepartureTime() { 
        return departureTime.get(); 
    }
    public ObjectProperty<LocalTime> departureTimeProperty() { 
        return departureTime; 
    }
    public void setDepartureTime(LocalTime departureTime) { 
        this.departureTime.set(departureTime); 
    }
    
    public boolean isEditable() { 
        return editable.get(); 
    }
    public BooleanProperty editableProperty() { 
        return editable; 
    }
    public void setEditable(boolean editable) { 
        this.editable.set(editable); 
    }

    // Helper method to check if child is present
    public boolean isPresent() {
        return arrivalTime.get() != null;
    }

    // Helper method to get total hours present (if both arrival and departure are set)
    public double getHoursPresent() {
        if (arrivalTime.get() != null && departureTime.get() != null) {
            return java.time.Duration.between(arrivalTime.get(), departureTime.get()).toMinutes() / 60.0;
        }
        return 0.0;
    }

    @Override
    public String toString() {
        return String.format("ChildAttendanceRow{id=%d, name='%s', arrival=%s, departure=%s}", 
                           childId, getName(), getArrivalTime(), getDepartureTime());
    }
}