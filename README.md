# Embabel Trip Planner — Multi-Agent Orchestration (Spring AI + Embabel)

A minimal, runnable sample showing **multi-agent orchestration on the JVM** with
[Embabel](https://github.com/embabel/embabel-agent) (Rod Johnson's GOAP-based
agent framework) sitting on top of **Spring AI** for the LLM plumbing.

Three independent agents collaborate to turn a `TravelRequest` into a complete
`TripPlan` — **without any hand-written control flow**. Embabel's planner figures
out the order from the data dependencies between the agents' actions.

## The orchestration

```
                         TravelRequest  (input)
                          /            \
          (parallel)     /              \
   DestinationResearchAgent        BudgetPlanningAgent
        research()                     plan()
             |                              |
      DestinationResearch            BudgetBreakdown
              \                          /
               \                        /
                \                      /
                  ItineraryAgent.buildItinerary()   @AchievesGoal
                              |
                          TripPlan  (goal)
```

- **`DestinationResearchAgent`** — `TravelRequest` → `DestinationResearch`
- **`BudgetPlanningAgent`** — `TravelRequest` → `BudgetBreakdown`
- **`ItineraryAgent`** — `TravelRequest` + `DestinationResearch` + `BudgetBreakdown`
  → `TripPlan`, annotated `@AchievesGoal`

The first two agents depend only on the initial input, so the planner can run
them **in parallel**. The third depends on both of their outputs, so it runs
**last**. That dependency graph — expressed purely as method parameter/return
types — *is* the orchestration. No `if/else`, no explicit pipeline.

### How Embabel decides what to run
When the controller asks for a `TripPlan`, Embabel's **GOAP planner** works
backwards from that goal type: `TripPlan` needs a `DestinationResearch` and a
`BudgetBreakdown`; each of those needs a `TravelRequest`, which is supplied.
The planner composes the actions that bridge input → goal and executes them.

## Requirements

- **Java 21+** (this project targets 21; tested toolchains: JDK 21–24)
- **Maven 3.9+**
- **Embabel 0.4.0**, **Spring Boot 3.5.x** (declared in `pom.xml`)
- A local **[Ollama](https://ollama.com)** server to run live (no API key needed)

## Configure

```bash
# Install Ollama (https://ollama.com/download), then pull the model:
ollama pull llama3.2
# Make sure the server is running (default http://localhost:11434):
ollama serve
```

Model and temperature are set in `src/main/resources/application.yml`
(default `llama3.2`, served locally by Ollama).

## Build

```bash
mvn clean package
```

## Run

```bash
mvn spring-boot:run
# or
java -jar target/embabel-trip-planner-1.0.0.jar
```

The app starts on `http://localhost:8080`.

## Try it

```bash
curl -X POST http://localhost:8080/api/trips/plan \
  -H "Content-Type: application/json" \
  -d '{
        "destination": "Kyoto, Japan",
        "days": 4,
        "budgetUsd": 2500,
        "interests": "temples, food, gardens, photography"
      }'
```

You get back a JSON `TripPlan` with a per-day itinerary and the budget breakdown,
assembled by the three cooperating agents.

## Project layout

```
embabel-trip-planner/
├── pom.xml
├── README.md
└── src/main/
    ├── java/com/example/tripplanner/
    │   ├── TripPlannerApplication.java        # @SpringBootApplication @EnableAgents
    │   ├── agents/
    │   │   ├── DestinationResearchAgent.java   # @Agent  -> DestinationResearch
    │   │   ├── BudgetPlanningAgent.java         # @Agent  -> BudgetBreakdown
    │   │   └── ItineraryAgent.java              # @Agent  -> TripPlan (@AchievesGoal)
    │   ├── domain/                              # types passed between agents
    │   │   ├── TravelRequest.java
    │   │   ├── DestinationResearch.java
    │   │   ├── BudgetBreakdown.java
    │   │   ├── DayPlan.java
    │   │   └── TripPlan.java
    │   └── web/
    │       └── TripPlannerController.java       # asks platform for TripPlan
    └── resources/
        └── application.yml
```

## Key ideas to take away

- **Agents are plain Spring beans** annotated `@Agent`; their capabilities are
  `@Action` methods.
- **Types are the contract.** An action's parameters are its preconditions and
  its return type is its effect — the planner chains actions by matching them.
- **The goal is a type.** `@AchievesGoal` marks the terminal action; callers ask
  for the output type and stay ignorant of the steps.
- **Swappable LLM.** `context.ai().withDefaultLlm().createObject(prompt, T.class)`
  goes through Spring AI, so switching OpenAI ↔ Anthropic ↔ Ollama is config-only.

## References

- Embabel Agent — https://github.com/embabel/embabel-agent
- Embabel examples (Java & Kotlin) — https://github.com/embabel/embabel-agent-examples
- User guide — https://docs.embabel.com/embabel-agent/guide/
