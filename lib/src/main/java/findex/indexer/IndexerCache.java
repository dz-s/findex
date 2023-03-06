package findex.indexer;

import java.util.Set;

public interface IndexerCache {
    boolean isStopWord(String word);
    void addIfAbsent(String word, String path);
    Set<String> getFilePathsFor(String word);
}
