package com.example.tripplanner.web;

import com.embabel.agent.api.common.TypedOps;
import com.embabel.agent.core.AgentPlatform;
import com.embabel.agent.core.ProcessOptions;
import com.example.tripplanner.domain.TravelRequest;
import com.example.tripplanner.domain.TripPlan;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TripPlannerControllerTest {

    @Test
    void planDelegatesToTypedOpsAskingOnlyForTheGoalType() {
        TripPlannerController controller = new TripPlannerController(mock(AgentPlatform.class));
        // The controller builds its own AgentPlatformTypedOps facade; swap in a
        // mock so the test exercises the delegation without a live platform.
        TypedOps typedOps = mock(TypedOps.class);
        ReflectionTestUtils.setField(controller, "typedOps", typedOps);

        TravelRequest request = new TravelRequest("Kyoto, Japan", 4, 2500, "temples, food");
        TripPlan expected = new TripPlan();
        when(typedOps.transform(request, TripPlan.class, ProcessOptions.DEFAULT)).thenReturn(expected);

        TripPlan actual = controller.plan(request);

        assertThat(actual).isSameAs(expected);
    }

    @Test
    void constructorWrapsThePlatformInTypedOps() {
        TripPlannerController controller = new TripPlannerController(mock(AgentPlatform.class));

        assertThat(ReflectionTestUtils.getField(controller, "typedOps")).isNotNull();
    }
}
