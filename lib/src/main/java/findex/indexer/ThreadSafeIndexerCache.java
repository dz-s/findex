package findex.indexer;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ThreadSafeIndexerCache implements IndexerCache{

    private final ConcurrentHashMap<String, ConcurrentHashMap<String, Boolean>> index
            = new ConcurrentHashMap<>();
    private final Set<String> stopWords;

    ThreadSafeIndexerCache(Collection<String> words){
        this.stopWords = new HashSet<>(words);
    }

    @Override
    public boolean isStopWord(String word) {
        return stopWords.contains(word);
    }

    @Override
    public void addIfAbsent(String word, String path) {
        if (!isStopWord(word)){
            var idx = index.computeIfAbsent(word, k -> new ConcurrentHashMap<>());
            idx.put(path, true);
        }
    }

    @Override
    public Set<String> getFilePathsFor(String word) {
        return index.get(word).keySet();
    }
}
