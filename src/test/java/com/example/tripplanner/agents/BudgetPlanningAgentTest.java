package com.example.tripplanner.agents;

import com.embabel.agent.api.common.Ai;
import com.embabel.agent.api.common.OperationContext;
import com.embabel.agent.api.common.PromptRunner;
import com.example.tripplanner.domain.BudgetBreakdown;
import com.example.tripplanner.domain.TravelRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BudgetPlanningAgentTest {

    private final OperationContext context = mock(OperationContext.class);
    private final Ai ai = mock(Ai.class);
    private final PromptRunner promptRunner = mock(PromptRunner.class);

    private final BudgetPlanningAgent agent = new BudgetPlanningAgent();

    @BeforeEach
    void wireLlmChain() {
        when(context.ai()).thenReturn(ai);
        when(ai.withDefaultLlm()).thenReturn(promptRunner);
    }

    @Test
    void planAsksDefaultLlmForBudgetBreakdown() {
        TravelRequest request = new TravelRequest("Kyoto, Japan", 4, 2500, "temples, food");
        BudgetBreakdown expected = new BudgetBreakdown();
        when(promptRunner.createObject(anyString(), eq(BudgetBreakdown.class))).thenReturn(expected);

        BudgetBreakdown actual = agent.plan(request, context);

        assertThat(actual).isSameAs(expected);
    }

    @Test
    void promptContainsDestinationTripLengthAndInterests() {
        TravelRequest request = new TravelRequest("Kyoto, Japan", 4, 2500, "temples, food");
        when(promptRunner.createObject(anyString(), eq(BudgetBreakdown.class)))
                .thenReturn(new BudgetBreakdown());

        agent.plan(request, context);

        ArgumentCaptor<String> prompt = ArgumentCaptor.forClass(String.class);
        verify(promptRunner).createObject(prompt.capture(), eq(BudgetBreakdown.class));
        assertThat(prompt.getValue())
                .contains("Kyoto, Japan")
                .contains("4 days")
                .contains("temples, food");
    }
}
