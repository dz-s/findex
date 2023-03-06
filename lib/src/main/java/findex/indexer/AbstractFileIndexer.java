package findex.indexer;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

public abstract class AbstractFileIndexer {
    protected final String rootPath;

    protected final ConcurrentHashMap<String, ConcurrentSkipListSet<String>> index
            = new ConcurrentHashMap<>();
    protected final ConcurrentSkipListSet<String> stopWords = new ConcurrentSkipListSet<>();

    protected AbstractFileIndexer(Set<String> _stopWords, String _rootPath) {
        rootPath = _rootPath;
        stopWords.addAll(_stopWords);
    }

    public abstract void compute() throws IOException;
    public abstract void computeFolder(File folder);

    public abstract Collection<String> search(Collection<String> words);
}