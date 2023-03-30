package search;
import cluster.management.ServiceRegistry;
import model.DocumentData;
import model.Result;
import model.SerializationUtils;
import model.Task;
import model.proto.SearchModel;
import networking.OnRequestCallback;
import networking.WebClient;
import org.apache.zookeeper.KeeperException;

import java.io.File;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
public class SearchCoordinator implements OnRequestCallback {
    // private static final String ENDPOINT = "/search";
    private static final String BOOKS_DIRECTORY = "./resources/books/";
    private final ServiceRegistry workersServiceRegistry;
    private final WebClient client;
    private final List<String> documents;

    public SearchCoordinator(ServiceRegistry workersServiceRegistry,WebClient client){
        this.workersServiceRegistry = workersServiceRegistry;
        this.client = client;
        this.documents = readDocumentsList();
    }
    private static List<String> readDocumentsList() {
        File documentsDirectory = new File(BOOKS_DIRECTORY);
        return Arrays.asList(documentsDirectory.list())
                .stream()
                .map(documentName -> BOOKS_DIRECTORY + "/" + documentName)
                .collect(Collectors.toList());
    }
    //Split documents amongst worker nodes
    private static List<List<String>> splitDocumentList(int numberOfWorkers, List<String> documents) {
        int numberOfDocumentsPerWorker = (documents.size() + numberOfWorkers - 1) / numberOfWorkers;

        List<List<String>> workersDocuments = new ArrayList<>();

        for (int i = 0; i < numberOfWorkers; i++) {
            int firstDocumentIndex = i * numberOfDocumentsPerWorker;
            int lastDocumentIndexExclusive = Math.min(firstDocumentIndex + numberOfDocumentsPerWorker, documents.size());

            if (firstDocumentIndex >= lastDocumentIndexExclusive) {
                break;
            }
            List<String> currentWorkerDocuments = new ArrayList<>(documents.subList(firstDocumentIndex, lastDocumentIndexExclusive));

            workersDocuments.add(currentWorkerDocuments);
        }
        return workersDocuments;
    }
    //Create tasks that are sent to different worker nodes
    public List<Task> createTasks(int numberOfWorkers, List<String> searchTerms) {
        List<List<String>> workersDocuments = splitDocumentList(numberOfWorkers, documents);

        List<Task> tasks = new ArrayList<>();

        for (List<String> documentsForWorker : workersDocuments) {
            Task task = new Task(searchTerms, documentsForWorker);
            tasks.add(task);
        }

        return tasks;
    }
    @Override
    public byte[] handleRequest(byte[] requestPayload) {
        try {
            SearchModel.Request request = SearchModel.Request.parseFrom(requestPayload);    
            SearchModel.Response response = createResponse(request);
            return response.toByteArray();
        } catch (Exception e) {
            e.getStackTrace();
            return SearchModel.Response.getDefaultInstance().toByteArray();
        }
    }

    private SearchModel.Response createResponse(SearchModel.Request request) throws KeeperException, InterruptedException {
        SearchModel.Response.Builder searchResponse = SearchModel.Response.newBuilder();
        String query = request.getSearchQuery();
        System.out.println("Received Search Query "+ query);
        List<String> searchTerms = TFIDF.getWordsFromLine(query);
        List<String> workers = workersServiceRegistry.getAllServiceAddresses();
        if(workers.isEmpty()){
            System.out.println("No worker nodes available");
            return searchResponse.build();
        }
        //Create tasks for workers
        List<Task> tasks = createTasks(workers.size(), searchTerms);
        List<Result> results = sendTasksToWorkers(workers,tasks);
        List<SearchModel.Response.DocumentStats> sortedDocs = aggregateResults(results,searchTerms);
        searchResponse.addAllRelevantDocuments(sortedDocs);
        return searchResponse.build();

    }
    private List<SearchModel.Response.DocumentStats> aggregateResults(List<Result> results, List<String> searchTerms) {
        Map<String,DocumentData> allDocumentResult = new HashMap<>();
        for(Result result:results){
            allDocumentResult.putAll(result.getDocumentToDocumentData());
        }
        System.out.println("Calculating score for all documents");
        Map<Double,List<String>> scoreToDocMap = TFIDF.getDocumentsSortedByScore(searchTerms, allDocumentResult);
        return sortDocumentsByScore(scoreToDocMap);
    }

    private List<SearchModel.Response.DocumentStats> sortDocumentsByScore(Map<Double, List<String>> scoreToDocMap) {
        List<SearchModel.Response.DocumentStats> sortedDocumentStatsList = new ArrayList<>();
        for(Map.Entry<Double,List<String>> docScorePair:scoreToDocMap.entrySet()){
            double score = docScorePair.getKey();
            for(String document:docScorePair.getValue()){
                File documentPath = new File(document);
                SearchModel.Response.DocumentStats docstats = SearchModel.Response.DocumentStats.newBuilder()
                    .setScore(score)
                    .setDocumentName(documentPath.getName())
                    .setDocumentSize(documentPath.length())
                    .build();
                sortedDocumentStatsList.add(docstats);
                
            }
        }
        return sortedDocumentStatsList;
    }
    @Override
    public String getEndpoint() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getEndpoint'");
    }
    
    private List<Result> sendTasksToWorkers(List<String> workers, List<Task> tasks) {
        CompletableFuture<Result>[] futures = new CompletableFuture[workers.size()];
        for (int i = 0; i < workers.size(); i++) {
            String worker = workers.get(i);
            Task task = tasks.get(i);
            byte[] payload = SerializationUtils.serialize(task);

            futures[i] = client.sendTask(worker, payload);
        }

        List<Result> results = new ArrayList<>();
        for (CompletableFuture<Result> future : futures) {
            try {
                Result result = future.get();
                results.add(result);
            } catch (InterruptedException | ExecutionException e) {
            }
        }
        System.out.println(String.format("Received %d/%d results", results.size(), tasks.size()));
        return results;
    }

    
    
}
