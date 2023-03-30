package model;
import java.io.Serializable;
import java.util.*;
public class Result implements Serializable{
    private HashMap<String,DocumentData> documentToDocumentData = new HashMap<>();
    public void addDocumentData(String document,DocumentData docdata){
        this.documentToDocumentData.put(document, docdata);
    }
    public HashMap<String,DocumentData> getDocumentToDocumentData(){
        return (HashMap<String, DocumentData>) Collections.unmodifiableMap(documentToDocumentData);
    }
}
