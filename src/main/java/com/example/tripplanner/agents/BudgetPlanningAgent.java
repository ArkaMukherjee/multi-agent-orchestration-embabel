package com.example.tripplanner.agents;

import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.OperationContext;
import com.example.tripplanner.domain.BudgetBreakdown;
import com.example.tripplanner.domain.TravelRequest;

/**
 * Agent #2 in the orchestration.
 *
 * <p>Consumes a {@link TravelRequest} and produces a {@link BudgetBreakdown}.
 * Independent of the research agent — the planner may schedule this concurrently.
 */
@Agent(description = "Breaks a total travel budget into accommodation, food, activities, and transport")
public class BudgetPlanningAgent {

    @Action(description = "Allocate the total budget across categories for the trip")
    public BudgetBreakdown plan(TravelRequest request, OperationContext context) {
        String prompt = """
                You are a travel budgeting expert.
                Split the total budget into realistic category allocations for this trip.

                Destination: %s
                Trip length: %d days
                Total budget (USD): %.2f
                Interests: %s

                Return a BudgetBreakdown where accommodationUsd + foodUsd +
                activitiesUsd + transportUsd sums to roughly the total budget.
                Put any assumptions (e.g. mid-range hotels, public transport) in 'notes'.
                """.formatted(
                request.destination(), request.days(), request.budgetUsd(), request.interests());

        return context.ai()
                .withDefaultLlm()
                .createObject(prompt, BudgetBreakdown.class);
    }
}
