package com.example.tripplanner.agents;

import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.OperationContext;
import com.example.tripplanner.domain.DestinationResearch;
import com.example.tripplanner.domain.TravelRequest;

/**
 * Agent #1 in the orchestration.
 *
 * <p>Consumes a {@link TravelRequest} and produces {@link DestinationResearch}.
 * It has no dependency on the budget agent, so Embabel's planner is free to run
 * it in parallel with {@code BudgetPlanningAgent} — both only need the initial
 * {@code TravelRequest}.
 */
@Agent(description = "Researches a travel destination: top attractions, best time to visit, and local tips")
public class DestinationResearchAgent {

    @Action(description = "Research the destination for the requested interests")
    public DestinationResearch research(TravelRequest request, OperationContext context) {
        String prompt = """
                You are a seasoned travel researcher.
                Research the destination below and return concise, practical findings.

                Destination: %s
                Trip length: %d days
                Traveler interests: %s

                Provide:
                - topAttractions: 5-8 attractions matching the interests
                - bestTimeToVisit: one short sentence
                - localTips: 3-5 practical tips (transport, etiquette, safety, money)
                """.formatted(request.destination(), request.days(), request.interests());

        return context.ai()
                .withDefaultLlm()
                .createObject(prompt, DestinationResearch.class);
    }
}
