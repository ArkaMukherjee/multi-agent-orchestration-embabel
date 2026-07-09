package com.example.tripplanner.domain;

import java.util.List;

/**
 * Output of the {@code DestinationResearchAgent}. Produced from a
 * {@link TravelRequest} and later consumed by the {@code ItineraryAgent}.
 *
 * <p>Mutable POJO rather than a record so Jackson tolerates duplicate JSON keys
 * from local Ollama models (last occurrence wins instead of a binding error).
 */
public class DestinationResearch {

    private String destination;
    private List<String> topAttractions;
    private String bestTimeToVisit;
    private List<String> localTips;

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public List<String> getTopAttractions() {
        return topAttractions;
    }

    public void setTopAttractions(List<String> topAttractions) {
        this.topAttractions = topAttractions;
    }

    public String getBestTimeToVisit() {
        return bestTimeToVisit;
    }

    public void setBestTimeToVisit(String bestTimeToVisit) {
        this.bestTimeToVisit = bestTimeToVisit;
    }

    public List<String> getLocalTips() {
        return localTips;
    }

    public void setLocalTips(List<String> localTips) {
        this.localTips = localTips;
    }

    @Override
    public String toString() {
        return "DestinationResearch[destination=%s, topAttractions=%s, bestTimeToVisit=%s, localTips=%s]"
                .formatted(destination, topAttractions, bestTimeToVisit, localTips);
    }
}
