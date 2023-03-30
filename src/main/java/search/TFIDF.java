package search;
import model.DocumentData;

import java.util.*;
public class TFIDF {
    /*
    Calculates the frequency of a given word
    * params:
    * words list
    * term to search for
    * */
    public static double calculateTermFrequency(List<String> words,String term){
        long count=0;
        for(String word:words){
            if(word.equalsIgnoreCase(term)){
                count++;
            }
        }
        double termFrequency = (double) count/ words.size();
        return termFrequency;
    }
    public static DocumentData createDocumentData(List<String> words, List<String> terms){
        DocumentData documentData = new DocumentData();
        for(String term: terms){
            double TermFrequency = calculateTermFrequency(words,term);
            documentData.putTermFrequency(term,TermFrequency);
        }
        return documentData;
    }

    /**
     * IDF(term) = log10(N/nt)
     * N:No. of docs
     * nt:No.of documents containing the term
     */
    private static double getInverseDocumentFrequency(String term,Map<String, DocumentData> allDocumentResult){
        double nt = 0;
        for(String document: allDocumentResult.keySet()){
            DocumentData docdata = allDocumentResult.get(document);
            double termFreq = docdata.getFrequency(term);
            if(termFreq>0.0){
                nt++;
            }
        }
        return nt==0?0:Math.log10(allDocumentResult.size()/nt);
    }

    private static HashMap<String,Double> getTermToInverseDocumentFrequencyMap(List<String> terms,
                                                                           Map<String, DocumentData> allDocumentResult){
        HashMap<String,Double> map = new HashMap<>();
        for(String term:terms){
            double idf = getInverseDocumentFrequency(term,allDocumentResult);
            map.put(term,idf);
        }
        return map;
    }
    private static double calculateDocumentScore(List<String> terms,DocumentData documentData,HashMap<String,Double> termToIDF){
        double score = 0.0;
        for(String term:terms){
            double termFrequency = documentData.getFrequency(term);
            double itf = termToIDF.get(term);
            score+=termFrequency*itf;
        }
        return score;
    }
    public static Map<Double,List<String>> getDocumentsSortedByScore(List<String> terms,Map<String, DocumentData> allDocumentResult){
        TreeMap<Double,List<String>> scoreToDocuments = new TreeMap<>();
        HashMap<String,Double> termToIDFMap = getTermToInverseDocumentFrequencyMap(terms,allDocumentResult);
        for(String doc: allDocumentResult.keySet()){
            DocumentData dd = allDocumentResult.get(doc);
            double score= calculateDocumentScore(terms,dd,termToIDFMap);
            addDocumentScoreToTreeMap(scoreToDocuments,score,doc);
        }
        return scoreToDocuments.descendingMap();
    }

    private static void addDocumentScoreToTreeMap(TreeMap<Double, List<String>> scoreToDocuments, double score, String doc) {
        List<String> res = scoreToDocuments.get(score);
        if(res==null){
            res = new ArrayList<>();
        }
        res.add(doc);
        scoreToDocuments.put(score,res);

    }
    public static List<String> getWordsFromDocument(List<String> lines) {
        List<String> words = new ArrayList<>();
        for (String line : lines) {
            words.addAll(getWordsFromLine(line));
        }
        return words;
    }

    public static List<String> getWordsFromLine(String line) {
        return Arrays.asList(line.split("(\\.)+|(,)+|( )+|(-)+|(\\?)+|(!)+|(;)+|(:)+|(/d)+|(/n)+"));
    }
}


