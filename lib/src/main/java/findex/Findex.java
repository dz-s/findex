package findex;

import findex.indexer.FileIndexer;
import findex.indexer.SimpleFileIndexer;
import findex.watchers.FileWatcher;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Findex {
    private final FileIndexer fileIndexer;

    private final FileWatcher fileWatcher;

    Findex(Set<String> stopWords, String rootPath, String lexer) throws IOException {
        fileIndexer = new SimpleFileIndexer(stopWords, rootPath, lexer);
        fileWatcher = new FileWatcher(fileIndexer, Path.of(rootPath), true);
    }

    public void compute() {
        try {
            ExecutorService service = Executors.newSingleThreadExecutor();
            try (Closeable close = service::shutdown) {
                service.submit(() -> {
                    try {
                        fileWatcher.processEvents();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
            fileIndexer.compute();
        } catch (IOException e){
            throw new RuntimeException(e);
        }

    }

    public Collection<String> search(Collection<String> words){
        return fileIndexer.search(words);
    }

}
