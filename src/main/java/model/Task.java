package model;
import java.io.Serializable;
import java.util.*;

//This is a task assigned to each worker node, which contains a subset of documents and a list of search terms
//This is a Map Reduce where the mapping is using TFIDF
public class Task implements Serializable{
    private final List<String> searchTerms;
    private final List<String> documents;
    public Task(List<String> searchTerms,List<String>documents){
        this.searchTerms = searchTerms;
        this.documents = documents;
    }
    public List<String> getSearchTerms(){
        return Collections.unmodifiableList(searchTerms);
    }
    public List<String> getDocuments(){
        return Collections.unmodifiableList(documents);
    }
}
