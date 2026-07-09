package com.example.tripplanner.domain;

import java.util.List;

/**
 * A single day within a {@link TripPlan}.
 *
 * <p>Mutable POJO rather than a record: local Ollama models sometimes emit
 * duplicate JSON keys, which Jackson cannot bind to a record's constructor
 * ("Should never call set() on setterless property"). With setters the last
 * occurrence simply wins.
 */
public class DayPlan {

    private int day;
    private String theme;
    private List<String> activities;

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public List<String> getActivities() {
        return activities;
    }

    public void setActivities(List<String> activities) {
        this.activities = activities;
    }

    @Override
    public String toString() {
        return "DayPlan[day=%d, theme=%s, activities=%s]".formatted(day, theme, activities);
    }
}
