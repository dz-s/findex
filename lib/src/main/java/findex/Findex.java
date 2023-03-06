package findex;

import findex.indexer.FileIndexer;
import findex.indexer.SimpleIndexer;
import findex.watchers.FileWatcher;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.*;
import java.util.function.Function;


public class Findex {
    private final FileIndexer fileIndexer;

    private final FileWatcher fileWatcher;

    private final ExecutorService executor;
    private final Path rootPath;

    Findex(Set<String> stopWords, String rootPath) throws IOException {
        executor = Executors.newCachedThreadPool();
        this.rootPath = Path.of(rootPath);
        fileIndexer = new SimpleIndexer(executor, stopWords);
        fileWatcher = new FileWatcher(executor, this.rootPath, true);
    }

    public void compute() throws InterruptedException {
        final var eventsQueue = new SynchronousQueue<Callable<Path>>();
        executor.submit(() -> fileWatcher.addEvents(eventsQueue));
        eventsQueue.offer(() -> rootPath);
        executor.submit(() -> fileIndexer.compute(eventsQueue));

        executor.awaitTermination(5, TimeUnit.MINUTES);
    }

    public Collection<String> search(Collection<String> words){
        return fileIndexer.search(words);
    }
}
