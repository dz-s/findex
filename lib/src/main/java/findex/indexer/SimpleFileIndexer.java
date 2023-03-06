package findex.indexer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static findex.patterns.Lexer.ENG_WORD;

public class SimpleFileIndexer implements FileIndexer<Collection<String>> {
    private final String rootPath;
    private final ThreadSafeIndexerCache threadSafeIndexerCache;

    private final Pattern regexPattern;

    public SimpleFileIndexer(Set<String> stopWords, String _rootPath, String lexer) {
        rootPath = _rootPath;
        regexPattern = lexer == null ? Pattern.compile(ENG_WORD) : Pattern.compile(lexer);
        threadSafeIndexerCache = new ThreadSafeIndexerCache(stopWords);
    }

    private void computeFile(File file) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        for (String line = reader.readLine(); line != null; line = reader
                .readLine()) {
            for (String word : line.split(regexPattern.pattern())) {
               threadSafeIndexerCache.addIfAbsent(word, file.getAbsolutePath());
            }
        }
    }

    @Override
    public void compute() {
        computeFolder(new File(rootPath));
    }

    @Override
    public void computeFolder(File folder) {
        if (folder == null) {
            return;
        }
        var list = folder.listFiles();
        if (list != null) {
            Stream.of(list)
                    .parallel()
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
    @Override
    public Collection<String> search(Collection<String> words) {
        Set<String> answer = new HashSet<>();
        for (String word : words) {
            answer.addAll(threadSafeIndexerCache.getFilePathsFor(word));
        }
        return answer;
    }
}