

package model.frontend;

import java.util.Collections;
import java.util.List;

public class FrontendSearchResponse {
    private List<SearchResultInfo> searchResults = Collections.emptyList();
    private String documentsLocation = "";

    public FrontendSearchResponse(List<SearchResultInfo> searchResults, String documentsLocation) {
        this.searchResults = searchResults;
        this.documentsLocation = documentsLocation;
    }

    public List<SearchResultInfo> getSearchResults() {
        return searchResults;
    }

    public String getDocumentsLocation() {
        return documentsLocation;
    }

    public static class SearchResultInfo {
        private String title;
        private String extension;
        private int score;

        public SearchResultInfo(String title, String extension, int score) {
            this.title = title;
            this.extension = extension;
            this.score = score;
        }

        public String getTitle() {
            return title;
        }

        public String getExtension() {
            return extension;
        }

        public int getScore() {
            return score;
        }
    }
}
