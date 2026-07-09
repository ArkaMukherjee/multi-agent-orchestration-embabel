package com.example.tripplanner.web;

import com.embabel.agent.api.common.AgentPlatformTypedOps;
import com.embabel.agent.api.common.TypedOps;
import com.embabel.agent.core.AgentPlatform;
import com.embabel.agent.core.ProcessOptions;
import com.example.tripplanner.domain.TravelRequest;
import com.example.tripplanner.domain.TripPlan;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Triggers the multi-agent flow over HTTP.
 *
 * <p>Note what this controller does <em>not</em> do: it never names the three
 * agents or wires a sequence. It hands a {@link TravelRequest} to the platform
 * and asks only for a {@link TripPlan}. Embabel's planner works backwards from
 * the goal type, discovers that {@code TripPlan} needs {@code DestinationResearch}
 * and {@code BudgetBreakdown} (each derivable from the {@code TravelRequest}),
 * and orchestrates the three agents to satisfy those dependencies.
 *
 * <p>{@link TypedOps#transform} is Embabel 0.4.0's type-safe façade over the
 * {@link AgentPlatform}: given an input object and a desired output type, it
 * plans and runs the agents that bridge the two.
 */
@RestController
@RequestMapping("/api/trips")
public class TripPlannerController {

    private final TypedOps typedOps;

    public TripPlannerController(AgentPlatform agentPlatform) {
        this.typedOps = new AgentPlatformTypedOps(agentPlatform);
    }

    @PostMapping("/plan")
    public TripPlan plan(@RequestBody TravelRequest request) {
        return typedOps.transform(request, TripPlan.class, ProcessOptions.DEFAULT);
    }
}
