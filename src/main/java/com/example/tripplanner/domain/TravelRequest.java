package com.example.tripplanner.domain;

/**
 * The starting input for the trip-planning flow. Supplied by the caller; the
 * Embabel planner uses its presence on the blackboard to trigger the agents
 * that consume it (destination research and budget planning).
 *
 * @param destination free-text destination, e.g. "Tokyo, Japan"
 * @param days        number of days for the trip
 * @param budgetUsd   total budget in US dollars
 * @param interests   comma-separated interests, e.g. "food, temples, hiking"
 */
public record TravelRequest(
        String destination,
        int days,
        double budgetUsd,
        String interests
) {}
