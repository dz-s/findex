package findex;

import findex.indexer.FileIndexer;
import findex.indexer.SimpleFileIndexer;
import findex.watchers.FileWatcher;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.*;


public class Findex {
    private final FileIndexer fileIndexer;
    private final FileWatcher fileWatcher;
    private final ExecutorService executor;
    private final Path rootPath;

    Findex(Set<String> stopWords, String rootPath) throws IOException {
        executor = Executors.newCachedThreadPool();
        this.rootPath = Path.of(rootPath);
        fileIndexer = new SimpleFileIndexer(executor, stopWords, null);
        fileWatcher = new FileWatcher(this.rootPath, true);
    }

    public void compute() throws InterruptedException {
        final var eventsQueue = new LinkedBlockingQueue<Callable<Path>>();
        executor.submit(() -> fileWatcher.addEvents(eventsQueue));
        eventsQueue.offer(() -> rootPath);
        executor.submit(() -> fileIndexer.compute(eventsQueue));
    }

    public Collection<String> search(Collection<String> words){
        return fileIndexer.search(words);
    }
}