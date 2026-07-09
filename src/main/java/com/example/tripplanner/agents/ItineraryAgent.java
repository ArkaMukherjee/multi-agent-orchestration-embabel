package com.example.tripplanner.agents;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.OperationContext;
import com.example.tripplanner.domain.BudgetBreakdown;
import com.example.tripplanner.domain.DestinationResearch;
import com.example.tripplanner.domain.TravelRequest;
import com.example.tripplanner.domain.TripPlan;

/**
 * Agent #3 — the synthesizer that closes the flow.
 *
 * <p>Its action depends on THREE objects: the original {@link TravelRequest}
 * plus the outputs of the other two agents ({@link DestinationResearch} and
 * {@link BudgetBreakdown}). Because those objects are declared as parameters,
 * Embabel's GOAP planner knows this action can only run after both upstream
 * agents have produced their results — that data-dependency IS the orchestration.
 *
 * <p>The {@code @AchievesGoal} annotation marks {@link TripPlan} as the flow's
 * goal: once this method returns, planning is complete.
 */
@Agent(description = "Synthesizes destination research and a budget into a day-by-day trip plan")
public class ItineraryAgent {

    @Action(description = "Combine research and budget into a day-by-day itinerary")
    @AchievesGoal(description = "A complete, budget-aware day-by-day trip plan")
    public TripPlan buildItinerary(
            TravelRequest request,
            DestinationResearch research,
            BudgetBreakdown budget,
            OperationContext context) {

        String prompt = """
                You are an expert trip planner. Using the research and budget below,
                produce a %d-day itinerary for %s.

                RESEARCH
                Attractions: %s
                Best time to visit: %s
                Local tips: %s

                BUDGET (USD)
                Total: %.2f | Accommodation: %.2f | Food: %.2f | Activities: %.2f | Transport: %.2f
                Notes: %s

                Traveler interests: %s

                Produce a TripPlan with:
                - one DayPlan per day (day number, a short theme, 3-5 activities)
                - activities that respect the interests and stay within the activity budget
                - a 2-3 sentence 'summary' of the overall trip
                Echo the provided budget back in the 'budget' field.
                """.formatted(
                request.days(), request.destination(),
                research.getTopAttractions(), research.getBestTimeToVisit(), research.getLocalTips(),
                budget.getTotalUsd(), budget.getAccommodationUsd(), budget.getFoodUsd(),
                budget.getActivitiesUsd(), budget.getTransportUsd(), budget.getNotes(),
                request.interests());

        return context.ai()
                .withDefaultLlm()
                .createObject(prompt, TripPlan.class);
    }
}
