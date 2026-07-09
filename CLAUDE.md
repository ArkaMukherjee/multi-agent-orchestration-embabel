# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

A minimal demo of multi-agent orchestration on the JVM using [Embabel](https://github.com/embabel/embabel-agent) 0.4.0 (GOAP-based agent framework) on top of Spring AI / Spring Boot 3.5.x. Java 21, Maven.

## Commands

```bash
mvn clean package          # build (fails if a running app instance locks target/*.jar — stop it first)
mvn spring-boot:run        # run (starts on http://localhost:8082)
mvn test                   # run unit tests + generate JaCoCo report (target/site/jacoco/index.html)
mvn test -Dtest=ClassName  # run a single test class
mvn verify                 # tests + 80% instruction-coverage gate (JaCoCo check) + package
docker build -t embabel-trip-planner:1.0.0 .   # multi-stage image (skips tests)
docker run --rm -p 8082:8082 embabel-trip-planner:1.0.0
```

The Docker image defaults Embabel's Ollama URL to `http://host.docker.internal:11434` (a container's `localhost` is not the host); override with the `EMBABEL_AGENT_PLATFORM_MODELS_OLLAMA_BASE_URL` env var.

Running live requires a local [Ollama](https://ollama.com) server on `http://localhost:11434` with the configured model pulled (`ollama pull llama3.1:8b`) — no API key needed. Model selection lives in `src/main/resources/application.yml` (`embabel.models.default-llm`, currently `llama3.1:8b`). Embabel registers models under the exact name Ollama reports, **including the tag** — `llama3.2` fails with "Default LLM not found" while `llama3.2:latest` works. Model discovery comes from the `embabel-agent-starter-ollama` dependency; without a provider starter, Embabel has zero models ("available models: []" at startup).

Exercise the flow (LLM calls to a local model can take minutes):

```bash
curl -X POST http://localhost:8082/api/trips/plan \
  -H "Content-Type: application/json" \
  -d '{"destination": "Kyoto, Japan", "days": 4, "budgetUsd": 2500, "interests": "temples, food, gardens"}'
```

## Architecture

The entire point of this codebase is that **orchestration is implicit** — there is no hand-written control flow. Understanding it requires seeing how three pieces fit:

1. **Agents** (`agents/`) are plain Spring beans annotated `@Agent`, with capabilities as `@Action` methods. An action's parameter types are its preconditions; its return type is its effect. Each action builds a text prompt and calls `context.ai().withDefaultLlm().createObject(prompt, T.class)` to get a structured result via Spring AI.

2. **Domain types** (`domain/`) are the contract between agents. The LLM-produced ones (`DestinationResearch`, `BudgetBreakdown`, `DayPlan`, `TripPlan`) are deliberately mutable POJOs, not records: local Ollama models sometimes emit duplicate JSON keys, which Jackson cannot bind to a record constructor ("Should never call set() on setterless property") but binds fine through setters (last occurrence wins). `TravelRequest` (HTTP input, never LLM output) stays a record. Embabel's GOAP planner chains actions purely by matching these types:
   - `DestinationResearchAgent`: `TravelRequest` → `DestinationResearch`
   - `BudgetPlanningAgent`: `TravelRequest` → `BudgetBreakdown`
   - `ItineraryAgent`: `TravelRequest` + `DestinationResearch` + `BudgetBreakdown` → `TripPlan`, annotated `@AchievesGoal` (marks the flow's terminal goal)

   The first two agents depend only on the input, so the planner can run them in parallel; the third runs last because its parameters require both outputs.

3. **Entry points**: `TripPlannerApplication` has `@EnableAgents`, which makes Embabel scan for `@Agent` beans and stand up the `AgentPlatform` + planner. `TripPlannerController` (`POST /api/trips/plan`) never names any agent — it calls `typedOps.transform(request, TripPlan.class, ProcessOptions.DEFAULT)` and the planner works backwards from the goal type to compose the actions.

Implications for changes:
- Adding a new step to the flow means adding an `@Action` whose parameter/return types slot into the dependency graph — not editing any pipeline code.
- Changing a domain type's shape changes the orchestration graph; the planner matches on exact types.
- Swapping LLM provider means swapping the Embabel provider starter in `pom.xml` (e.g. `embabel-agent-starter-openai` instead of `embabel-agent-starter-ollama`) plus its config in `application.yml`; agent code goes through `withDefaultLlm()` and never changes. Spring AI's own model starters do NOT work here — Embabel only sees models registered as its own `Llm` beans by a provider starter.

## Testing

Unit tests (`src/test/java/...`) mock the Embabel interfaces (`OperationContext` → `Ai` → `PromptRunner`) so no Ollama server is needed. `domain/LlmJsonBindingTest` is the regression suite for the duplicate-key/POJO decision above — keep it passing when touching domain types. The controller test injects a mock `TypedOps` via `ReflectionTestUtils` because the controller builds its facade internally. JaCoCo enforces 80% instruction coverage at `verify`; the Spring Boot bootstrap class and Mockito-generated classes are excluded (the latter carry the running JDK's bytecode version, which JaCoCo's ASM may not support — that's why the `prepare-agent` execution has `*MockitoMock*` excluded).

Notes:
- Startup logs show `MISSING_GOALS` validation errors for `BudgetPlanningAgent`/`DestinationResearchAgent` — harmless by design; only `ItineraryAgent` carries `@AchievesGoal`.
- `config/AgentsConfig.java` is an empty file (no code at all) — it can be ignored or deleted.