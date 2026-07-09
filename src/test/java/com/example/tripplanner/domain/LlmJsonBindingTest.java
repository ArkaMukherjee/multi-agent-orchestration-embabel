package com.example.tripplanner.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Regression tests for binding raw LLM output to the domain types.
 *
 * <p>Local Ollama models occasionally emit duplicate keys within one JSON
 * object. When these types were records, that failed hard with Jackson's
 * "Should never call set() on setterless property". As mutable POJOs, the
 * last occurrence must win instead.
 */
class LlmJsonBindingTest {

    /** Built the same way Spring Boot builds the ObjectMapper Embabel uses. */
    private final ObjectMapper mapper = Jackson2ObjectMapperBuilder.json().build();

    @Test
    void bindsWellFormedLlmOutput() throws Exception {
        String json = """
                {"destination":"Kyoto, Japan","days":1,"summary":"Short trip",
                 "itinerary":[{"day":1,"theme":"Temples","activities":["Fushimi Inari","Kinkaku-ji"]}],
                 "budget":{"totalUsd":1500,"accommodationUsd":500,"foodUsd":400,
                           "activitiesUsd":300,"transportUsd":300,"notes":"Mid-range"}}
                """;

        TripPlan plan = mapper.readValue(json, TripPlan.class);

        assertThat(plan.getDestination()).isEqualTo("Kyoto, Japan");
        assertThat(plan.getDays()).isEqualTo(1);
        assertThat(plan.getItinerary()).hasSize(1);
        assertThat(plan.getItinerary().get(0).getActivities())
                .containsExactly("Fushimi Inari", "Kinkaku-ji");
        assertThat(plan.getBudget().getTotalUsd()).isEqualTo(1500);
    }

    @Test
    void toleratesDuplicateKeysInLlmOutputLastValueWins() throws Exception {
        String json = """
                {"destination":"Kyoto, Japan","days":1,"summary":"s",
                 "itinerary":[{"day":1,"theme":"t","activities":["first"],"activities":["second"]}],
                 "budget":null}
                """;

        TripPlan plan = mapper.readValue(json, TripPlan.class);

        assertThat(plan.getItinerary().get(0).getActivities()).containsExactly("second");
    }

    @Test
    void toleratesDuplicateKeysOnEveryLlmProducedType() throws Exception {
        DestinationResearch research = mapper.readValue("""
                {"destination":"Kyoto","topAttractions":["a"],"topAttractions":["b"],
                 "bestTimeToVisit":"Spring","localTips":["x"]}
                """, DestinationResearch.class);
        assertThat(research.getTopAttractions()).containsExactly("b");

        BudgetBreakdown budget = mapper.readValue("""
                {"totalUsd":100,"totalUsd":200,"accommodationUsd":50,"foodUsd":50,
                 "activitiesUsd":50,"transportUsd":50,"notes":"n"}
                """, BudgetBreakdown.class);
        assertThat(budget.getTotalUsd()).isEqualTo(200);
    }

    @Test
    void serializesTripPlanForHttpResponse() throws Exception {
        TripPlan plan = new TripPlan();
        plan.setDestination("Kyoto, Japan");
        plan.setDays(2);

        String json = mapper.writeValueAsString(plan);

        assertThat(json).contains("\"destination\":\"Kyoto, Japan\"", "\"days\":2");
    }
}
