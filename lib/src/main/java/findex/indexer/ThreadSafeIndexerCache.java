package findex.indexer;

import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

public class ThreadSafeIndexerCache implements IndexerCache {
    protected final ConcurrentHashMap<String, HashSet<String>> index
            = new ConcurrentHashMap<>();
    protected final HashSet<String> stopWords;

    public ThreadSafeIndexerCache(Collection<String> stopWords) {
        this.stopWords = new HashSet<>(stopWords);
    }

    @Override
    public boolean isStopWord(String word) {
        return stopWords.contains(word);
    }

    @Override
    public void addIfAbsent(String word, String path) {
        if(isStopWord(word)){
            return;
        }
        index.compute(word, (key, list) -> {
            if (list == null) {
                list = new HashSet<>();
            }
            list.add(path);
            return list;
        });
    }

    @Override
    public HashSet<String> getFilePathsFor(String word) {
        return index.get(word);
    }
}
