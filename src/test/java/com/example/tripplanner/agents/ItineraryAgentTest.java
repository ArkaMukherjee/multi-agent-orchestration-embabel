package com.example.tripplanner.agents;

import com.embabel.agent.api.common.Ai;
import com.embabel.agent.api.common.OperationContext;
import com.embabel.agent.api.common.PromptRunner;
import com.example.tripplanner.domain.BudgetBreakdown;
import com.example.tripplanner.domain.DestinationResearch;
import com.example.tripplanner.domain.TravelRequest;
import com.example.tripplanner.domain.TripPlan;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ItineraryAgentTest {

    private final OperationContext context = mock(OperationContext.class);
    private final Ai ai = mock(Ai.class);
    private final PromptRunner promptRunner = mock(PromptRunner.class);

    private final ItineraryAgent agent = new ItineraryAgent();

    private final TravelRequest request = new TravelRequest("Kyoto, Japan", 4, 2500, "temples, food");
    private final DestinationResearch research = new DestinationResearch();
    private final BudgetBreakdown budget = new BudgetBreakdown();

    @BeforeEach
    void wireLlmChain() {
        when(context.ai()).thenReturn(ai);
        when(ai.withDefaultLlm()).thenReturn(promptRunner);

        research.setDestination("Kyoto, Japan");
        research.setTopAttractions(List.of("Fushimi Inari", "Arashiyama"));
        research.setBestTimeToVisit("Spring");
        research.setLocalTips(List.of("Buy an IC card"));

        budget.setTotalUsd(2500);
        budget.setAccommodationUsd(1000);
        budget.setFoodUsd(600);
        budget.setActivitiesUsd(500);
        budget.setTransportUsd(400);
        budget.setNotes("Mid-range hotels");
    }

    @Test
    void buildItineraryAsksDefaultLlmForTripPlan() {
        TripPlan expected = new TripPlan();
        when(promptRunner.createObject(anyString(), eq(TripPlan.class))).thenReturn(expected);

        TripPlan actual = agent.buildItinerary(request, research, budget, context);

        assertThat(actual).isSameAs(expected);
    }

    @Test
    void promptCombinesRequestResearchAndBudget() {
        when(promptRunner.createObject(anyString(), eq(TripPlan.class))).thenReturn(new TripPlan());

        agent.buildItinerary(request, research, budget, context);

        ArgumentCaptor<String> prompt = ArgumentCaptor.forClass(String.class);
        verify(promptRunner).createObject(prompt.capture(), eq(TripPlan.class));
        assertThat(prompt.getValue())
                .contains("Kyoto, Japan")                     // from the request
                .contains("Fushimi Inari", "Arashiyama")      // from the research
                .contains("Spring", "Buy an IC card")
                .contains("Mid-range hotels")                 // from the budget
                .contains("temples, food");
    }
}
