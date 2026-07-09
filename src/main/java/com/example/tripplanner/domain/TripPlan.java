package com.example.tripplanner.domain;

import java.util.List;

/**
 * The final artifact of the flow. Produced by the {@code ItineraryAgent}, whose
 * producing action is annotated {@code @AchievesGoal}. When Embabel's planner
 * can construct a {@code TripPlan}, the goal is satisfied and the flow ends.
 *
 * <p>Mutable POJO rather than a record so Jackson tolerates duplicate JSON keys
 * from local Ollama models (last occurrence wins instead of a binding error).
 */
public class TripPlan {

    private String destination;
    private int days;
    private String summary;
    private List<DayPlan> itinerary;
    private BudgetBreakdown budget;

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public int getDays() {
        return days;
    }

    public void setDays(int days) {
        this.days = days;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public List<DayPlan> getItinerary() {
        return itinerary;
    }

    public void setItinerary(List<DayPlan> itinerary) {
        this.itinerary = itinerary;
    }

    public BudgetBreakdown getBudget() {
        return budget;
    }

    public void setBudget(BudgetBreakdown budget) {
        this.budget = budget;
    }

    @Override
    public String toString() {
        return "TripPlan[destination=%s, days=%d, summary=%s, itinerary=%s, budget=%s]"
                .formatted(destination, days, summary, itinerary, budget);
    }
}
