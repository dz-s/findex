package findex.indexer;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

public interface FileIndexer <T extends Collection<String>> {
    void compute() throws IOException;
    void computeFolder(File folder);
    T search(T words);
}
