# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

A minimal demo of multi-agent orchestration on the JVM using [Embabel](https://github.com/embabel/embabel-agent) 0.4.0 (GOAP-based agent framework) on top of Spring AI / Spring Boot 3.5.x. Java 21, Maven.

## Commands

```bash
mvn clean package          # build
mvn spring-boot:run        # run (starts on http://localhost:8080)
mvn test                   # run tests (spring-boot-starter-test is on the classpath; no tests exist yet)
mvn test -Dtest=ClassName  # run a single test class
```

Running live requires a local [Ollama](https://ollama.com) server on `http://localhost:11434` with the model pulled (`ollama pull llama3.2`) — no API key needed. Model selection lives in `src/main/resources/application.yml` (`embabel.models.default-llm`, default `llama3.2`); the Spring AI Ollama starter is pinned to the same Spring AI version (1.1.5) the Embabel starter pulls transitively.

Exercise the flow:

```bash
curl -X POST http://localhost:8080/api/trips/plan \
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
- Changing a domain record's shape changes the orchestration graph; the planner matches on exact types.
- Swapping LLM provider (OpenAI ↔ Anthropic ↔ Ollama) is config-only via `application.yml`; agent code goes through `withDefaultLlm()`.

Note: `config/AgentsConfig.java` is an empty file (no code at all) and `README.md`'s project-layout tree omits it — it can be ignored or deleted.