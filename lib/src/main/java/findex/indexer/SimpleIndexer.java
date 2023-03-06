package findex.indexer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Stream;

public class SimpleIndexer extends AbstractFileIndexer {

    public SimpleIndexer(Set<String> stopWords, String rootPath) {
        super(stopWords, rootPath);
    }

    private void computeFile(File file) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        for (String line = reader.readLine(); line != null; line = reader
                .readLine()) {
            for (String word : line.split("\\W+")) {
                if (stopWords.contains(word))
                    continue;
                ConcurrentSkipListSet<String> idx =
                        index.computeIfAbsent(word, k -> new ConcurrentSkipListSet<>());
                idx.add(file.getAbsolutePath());
            }
        }
    }

    @Override
    public void computeFolder(File folder) {
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

    @Override
    public void compute() {
        computeFolder(new File(rootPath));
    }

    @Override
    public Collection<String> search(Collection<String> words) {
        Set<String> answer = new HashSet<>();
        for (String word : words) {
            answer.addAll(index.get(word));
        }
        return answer;
    }
}