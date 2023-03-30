package search;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;
import java.util.stream.Collectors;

import model.SerializationUtils;
import model.*;
import networking.OnRequestCallback;

public class SearchWorker implements OnRequestCallback{
    private static final String ENDPOINT = "/task";
    @Override
    public byte[] handleRequest(byte[] requestPayload) {
        Task task = (Task)SerializationUtils.deserialize(requestPayload);
        Result result = createResult(task);
        return SerializationUtils.serialize(result);
        
        // throw new UnsupportedOperationException("Unimplemented method 'handleRequest'");
    }
    //
    private Result createResult(Task task){
        List<String> documents = task.getDocuments();
        Result result = new Result();
        for(String document:documents){
            List<String> words = parseWordsFromDocument(document);
            DocumentData docdata = TFIDF.createDocumentData(words, task.getSearchTerms());
            result.addDocumentData(document, docdata);
        }
        return result;
    }
    private List<String> parseWordsFromDocument(String document) {
        try {
            FileReader fr = new FileReader(document);
            BufferedReader bf=new BufferedReader(fr);
            List<String> lines = bf.lines().collect(Collectors.toList());
            List<String> words = TFIDF.getWordsFromDocument(lines);
            return words;
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }


    @Override
    public String getEndpoint() {
        // TODO Auto-generated method stub
        return ENDPOINT;
    }
    
}
