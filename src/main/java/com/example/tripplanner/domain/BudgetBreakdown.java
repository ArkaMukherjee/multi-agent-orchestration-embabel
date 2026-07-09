package com.example.tripplanner.domain;

/**
 * Output of the {@code BudgetPlanningAgent}. Produced from a
 * {@link TravelRequest} and later consumed by the {@code ItineraryAgent}.
 *
 * <p>Mutable POJO rather than a record so Jackson tolerates duplicate JSON keys
 * from local Ollama models (last occurrence wins instead of a binding error).
 */
public class BudgetBreakdown {

    private double totalUsd;
    private double accommodationUsd;
    private double foodUsd;
    private double activitiesUsd;
    private double transportUsd;
    private String notes;

    public double getTotalUsd() {
        return totalUsd;
    }

    public void setTotalUsd(double totalUsd) {
        this.totalUsd = totalUsd;
    }

    public double getAccommodationUsd() {
        return accommodationUsd;
    }

    public void setAccommodationUsd(double accommodationUsd) {
        this.accommodationUsd = accommodationUsd;
    }

    public double getFoodUsd() {
        return foodUsd;
    }

    public void setFoodUsd(double foodUsd) {
        this.foodUsd = foodUsd;
    }

    public double getActivitiesUsd() {
        return activitiesUsd;
    }

    public void setActivitiesUsd(double activitiesUsd) {
        this.activitiesUsd = activitiesUsd;
    }

    public double getTransportUsd() {
        return transportUsd;
    }

    public void setTransportUsd(double transportUsd) {
        this.transportUsd = transportUsd;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public String toString() {
        return "BudgetBreakdown[totalUsd=%s, accommodationUsd=%s, foodUsd=%s, activitiesUsd=%s, transportUsd=%s, notes=%s]"
                .formatted(totalUsd, accommodationUsd, foodUsd, activitiesUsd, transportUsd, notes);
    }
}
