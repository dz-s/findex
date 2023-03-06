package findex.indexer;

import java.util.HashSet;

public interface IndexerCache {
    boolean isStopWord(String word);
    void addIfAbsent(String word, String path);
    HashSet<String> getFilePathsFor(String word);
}
