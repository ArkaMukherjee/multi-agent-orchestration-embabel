package com.example.tripplanner.domain;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** State round-trip tests for the domain types exchanged between agents. */
class DomainModelTest {

    @Test
    void travelRequestRecordExposesComponents() {
        TravelRequest request = new TravelRequest("Kyoto, Japan", 4, 2500, "temples, food");

        assertThat(request.destination()).isEqualTo("Kyoto, Japan");
        assertThat(request.days()).isEqualTo(4);
        assertThat(request.budgetUsd()).isEqualTo(2500);
        assertThat(request.interests()).isEqualTo("temples, food");
    }

    @Test
    void dayPlanHoldsState() {
        DayPlan dayPlan = new DayPlan();
        dayPlan.setDay(1);
        dayPlan.setTheme("Temple Hopping");
        dayPlan.setActivities(List.of("Fushimi Inari", "Kinkaku-ji"));

        assertThat(dayPlan.getDay()).isEqualTo(1);
        assertThat(dayPlan.getTheme()).isEqualTo("Temple Hopping");
        assertThat(dayPlan.getActivities()).containsExactly("Fushimi Inari", "Kinkaku-ji");
        assertThat(dayPlan.toString()).contains("Temple Hopping", "Fushimi Inari");
    }

    @Test
    void tripPlanHoldsState() {
        DayPlan dayPlan = new DayPlan();
        dayPlan.setDay(1);

        BudgetBreakdown budget = new BudgetBreakdown();
        budget.setTotalUsd(2500);

        TripPlan tripPlan = new TripPlan();
        tripPlan.setDestination("Kyoto, Japan");
        tripPlan.setDays(4);
        tripPlan.setSummary("A relaxing trip");
        tripPlan.setItinerary(List.of(dayPlan));
        tripPlan.setBudget(budget);

        assertThat(tripPlan.getDestination()).isEqualTo("Kyoto, Japan");
        assertThat(tripPlan.getDays()).isEqualTo(4);
        assertThat(tripPlan.getSummary()).isEqualTo("A relaxing trip");
        assertThat(tripPlan.getItinerary()).containsExactly(dayPlan);
        assertThat(tripPlan.getBudget()).isSameAs(budget);
        assertThat(tripPlan.toString()).contains("Kyoto, Japan", "A relaxing trip");
    }

    @Test
    void destinationResearchHoldsState() {
        DestinationResearch research = new DestinationResearch();
        research.setDestination("Kyoto, Japan");
        research.setTopAttractions(List.of("Fushimi Inari", "Arashiyama"));
        research.setBestTimeToVisit("Spring");
        research.setLocalTips(List.of("Buy an IC card"));

        assertThat(research.getDestination()).isEqualTo("Kyoto, Japan");
        assertThat(research.getTopAttractions()).containsExactly("Fushimi Inari", "Arashiyama");
        assertThat(research.getBestTimeToVisit()).isEqualTo("Spring");
        assertThat(research.getLocalTips()).containsExactly("Buy an IC card");
        assertThat(research.toString()).contains("Kyoto, Japan", "Spring");
    }

    @Test
    void budgetBreakdownHoldsState() {
        BudgetBreakdown budget = new BudgetBreakdown();
        budget.setTotalUsd(2500);
        budget.setAccommodationUsd(1000);
        budget.setFoodUsd(600);
        budget.setActivitiesUsd(500);
        budget.setTransportUsd(400);
        budget.setNotes("Mid-range hotels");

        assertThat(budget.getTotalUsd()).isEqualTo(2500);
        assertThat(budget.getAccommodationUsd()).isEqualTo(1000);
        assertThat(budget.getFoodUsd()).isEqualTo(600);
        assertThat(budget.getActivitiesUsd()).isEqualTo(500);
        assertThat(budget.getTransportUsd()).isEqualTo(400);
        assertThat(budget.getNotes()).isEqualTo("Mid-range hotels");
        assertThat(budget.toString()).contains("2500", "Mid-range hotels");
    }
}
