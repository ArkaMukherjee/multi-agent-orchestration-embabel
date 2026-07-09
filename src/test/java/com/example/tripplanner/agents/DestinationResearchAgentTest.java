package com.example.tripplanner.agents;

import com.embabel.agent.api.common.Ai;
import com.embabel.agent.api.common.OperationContext;
import com.embabel.agent.api.common.PromptRunner;
import com.example.tripplanner.domain.DestinationResearch;
import com.example.tripplanner.domain.TravelRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DestinationResearchAgentTest {

    private final OperationContext context = mock(OperationContext.class);
    private final Ai ai = mock(Ai.class);
    private final PromptRunner promptRunner = mock(PromptRunner.class);

    private final DestinationResearchAgent agent = new DestinationResearchAgent();

    @BeforeEach
    void wireLlmChain() {
        when(context.ai()).thenReturn(ai);
        when(ai.withDefaultLlm()).thenReturn(promptRunner);
    }

    @Test
    void researchAsksDefaultLlmForDestinationResearch() {
        TravelRequest request = new TravelRequest("Kyoto, Japan", 4, 2500, "temples, food");
        DestinationResearch expected = new DestinationResearch();
        when(promptRunner.createObject(anyString(), eq(DestinationResearch.class))).thenReturn(expected);

        DestinationResearch actual = agent.research(request, context);

        assertThat(actual).isSameAs(expected);
    }

    @Test
    void promptContainsTheTravelRequestDetails() {
        TravelRequest request = new TravelRequest("Kyoto, Japan", 4, 2500, "temples, food");
        when(promptRunner.createObject(anyString(), eq(DestinationResearch.class)))
                .thenReturn(new DestinationResearch());

        agent.research(request, context);

        ArgumentCaptor<String> prompt = ArgumentCaptor.forClass(String.class);
        org.mockito.Mockito.verify(promptRunner).createObject(prompt.capture(), eq(DestinationResearch.class));
        assertThat(prompt.getValue())
                .contains("Kyoto, Japan")
                .contains("4 days")
                .contains("temples, food");
    }
}
