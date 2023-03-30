

package model.frontend;

public class FrontendSearchRequest {
    private String searchQuery;
    private long maxNumberOfResults = Long.MAX_VALUE;
    private double minScore = 0.0;

    public String getSearchQuery() {
        return searchQuery;
    }

    public long getMaxNumberOfResults() {
        return maxNumberOfResults;
    }

    public double getMinScore() {
        return minScore;
    }
}
