package findex.indexer;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.Callable;

public interface FileIndexer <T extends Collection<String>> {
    void compute(Queue<Callable<Path>> events);
    T search(Collection<String> words);
}
