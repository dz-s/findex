package findex.indexer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static findex.patterns.Lexer.ENG_WORD;

public class SimpleFileIndexer implements FileIndexer<Collection<String>> {

    private final String regexPattern;
    private final IndexerCache cache;
    private final ExecutorService executor;
    public SimpleFileIndexer(ExecutorService executor, Set<String> stopWords,
                             String regexPattern) {
        this.regexPattern = regexPattern == null ? ENG_WORD :
                Pattern.compile(regexPattern).pattern();
        this.executor = executor;
        this.cache = new ThreadSafeIndexerCache(stopWords);
    }

    @Override
    public void compute(Queue<Callable<Path>> events) {
        executor.submit(() -> {
            while (true) {
                final var event = events.poll();
                if (event != null) {
                    final var path = event.call();
                    if (path != null) {
                        final var file = path.toFile();
                        if (file.isDirectory()) {
                            computeFolder(file);
                        } else {
                            computeFile(file);
                        }
                    }
                }
            }
        });
    }

    private void computeFolder(File folder) {
        if (folder == null) {
            return;
        }
        var list = folder.listFiles();
        if (list != null) {
            Stream.of(list)
                    .forEach(file -> {
                        try {
                            if (file.isFile()) {
                                computeFile(file);
                            } else {
                                computeFolder(file);
                            }
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
        }
    }

    private void computeFile(File file) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        for (String line = reader.readLine(); line != null; line = reader
                .readLine()) {
            for (String word : line.split(regexPattern)) {
                cache.addIfAbsent(word, file.getAbsolutePath());
            }
        }
    }

    @Override
    public Set<String> search(Collection<String> words) {
        return words.stream()
                .map(cache::getFilePathsFor)
                .reduce(new HashSet<>(), (it, other) -> {
                    it.addAll(other);
                    return it;
                });
    }
}