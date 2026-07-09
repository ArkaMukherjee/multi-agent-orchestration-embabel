package com.example.tripplanner;

import com.embabel.agent.config.annotation.EnableAgents;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Boots Spring and turns on Embabel.
 *
 * <p>{@code @EnableAgents} makes Embabel scan the context for {@code @Agent}
 * beans, register every {@code @Action}/{@code @AchievesGoal}, and stand up the
 * {@code AgentPlatform} + GOAP planner that orchestrates them at runtime.
 */
@SpringBootApplication
@EnableAgents
public class TripPlannerApplication {
    public static void main(String[] args) {
        SpringApplication.run(TripPlannerApplication.class, args);
    }
}

